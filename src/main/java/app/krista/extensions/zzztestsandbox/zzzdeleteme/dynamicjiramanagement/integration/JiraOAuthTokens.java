package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

/**
 * Persisted OAuth 2.0 (3LO) token record for one Jira identity (the operator's single connection, or
 * one end-user's own, depending on which key it is stored under in {@link JiraOAuthTokenStore}).
 *
 * Plain value object serialized to JSON by {@link JiraOAuthTokenStore} — never log {@code accessToken}
 * or {@code refreshToken}.
 */
public class JiraOAuthTokens {

    public String accessToken;
    public String refreshToken;
    public long expiresAtEpochMillis;
    public String cloudId;
    public String siteUrl;
    public String accountId;

    public JiraOAuthTokens() {
    }

    public JiraOAuthTokens(String accessToken, String refreshToken, long expiresAtEpochMillis,
                            String cloudId, String siteUrl, String accountId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAtEpochMillis = expiresAtEpochMillis;
        this.cloudId = cloudId;
        this.siteUrl = siteUrl;
        this.accountId = accountId;
    }

    public boolean isExpired() {
        // 60s safety margin so a near-expiry token isn't used for a call that then fails mid-flight.
        return System.currentTimeMillis() > (expiresAtEpochMillis - 60_000L);
    }
}
