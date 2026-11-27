package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

import app.krista.extensions.util.KeyValueStore;
import com.google.gson.Gson;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Persists {@link JiraOAuthTokens} in the platform KeyValueStore (KeyValueStore handles encryption at
 * rest — no hand-rolled encryption here).
 *
 * Three key namespaces (same store, different keys — see authentication-patterns.md Pattern C and the
 * generalized per-user dispatch note):
 *  - {@code invokerId} — the operator's single connection (the normal Setup-tab-configured case).
 *  - {@code accountId + "_jira_oauth"} — one real end-user's own tokens (per-user / MCP-attributed
 *    requests, {@code requestContext.invokeAsUser() == true}).
 *  - {@code "authContextId_" + UUID} — transient, carries the not-yet-saved candidate config through
 *    the browser OAuth round-trip; the caller removes the key once the callback consumes it.
 */
@Service
public class JiraOAuthTokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(JiraOAuthTokenStore.class);
    private static final Gson GSON = new Gson();

    public static final String PER_USER_SUFFIX = "_jira_oauth";

    private final KeyValueStore keyValueStore;

    @Inject
    public JiraOAuthTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    public void save(String key, JiraOAuthTokens tokens) {
        keyValueStore.put(key, GSON.toJson(tokens));
        LOG.info("Saved Jira OAuth tokens under key {}", key);
    }

    /** Transient placeholder, keyed by a fresh authContextId — removed by the callback once consumed. */
    public String saveTransient(JiraOAuthTokens tokens) {
        String key = "authContextId_" + UUID.randomUUID();
        keyValueStore.put(key, GSON.toJson(tokens));
        return key;
    }

    public JiraOAuthTokens load(String key) {
        Object stored = keyValueStore.get(key);
        if (stored == null) {
            return null;
        }
        try {
            return GSON.fromJson(String.valueOf(stored), JiraOAuthTokens.class);
        } catch (Exception e) {
            LOG.error("Failed to deserialize Jira OAuth tokens for key {}", key, e);
            return null;
        }
    }

    public void remove(String key) {
        keyValueStore.remove(key);
    }

    public static String perUserKey(String accountId) {
        return accountId + PER_USER_SUFFIX;
    }
}
