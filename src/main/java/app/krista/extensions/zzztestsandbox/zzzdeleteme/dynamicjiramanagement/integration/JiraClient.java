package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for Jira Cloud REST API v3. One shared {@code OkHttpClient}; auth/retry/rate-limit are
 * handled here (not per-call code); one {@code execute()} path with a single status-&gt;exception
 * mapping point.
 *
 * <p><b>Auth dispatch</b> (per authentication-patterns.md's generalized Trap 0.12 note — this applies
 * to any Pattern C build, not just Microsoft): every call resolves a {@link Session} fresh, branching
 * on {@link DynamicJiraManagementAttributes#isOAuth()} and, for OAuth, on
 * {@link RequestContext#invokeAsUser()} — operator (Setup-tab-configured, shared) vs a real end-user's
 * own Jira identity (per-user/MCP). A missing or unrefreshable OAuth token raises
 * {@link MustAuthorizeException}, which every {@code @CatalogRequest} method must route through
 * {@link app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.authentication
 * .AuthorizationExceptionHandler} rather than swallow.
 */
@Service
public class JiraClient {

    private static final Logger LOG = LoggerFactory.getLogger(JiraClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final DynamicJiraManagementAttributes attributes;
    private final JiraOAuthTokenStore tokenStore;
    private final JiraOAuthService oauthService;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final String invokerId;

    @Inject
    public JiraClient(DynamicJiraManagementAttributes attributes, JiraOAuthTokenStore tokenStore,
                       JiraOAuthService oauthService, RequestContext requestContext,
                       AuthorizationContext authorizationContext, Invoker invoker) {
        this.attributes = attributes;
        this.tokenStore = tokenStore;
        this.oauthService = oauthService;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.invokerId = invoker.getInvokerId();
    }

    // ---------------------------------------------------------------------------------------------
    // Verbs
    // ---------------------------------------------------------------------------------------------
    public JsonObject get(String path) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .header("Accept", "application/json")
                .get().build();
        return parseObject(execute(request, session));
    }

    public JsonObject post(String path, JsonObject body) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .header("Accept", "application/json")
                .post(RequestBody.create(body == null ? "{}" : body.toString(), JSON))
                .build();
        return parseObject(execute(request, session));
    }

    public JsonObject put(String path, JsonObject body) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .header("Accept", "application/json")
                .put(RequestBody.create(body == null ? "{}" : body.toString(), JSON))
                .build();
        return parseObject(execute(request, session));
    }

    /** For endpoints that return 204 No Content on success (update/transition/assign). */
    public void putNoContent(String path, JsonObject body) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .put(RequestBody.create(body == null ? "{}" : body.toString(), JSON))
                .build();
        try (Response response = execute(request, session)) {
            // 204/empty body expected; nothing to parse.
        }
    }

    public void postNoContent(String path, JsonObject body) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .post(RequestBody.create(body == null ? "{}" : body.toString(), JSON))
                .build();
        try (Response response = execute(request, session)) {
            // 204/empty body expected; nothing to parse.
        }
    }

    public void delete(String path) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .delete().build();
        try (Response response = execute(request, session)) {
            // 204 expected; nothing to parse.
        }
    }

    public JsonArray getArray(String path) throws IOException {
        Session session = resolveSession();
        Request request = new Request.Builder().url(session.baseUrl + path)
                .header("Authorization", session.authHeader)
                .header("Accept", "application/json")
                .get().build();
        try (Response response = execute(request, session)) {
            String body = response.body() != null ? response.body().string() : "[]";
            JsonElement parsed = JsonParser.parseString(body.isBlank() ? "[]" : body);
            return parsed.isJsonArray() ? parsed.getAsJsonArray() : new JsonArray();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // The single execute + retry + error-mapping path
    // ---------------------------------------------------------------------------------------------
    private Response execute(Request request, Session session) throws IOException {
        int maxAttempts = Math.max(1, attributes.getMaxRetryAttempts());
        IOException lastNetworkError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response;
            try {
                response = HTTP.newCall(request).execute();
            } catch (IOException networkError) {
                lastNetworkError = networkError;
                if (attempt < maxAttempts) {
                    long retryAfterMs = (long) Math.pow(2, attempt - 1) * attributes.getRetryDelayMillis();
                    LOG.warn("Jira API {} {} network error — retry {}/{} in {}ms: {}",
                            request.method(), request.url(), attempt, maxAttempts, retryAfterMs, networkError.getMessage());
                    sleep(retryAfterMs);
                    continue;
                }
                throw networkError;
            }
            if (response.isSuccessful()) {
                return response;
            }
            int code = response.code();
            if (shouldRetry(code) && attempt < maxAttempts) {
                long retryAfterMs = retryDelay(response, attempt);
                response.close();
                LOG.warn("Jira API {} {} -> HTTP {} — retry {}/{} in {}ms",
                        request.method(), request.url(), code, attempt, maxAttempts, retryAfterMs);
                sleep(retryAfterMs);
                continue;
            }
            handleErrorResponse(response, session);
        }
        throw lastNetworkError != null ? lastNetworkError
                : new IOException("Jira API call failed after " + maxAttempts + " attempts.");
    }

    private boolean shouldRetry(int code) {
        return code == 429 || code == 502 || code == 503 || code == 504;
    }

    private long retryDelay(Response response, int attempt) {
        String retryAfter = response.header("Retry-After");
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter.trim()) * 1000L;
            } catch (NumberFormatException ignored) {
                // fall through to backoff
            }
        }
        long base = (long) Math.pow(2, attempt - 1) * attributes.getRetryDelayMillis();
        return base + (long) (Math.random() * base * 0.5);
    }

    private void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** SINGLE status -> exception mapping point. */
    private void handleErrorResponse(Response response, Session session) throws IOException {
        int code = response.code();
        String body = response.body() != null ? response.body().string() : "";
        String apiMessage = extractMessage(body);
        LOG.error("Jira API error: {} {} -> HTTP {} | message='{}'",
                response.request().method(), response.request().url(), code, apiMessage);
        response.close();
        switch (code) {
            case 401 -> {
                if (attributes.isOAuth()) {
                    String perUserAccountId = requestContext.invokeAsUser() && authorizationContext.getAuthorizedAccount() != null
                            ? authorizationContext.getAuthorizedAccount().getAccountId() : null;
                    throw authorizeExceptionForMissingToken(perUserAccountId);
                }
                throw new SecurityException(withDetail(
                        "Authentication failed — check the Account Email / API Token on the Setup tab.", apiMessage));
            }
            case 403 -> throw new SecurityException(withDetail(
                    "Permission denied — this Jira account cannot perform that action (check Jira project "
                            + "permissions).", apiMessage));
            case 404 -> throw new IllegalArgumentException(withDetail(
                    "Not found — check the project key, issue key, or field id.", apiMessage));
            case 400, 422 -> throw new IllegalArgumentException(withDetail(
                    "Jira rejected the request — check the submitted field values against the discovered "
                            + "schema.", apiMessage));
            case 409 -> throw new IllegalStateException(withDetail("Conflict with the current Jira state.", apiMessage));
            case 429 -> throw new IllegalStateException(withDetail(
                    "Jira rate limit exceeded — please retry shortly.", apiMessage));
            default -> throw new IOException("Jira API error (" + code + "): " + apiMessage);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Session resolution — operator (API Token) / operator (OAuth) / per-user (OAuth, MCP-attributed)
    // ---------------------------------------------------------------------------------------------
    private Session resolveSession() throws IOException {
        if (attributes.isApiToken()) {
            if (isBlank(attributes.getSiteUrl()) || isBlank(attributes.getAccountEmail())
                    || isBlank(attributes.getApiToken())) {
                throw new IllegalStateException(
                        "Jira is not configured — set Jira Site URL, Account Email, and API Token on the Setup tab.");
            }
            String basic = Base64.getEncoder().encodeToString(
                    (attributes.getAccountEmail() + ":" + attributes.getApiToken()).getBytes(StandardCharsets.UTF_8));
            return new Session(attributes.getSiteUrl(), "Basic " + basic, attributes.getSiteUrl());
        }
        if (!requestContext.invokeAsUser()) {
            return resolveOAuthSession(invokerId, null);
        }
        String accountId = authorizationContext.getAuthorizedAccount() != null
                ? authorizationContext.getAuthorizedAccount().getAccountId() : null;
        if (accountId == null) {
            throw new IllegalStateException("No end-user identity is attached to this request.");
        }
        return resolveOAuthSession(JiraOAuthTokenStore.perUserKey(accountId), accountId);
    }

    private Session resolveOAuthSession(String storeKey, String perUserAccountId) throws IOException {
        JiraOAuthTokens tokens = tokenStore.load(storeKey);
        if (tokens == null) {
            throw authorizeExceptionForMissingToken(perUserAccountId);
        }
        if (tokens.isExpired()) {
            tokens = refresh(storeKey, tokens, perUserAccountId);
        }
        return new Session("https://api.atlassian.com/ex/jira/" + tokens.cloudId, "Bearer " + tokens.accessToken,
                tokens.siteUrl);
    }

    /** The human-facing, browsable Jira site URL (distinct from the API routing base used for calls —
     *  OAuth mode routes calls through api.atlassian.com/ex/jira/&lt;cloudId&gt;, not the site itself). */
    public String getIssueUrl(String issueKey) throws IOException {
        Session session = resolveSession();
        return session.browseBaseUrl + "/browse/" + issueKey;
    }

    public String getBrowseBaseUrl() throws IOException {
        return resolveSession().browseBaseUrl;
    }

    private JiraOAuthTokens refresh(String storeKey, JiraOAuthTokens tokens, String perUserAccountId) throws IOException {
        if (tokens.refreshToken == null || tokens.refreshToken.isBlank()) {
            throw authorizeExceptionForMissingToken(perUserAccountId);
        }
        try {
            OAuth20Service service = oauthService.buildService(attributes);
            OAuth2AccessToken refreshed = oauthService.refresh(service, tokens.refreshToken);
            long expiresAt = System.currentTimeMillis()
                    + (refreshed.getExpiresIn() != null ? refreshed.getExpiresIn() * 1000L : 3600_000L);
            JiraOAuthTokens updated = new JiraOAuthTokens(
                    refreshed.getAccessToken(),
                    refreshed.getRefreshToken() != null ? refreshed.getRefreshToken() : tokens.refreshToken,
                    expiresAt, tokens.cloudId, tokens.siteUrl, tokens.accountId);
            tokenStore.save(storeKey, updated);
            return updated;
        } catch (Exception e) {
            LOG.warn("Jira OAuth token refresh failed for key {}: {}", storeKey, e.getMessage());
            throw authorizeExceptionForMissingToken(perUserAccountId);
        }
    }

    private MustAuthorizeException authorizeExceptionForMissingToken(String perUserAccountId) {
        if (perUserAccountId != null) {
            return new MustAuthorizeException(
                    "No Jira authorization found for this user. Please sign in to Jira.",
                    List.of(new NamedValuedField("clientUserId", "Text", perUserAccountId, Map.of(), Map.of()),
                            new NamedValuedField("userId", "Text", invokerId, Map.of(), Map.of())));
        }
        return new MustAuthorizeException(
                "No Jira authorization configured. Use Test Connection on the Setup tab to authorize.",
                List.of(new NamedValuedField("userId", "Text", invokerId, Map.of(), Map.of())));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private JsonObject parseObject(Response response) throws IOException {
        try (response) {
            String body = response.body() != null ? response.body().string() : "{}";
            if (body.isBlank()) {
                return new JsonObject();
            }
            JsonElement parsed = JsonParser.parseString(body);
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
        }
    }

    private static String extractMessage(String body) {
        try {
            JsonObject o = JsonParser.parseString(body).getAsJsonObject();
            if (o.has("errorMessages") && o.get("errorMessages").isJsonArray() && !o.getAsJsonArray("errorMessages").isEmpty()) {
                return o.getAsJsonArray("errorMessages").get(0).getAsString();
            }
            if (o.has("errors") && o.get("errors").isJsonObject()) {
                return o.getAsJsonObject("errors").toString();
            }
            if (o.has("message")) {
                return o.get("message").getAsString();
            }
        } catch (Exception ignored) {
            // fall through to raw body
        }
        return body.length() > 300 ? body.substring(0, 300) : body;
    }

    private static String withDetail(String friendly, String api) {
        return (api == null || api.isBlank()) ? friendly : friendly + " (" + api + ")";
    }

    private static final class Session {
        final String baseUrl;
        final String authHeader;
        final String browseBaseUrl;

        Session(String baseUrl, String authHeader, String browseBaseUrl) {
            this.baseUrl = baseUrl;
            this.authHeader = authHeader;
            this.browseBaseUrl = browseBaseUrl;
        }
    }
}
