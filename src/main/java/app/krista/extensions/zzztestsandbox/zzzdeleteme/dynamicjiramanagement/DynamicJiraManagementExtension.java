package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.ChangeLog;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.authentication.JiraRequestAuthenticator;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

/**
 * Dynamic Jira Management extension entry point.
 *
 * Authentication combines BOTH confirmed mechanisms ("api-key,oauth2-interactive") behind one
 * {@code Authentication Method} PickOne (servicenow-style, see authentication-patterns.md): "API Token"
 * (Pattern A — Basic auth against the tenant's own site) or "OAuth 2.0" (Pattern C — Atlassian 3-legged
 * browser authorization). No custom Authentication tab is needed for either (that is a Pattern-D-only
 * requirement) — both live on the standard Setup tab.
 */
@Field.PickOne(name = DynamicJiraManagementAttributes.AUTH_TYPE,
        values = {DynamicJiraManagementAttributes.AUTH_TYPE_API_TOKEN, DynamicJiraManagementAttributes.AUTH_TYPE_OAUTH})
@Field.Text(value = DynamicJiraManagementAttributes.SITE_URL, required = false,
        description = "Required for API Token — e.g. https://your-company.atlassian.net")
@Field.Text(value = DynamicJiraManagementAttributes.ACCOUNT_EMAIL, required = false,
        description = "Required for API Token — the Atlassian account email the token belongs to.")
@Field.Text(value = DynamicJiraManagementAttributes.API_TOKEN, required = false, isSecured = true,
        description = "Required for API Token — create one at id.atlassian.com/manage-profile/security/api-tokens.")
@Field.Text(value = DynamicJiraManagementAttributes.OAUTH_CLIENT_ID, required = false,
        description = "Required for OAuth 2.0 — from your Atlassian OAuth 2.0 (3LO) app.")
@Field.Text(value = DynamicJiraManagementAttributes.OAUTH_CLIENT_SECRET, required = false, isSecured = true,
        description = "Required for OAuth 2.0 — from your Atlassian OAuth 2.0 (3LO) app.")
@Field.Text(value = DynamicJiraManagementAttributes.OAUTH_REDIRECT_URL, required = false,
        description = "Required for OAuth 2.0 — must exactly match a Redirect URI registered on the app.")
@Field.Boolean(value = DynamicJiraManagementAttributes.DEFAULT_ASSIGN_TO_SELF, required = false)
@Field.Text(value = DynamicJiraManagementAttributes.TIMEOUT_SECONDS, required = false)
@Field.Text(value = DynamicJiraManagementAttributes.MAX_RETRY_ATTEMPTS, required = false)
@Field.Text(value = DynamicJiraManagementAttributes.RETRY_DELAY_MILLIS, required = false)
@Field.Text(value = DynamicJiraManagementAttributes.SCHEMA_CACHE_MINUTES, required = false)
@Extension(name = "Dynamic Jira Management", version = "1.0.0",
        description = "Create, update, comment on, transition, assign, and search Jira tickets across "
                + "projects via natural-language requests, discovering each project's and issue type's "
                + "field schema dynamically instead of using one fixed ticket format.")
@StaticResource(path = "docs", file = "docs")
@ChangeLog(file = "docs/pages/release-notes.md")
@Java(version = Java.Version.JAVA_21)
public final class DynamicJiraManagementExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicJiraManagementExtension.class);

    private final Invoker invoker;
    private final DynamicJiraManagementAttributes attributes;
    private final JiraClient client;
    private final JiraRequestAuthenticator requestAuthenticator;

    @Inject
    public DynamicJiraManagementExtension(Invoker invoker, DynamicJiraManagementAttributes attributes,
                                           JiraClient client, JiraOAuthService oauthService) {
        this.invoker = invoker;
        this.attributes = attributes;
        this.client = client;
        // Built with plain `new` — NOT @Service/@Inject on its own constructor. HK2 cannot bind a bare
        // String (invokerId) in any context; getting this wrong would blank every custom tab, not just
        // auth, because getCustomTabs() needs this whole class constructed first.
        this.requestAuthenticator = new JiraRequestAuthenticator(attributes, oauthService, invoker.getInvokerId());
    }

    /** Setup-tab tabs. Patterns A/C need only the bundled Documentation tab — no custom Authentication
     *  tab (that is a Pattern-D-only requirement). */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    /** Validate configuration before it is saved. Throw to reject. */
    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> connectionAttributes) {
        DynamicJiraManagementAttributes candidate = DynamicJiraManagementAttributes.create(invoker, connectionAttributes);
        if (candidate.isApiToken()) {
            requireNonBlank(candidate.getSiteUrl(), "Jira Site URL");
            if (!candidate.getSiteUrl().startsWith("https://")) {
                throw new IllegalArgumentException("Jira Site URL must start with https://, e.g. "
                        + "https://your-company.atlassian.net");
            }
            requireNonBlank(candidate.getAccountEmail(), "Account Email");
            requireNonBlank(candidate.getApiToken(), "API Token");
        } else {
            requireNonBlank(candidate.getOauthClientId(), "OAuth Client ID");
            requireNonBlank(candidate.getOauthClientSecret(), "OAuth Client Secret");
            requireNonBlank(candidate.getOauthRedirectUrl(), "OAuth Redirect URL");
        }
    }

    /** Test-Connection button. For OAuth, a MustAuthorizeException here is expected and must NOT be
     *  caught — the platform uses {@link JiraRequestAuthenticator#getMustAuthorizeResponse} to drive the
     *  browser authorization popup. */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() throws IOException, MustAuthorizeException {
        client.get("/rest/api/3/myself");
        LOGGER.info("Dynamic Jira Management test connection succeeded for invoker {}", invoker.getInvokerId());
    }

    /** React to Setup-tab configuration changes. */
    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        attributes.update(newAttributes);
    }

    /** Registers the request authenticator for real catalog-request authorization. */
    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }
}
