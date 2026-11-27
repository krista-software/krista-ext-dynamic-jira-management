package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Wraps the Atlassian OAuth 2.0 (3LO) authorization-code flow (ScribeJava) plus the one extra step
 * every Jira Cloud OAuth app needs: resolving the token-scoped {@code cloudId} via
 * {@code GET https://api.atlassian.com/oauth/token/accessible-resources} (api-model.json
 * "getAccessibleResources") before any {@code api.atlassian.com/ex/jira/<cloudId>/...} call can be made.
 *
 * The required Jira scopes for this extension's operations: read:jira-user, read:jira-work,
 * write:jira-work, manage:jira-project (component/version metadata lookups), offline_access (refresh
 * tokens).
 */
@Service
public class JiraOAuthService {

    private static final Logger LOG = LoggerFactory.getLogger(JiraOAuthService.class);
    private static final String SCOPE =
            "read:jira-user read:jira-work write:jira-work manage:jira-project offline_access";

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public OAuth20Service buildService(DynamicJiraManagementAttributes attributes) {
        if (isBlank(attributes.getOauthClientId()) || isBlank(attributes.getOauthClientSecret())
                || isBlank(attributes.getOauthRedirectUrl())) {
            throw new IllegalArgumentException(
                    "OAuth Client ID, OAuth Client Secret, and OAuth Redirect URL are all required for "
                            + "the OAuth 2.0 authentication method. Configure them on the Setup tab.");
        }
        return new ServiceBuilder(attributes.getOauthClientId())
                .apiSecret(attributes.getOauthClientSecret())
                .defaultScope(SCOPE)
                .callback(attributes.getOauthRedirectUrl())
                .build(AtlassianApi.instance());
    }

    /** Atlassian additionally requires audience + prompt=consent on the authorize URL. */
    public String buildAuthorizationUrl(OAuth20Service service, String state) {
        return service.createAuthorizationUrlBuilder()
                .state(state)
                .additionalParams(Map.of("audience", "api.atlassian.com", "prompt", "consent"))
                .build();
    }

    public OAuth2AccessToken exchangeCode(OAuth20Service service, String code) throws IOException {
        try {
            return service.getAccessToken(code);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while exchanging the Jira authorization code.", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IOException("Failed to exchange the Jira authorization code for a token.", e.getCause());
        }
    }

    public OAuth2AccessToken refresh(OAuth20Service service, String refreshToken) throws IOException {
        try {
            return service.refreshAccessToken(refreshToken);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while refreshing the Jira access token.", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IOException("Failed to refresh the Jira access token — re-authorization may be required.",
                    e.getCause());
        }
    }

    /**
     * Resolves the first accessible Jira site's cloudId + url for this token. Jira Cloud OAuth apps are
     * not scoped to a single tenant up front — this is the one extra call the interactive flow needs
     * that the API-token flow does not.
     */
    public JiraSite resolveSite(String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.atlassian.com/oauth/token/accessible-resources")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .get()
                .build();
        try (Response response = HTTP.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Could not resolve the accessible Jira site (HTTP " + response.code() + ").");
            }
            String body = response.body() != null ? response.body().string() : "[]";
            JsonArray resources = JsonParser.parseString(body).getAsJsonArray();
            if (resources.isEmpty()) {
                throw new IOException("This Atlassian account has no accessible Jira sites authorized for this app.");
            }
            JsonObject first = resources.get(0).getAsJsonObject();
            return new JiraSite(first.get("id").getAsString(), first.get("url").getAsString());
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static final class JiraSite {
        public final String cloudId;
        public final String url;

        public JiraSite(String cloudId, String url) {
            this.cloudId = cloudId;
            this.url = url;
        }
    }
}
