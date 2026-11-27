package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicJiraManagementAttributesTest {

    @Test
    void defaultAuthType_isApiToken() {
        var attrs = new DynamicJiraManagementAttributes(new HashMap<>());

        assertTrue(attrs.isApiToken());
        assertFalse(attrs.isOAuth());
    }

    @Test
    void authTypeOAuth_switchesIsOAuth() {
        Map<String, Object> map = new HashMap<>();
        map.put(DynamicJiraManagementAttributes.AUTH_TYPE, DynamicJiraManagementAttributes.AUTH_TYPE_OAUTH);

        var attrs = new DynamicJiraManagementAttributes(map);

        assertTrue(attrs.isOAuth());
        assertFalse(attrs.isApiToken());
    }

    @Test
    void siteUrl_trailingSlashIsNormalizedAway() {
        Map<String, Object> map = new HashMap<>();
        map.put(DynamicJiraManagementAttributes.SITE_URL, "https://example.atlassian.net/");

        var attrs = new DynamicJiraManagementAttributes(map);

        assertEquals("https://example.atlassian.net", attrs.getSiteUrl());
    }

    @Test
    void numericDefaults_applyWhenAttributesMissing() {
        var attrs = new DynamicJiraManagementAttributes(new HashMap<>());

        assertEquals(30, attrs.getTimeoutSeconds());
        assertEquals(3, attrs.getMaxRetryAttempts());
        assertEquals(500L, attrs.getRetryDelayMillis());
        assertEquals(30, attrs.getSchemaCacheMinutes());
    }

    @Test
    void update_appliesNewValuesOverOld() {
        var attrs = new DynamicJiraManagementAttributes(new HashMap<>());
        Map<String, Object> updated = new HashMap<>();
        updated.put(DynamicJiraManagementAttributes.SCHEMA_CACHE_MINUTES, 60);

        attrs.update(updated);

        assertEquals(60, attrs.getSchemaCacheMinutes());
    }
}
