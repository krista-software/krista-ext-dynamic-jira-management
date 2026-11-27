package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraOAuthServiceTest {

    private final JiraOAuthService service = new JiraOAuthService();

    @Test
    void buildService_missingClientId_throwsActionableError() {
        DynamicJiraManagementAttributes attrs = oauthAttrs(null, "secret", "https://example.com/callback");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.buildService(attrs));
        assertTrue(ex.getMessage().contains("OAuth Client ID"));
    }

    @Test
    void buildService_missingRedirectUrl_throwsActionableError() {
        DynamicJiraManagementAttributes attrs = oauthAttrs("client-id", "secret", null);

        assertThrows(IllegalArgumentException.class, () -> service.buildService(attrs));
    }

    @Test
    void buildService_allFieldsPresent_buildsScribeJavaService() {
        DynamicJiraManagementAttributes attrs = oauthAttrs("client-id", "secret", "https://example.com/callback");

        OAuth20Service oauthService = service.buildService(attrs);

        assertTrue(oauthService != null);
    }

    @Test
    void buildAuthorizationUrl_includesAtlassianRequiredParams() {
        DynamicJiraManagementAttributes attrs = oauthAttrs("client-id", "secret", "https://example.com/callback");
        OAuth20Service oauthService = service.buildService(attrs);

        String url = service.buildAuthorizationUrl(oauthService, "op:invoker-1");

        assertTrue(url.contains("audience=api.atlassian.com"));
        assertTrue(url.contains("prompt=consent"));
        assertTrue(url.contains("state=op%3Ainvoker-1") || url.contains("state=op:invoker-1"));
    }

    @Test
    void atlassianApi_usesTheDocumentedFixedEndpoints() {
        assertEquals("https://auth.atlassian.com/oauth/token", AtlassianApi.instance().getAccessTokenEndpoint());
    }

    private static DynamicJiraManagementAttributes oauthAttrs(String clientId, String secret, String redirectUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put(DynamicJiraManagementAttributes.AUTH_TYPE, DynamicJiraManagementAttributes.AUTH_TYPE_OAUTH);
        if (clientId != null) {
            map.put(DynamicJiraManagementAttributes.OAUTH_CLIENT_ID, clientId);
        }
        if (secret != null) {
            map.put(DynamicJiraManagementAttributes.OAUTH_CLIENT_SECRET, secret);
        }
        if (redirectUrl != null) {
            map.put(DynamicJiraManagementAttributes.OAUTH_REDIRECT_URL, redirectUrl);
        }
        return new DynamicJiraManagementAttributes(map);
    }
}
