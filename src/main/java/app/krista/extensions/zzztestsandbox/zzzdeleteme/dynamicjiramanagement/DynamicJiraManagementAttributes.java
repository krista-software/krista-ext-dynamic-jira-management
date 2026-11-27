package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement;

import app.krista.extension.executor.Invoker;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * Dynamic Jira Management connection configuration (Setup tab).
 *
 * The user's confirmed authentication choice combines TWO mechanisms ("api-key,oauth2-interactive"),
 * so both sets of fields are declared and {@link #AUTH_TYPE} (a {@code @Field.PickOne}, servicenow
 * style — see authentication-patterns.md) selects which one applies for this invoker:
 *  - "API Token" (Pattern A): Account Email + API Token, sent as HTTP Basic auth directly against the
 *    tenant's own site ({@code https://<site>.atlassian.net}).
 *  - "OAuth 2.0" (Pattern C): OAuth Client ID + Client Secret (the admin's own Atlassian OAuth 2.0
 *    (3LO) app, registered at developer.atlassian.com), interactive browser authorization; calls route
 *    through {@code https://api.atlassian.com/ex/jira/<cloudId>} using a resolved cloudId.
 *
 * HK2 {@code @Service} so it can be injected into the extension and every integration class. Keep the
 * key strings stable — they are the saved-configuration contract.
 */
@Service
public class DynamicJiraManagementAttributes {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicJiraManagementAttributes.class);

    public static final String AUTH_TYPE = "Authentication Method";
    public static final String AUTH_TYPE_API_TOKEN = "API Token";
    public static final String AUTH_TYPE_OAUTH = "OAuth 2.0";

    public static final String SITE_URL = "Jira Site URL";
    public static final String ACCOUNT_EMAIL = "Account Email";
    public static final String API_TOKEN = "API Token";

    public static final String OAUTH_CLIENT_ID = "OAuth Client ID";
    public static final String OAUTH_CLIENT_SECRET = "OAuth Client Secret";
    /** Must exactly match a Redirect URI registered on the Atlassian OAuth 2.0 (3LO) app (Atlassian
     *  requires exact pre-registered redirect URIs — there is no way to derive this reliably without
     *  the admin registering it first, so it is captured explicitly rather than guessed). */
    public static final String OAUTH_REDIRECT_URL = "OAuth Redirect URL";

    public static final String DEFAULT_ASSIGN_TO_SELF = "Default Assignee Is Requesting User";
    public static final String TIMEOUT_SECONDS = "Timeout Seconds";
    public static final String MAX_RETRY_ATTEMPTS = "Max Retry Attempts";
    public static final String RETRY_DELAY_MILLIS = "Retry Delay Millis";
    public static final String SCHEMA_CACHE_MINUTES = "Schema Cache Minutes";

    private String authType;
    private String siteUrl;
    private String accountEmail;
    private String apiToken;
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthRedirectUrl;
    private boolean defaultAssignToSelf;
    private int timeoutSeconds;
    private int maxRetryAttempts;
    private long retryDelayMillis;
    private int schemaCacheMinutes;

    @Inject
    public DynamicJiraManagementAttributes(Invoker invoker) {
        this(invoker.getAttributes());
    }

    public DynamicJiraManagementAttributes(Map<String, Object> attributes) {
        update(attributes);
    }

    public static DynamicJiraManagementAttributes create(Invoker invoker, Map<String, Object> attributes) {
        DynamicJiraManagementAttributes result = new DynamicJiraManagementAttributes(invoker);
        result.update(attributes);
        return result;
    }

    public void update(Map<String, Object> attributes) {
        this.authType = getString(attributes, AUTH_TYPE, AUTH_TYPE_API_TOKEN);
        this.siteUrl = normalizeUrl(getString(attributes, SITE_URL, ""));
        this.accountEmail = getString(attributes, ACCOUNT_EMAIL, "");
        this.apiToken = getString(attributes, API_TOKEN, "");
        this.oauthClientId = getString(attributes, OAUTH_CLIENT_ID, "");
        this.oauthClientSecret = getString(attributes, OAUTH_CLIENT_SECRET, "");
        this.oauthRedirectUrl = getString(attributes, OAUTH_REDIRECT_URL, "");
        this.defaultAssignToSelf = getBoolean(attributes, DEFAULT_ASSIGN_TO_SELF, false);
        this.timeoutSeconds = getInt(attributes, TIMEOUT_SECONDS, 30);
        this.maxRetryAttempts = getInt(attributes, MAX_RETRY_ATTEMPTS, 3);
        this.retryDelayMillis = getInt(attributes, RETRY_DELAY_MILLIS, 500);
        this.schemaCacheMinutes = getInt(attributes, SCHEMA_CACHE_MINUTES, 30);
        LOGGER.info("Dynamic Jira Management attributes updated - authType={}, siteUrl={}", authType, siteUrl);
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String getString(Map<String, Object> attributes, String key, String defaultValue) {
        Object value = attributes == null ? null : attributes.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private boolean getBoolean(Map<String, Object> attributes, String key, boolean defaultValue) {
        Object value = attributes == null ? null : attributes.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        return value != null ? Boolean.parseBoolean(value.toString()) : defaultValue;
    }

    private int getInt(Map<String, Object> attributes, String key, int defaultValue) {
        Object value = attributes == null ? null : attributes.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (int) Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isOAuth() {
        return AUTH_TYPE_OAUTH.equalsIgnoreCase(authType);
    }

    public boolean isApiToken() {
        return !isOAuth();
    }

    public String getAuthType() { return authType; }
    public String getSiteUrl() { return siteUrl; }
    public String getAccountEmail() { return accountEmail; }
    public String getApiToken() { return apiToken; }
    public String getOauthClientId() { return oauthClientId; }
    public String getOauthClientSecret() { return oauthClientSecret; }
    public String getOauthRedirectUrl() { return oauthRedirectUrl; }
    public boolean isDefaultAssignToSelf() { return defaultAssignToSelf; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public long getRetryDelayMillis() { return retryDelayMillis; }
    public int getSchemaCacheMinutes() { return schemaCacheMinutes; }
}
