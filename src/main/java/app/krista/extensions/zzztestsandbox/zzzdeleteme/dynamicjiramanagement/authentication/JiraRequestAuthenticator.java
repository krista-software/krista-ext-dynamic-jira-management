package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.authentication;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraOAuthService;
import app.krista.model.field.NamedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single {@code RequestAuthenticator} for BOTH confirmed authentication mechanisms (the admin's choice
 * of "API Token" vs "OAuth 2.0" on the Setup tab's {@code Authentication Method} PickOne) — Krista
 * registers exactly one authenticator per extension (one
 * {@code @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)} hook), so it dispatches internally:
 *
 *  - "API Token" (Pattern A): no interactive flow. The invoker itself is the authenticated account —
 *    {@link #getMustAuthorizeResponse} is never actually reached for this mode (the client never
 *    throws {@link MustAuthorizeException} when the configured auth type is API Token).
 *  - "OAuth 2.0" (Pattern C): builds the Atlassian authorize URL (ScribeJava) when a
 *    {@link MustAuthorizeException} is thrown, and resolves the authenticated account id from the
 *    callback's {@code state} query parameter.
 *
 * Built with plain {@code new} inside {@code DynamicJiraManagementExtension}'s constructor — NOT
 * {@code @Service}/{@code @Inject} on its own constructor, since HK2 cannot bind a bare {@code String}
 * (invokerId) in any context (see krista-extension-scaffolding's authentication-patterns.md "Common
 * auth traps" and the HK2 injection gotcha called out for this build).
 */
public class JiraRequestAuthenticator implements RequestAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(JiraRequestAuthenticator.class);
    private static final Pattern STATE_PATTERN = Pattern.compile("^(.*?)(?:#(.*))?$");

    private final DynamicJiraManagementAttributes attributes;
    private final JiraOAuthService oauthService;
    private final String invokerId;

    public JiraRequestAuthenticator(DynamicJiraManagementAttributes attributes, JiraOAuthService oauthService,
                                     String invokerId) {
        this.attributes = attributes;
        this.oauthService = oauthService;
        this.invokerId = invokerId;
    }

    @Override
    public String getScheme() {
        return attributes.isOAuth() ? "OAuth2" : "ApiKey";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of("https");
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        if (attributes.isApiToken()) {
            // Token-based: the invoker is the authenticated account (Pattern A).
            return invokerId;
        }
        // Pattern C callback: `state` carries "op:<invokerId>[#authContextId]" or "user:<accountId>[#authContextId]".
        String state = extractStateParam(protoRequest);
        if (state == null || state.isBlank()) {
            return invokerId;
        }
        String withoutPrefix = state.startsWith("op:") || state.startsWith("user:")
                ? state.substring(state.indexOf(':') + 1) : state;
        Matcher m = STATE_PATTERN.matcher(withoutPrefix);
        return m.matches() ? m.group(1) : withoutPrefix;
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Collections.emptyMap();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException exception, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException exception) {
        return new AuthorizationResponse(null, List.of());
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException exception, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException exception) {
        if (attributes.isApiToken()) {
            // Should not happen for the API Token mode — no interactive flow exists for it.
            LOG.warn("MustAuthorizeException raised while Authentication Method is API Token; "
                    + "no authorize URL can be built.");
            return new AuthorizationResponse(null, List.of());
        }
        String state = stateFromDetails(exception);
        String authorizeUrl = oauthService.buildAuthorizationUrl(oauthService.buildService(attributes), state);
        return new AuthorizationResponse(authorizeUrl, List.of());
    }

    /**
     * state = "op:&lt;invokerId&gt;[#authContextId]" for the operator/Setup-tab flow, or
     * "user:&lt;accountId&gt;[#authContextId]" for a per-user/MCP consent flow — the callback resource
     * uses this prefix to decide which {@link app.krista.extensions.zzztestsandbox.zzzdeleteme
     * .dynamicjiramanagement.integration.JiraOAuthTokenStore} keyspace to save into.
     */
    private static String stateFromDetails(MustAuthorizeException exception) {
        String authContextId = findDetail(exception, "authContextId");
        String clientUserId = findDetail(exception, "clientUserId");
        if (clientUserId != null) {
            return authContextId == null ? "user:" + clientUserId : "user:" + clientUserId + "#" + authContextId;
        }
        String userId = findDetail(exception, "userId");
        if (userId == null) {
            userId = "unknown";
        }
        return authContextId == null ? "op:" + userId : "op:" + userId + "#" + authContextId;
    }

    private static String findDetail(MustAuthorizeException exception, String name) {
        if (exception.getDetails() == null) {
            return null;
        }
        return exception.getDetails().stream()
                .filter(d -> name.equals(d.getName()))
                .map(d -> String.valueOf(d.getValue()))
                .findFirst()
                .orElse(null);
    }

    private static String extractStateParam(ProtoRequest protoRequest) {
        try {
            Object stateValue = protoRequest.getClass().getMethod("getQueryParam", String.class)
                    .invoke(protoRequest, "state");
            return stateValue != null ? String.valueOf(stateValue) : null;
        } catch (ReflectiveOperationException e) {
            LOG.debug("ProtoRequest does not expose getQueryParam(state); falling back to invokerId.", e);
            return null;
        }
    }
}
