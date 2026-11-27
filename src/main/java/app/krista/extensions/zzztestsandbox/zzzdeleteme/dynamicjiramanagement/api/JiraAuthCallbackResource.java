package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.api;

import app.krista.extensions.util.EventHandler;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraOAuthService;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraOAuthTokenStore;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraOAuthTokens;
import app.krista.ksdk.authentication.AuthorizationListener;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * OAuth 2.0 (3LO) callback resource (Pattern C). Exchanges {@code code} for an access/refresh token
 * pair, resolves the accessible Jira site's cloudId, then persists both under the correct
 * {@link JiraOAuthTokenStore} keyspace depending on whether {@code state} identifies the operator
 * ("op:&lt;invokerId&gt;") or a specific end-user ("user:&lt;accountId&gt;") — see
 * {@link app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.authentication.JiraRequestAuthenticator}.
 */
@Path("/")
public final class JiraAuthCallbackResource {

    private static final Logger LOG = LoggerFactory.getLogger(JiraAuthCallbackResource.class);

    private final AuthorizationListener authorizationListener;
    private final EventHandler eventHandler;
    private final DynamicJiraManagementAttributes attributes;
    private final JiraOAuthService oauthService;
    private final JiraOAuthTokenStore tokenStore;

    @Inject
    public JiraAuthCallbackResource(AuthorizationListener authorizationListener, EventHandler eventHandler,
                                     DynamicJiraManagementAttributes attributes, JiraOAuthService oauthService,
                                     JiraOAuthTokenStore tokenStore) {
        this.authorizationListener = authorizationListener;
        this.eventHandler = eventHandler;
        this.attributes = attributes;
        this.oauthService = oauthService;
        this.tokenStore = tokenStore;
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML)
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state,
                              @QueryParam("error") String error) {
        if (error != null && !error.isBlank()) {
            LOG.warn("Jira OAuth authorization was denied or failed: {}", error);
            return htmlResponse("Authorization was not completed (" + error + "). You may close this window.");
        }
        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            LOG.warn("Jira OAuth callback missing code/state.");
            return htmlResponse("Authorization callback is missing required parameters. You may close this window.");
        }
        try {
            String prefix = state.startsWith("user:") ? "user" : "op";
            String id = state.substring(state.indexOf(':') + 1);
            int hash = id.indexOf('#');
            if (hash >= 0) {
                id = id.substring(0, hash);
            }

            OAuth20Service service = oauthService.buildService(attributes);
            OAuth2AccessToken token = oauthService.exchangeCode(service, code);
            JiraOAuthService.JiraSite site = oauthService.resolveSite(token.getAccessToken());

            long expiresAt = System.currentTimeMillis()
                    + (token.getExpiresIn() != null ? token.getExpiresIn() * 1000L : 3600_000L);
            JiraOAuthTokens tokens = new JiraOAuthTokens(
                    token.getAccessToken(), token.getRefreshToken(), expiresAt, site.cloudId, site.url, id);

            String storeKey = "user".equals(prefix) ? JiraOAuthTokenStore.perUserKey(id) : id;
            tokenStore.save(storeKey, tokens);
            authorizationListener.authorized();

            LOG.info("Jira OAuth authorization completed for {} (site={})", "user".equals(prefix) ? "end-user" : "operator", site.url);
            return htmlResponse("Jira authorization complete. You may close this window.");
        } catch (Exception e) {
            LOG.error("Jira OAuth callback failed", e);
            return htmlResponse("Authorization failed: " + e.getMessage() + ". You may close this window and try again.");
        }
    }

    private static Response htmlResponse(String message) {
        return Response.ok("<html><body>" + escape(message) + "</body></html>").build();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
