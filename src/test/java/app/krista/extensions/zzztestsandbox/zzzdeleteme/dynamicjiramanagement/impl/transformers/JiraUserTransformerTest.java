package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraUserTransformerTest {

    @Test
    void transform_mapsAllFields() {
        var json = JsonParser.parseString(
                "{\"accountId\":\"abc123\",\"displayName\":\"Jane Doe\",\"emailAddress\":\"jane@example.com\","
                        + "\"active\":true,\"accountType\":\"atlassian\"}").getAsJsonObject();

        JiraUser user = JiraUserTransformer.transform(json);

        assertEquals("abc123", user.accountId);
        assertEquals("Jane Doe", user.displayName);
        assertEquals("jane@example.com", user.email);
        assertEquals(Boolean.TRUE, user.active);
        assertEquals("atlassian", user.accountType);
    }

    @Test
    void transform_nullInput_returnsNull() {
        assertNull(JiraUserTransformer.transform(null));
    }

    @Test
    void transformList_skipsNonObjectsAndNeverReturnsNull() {
        JsonArray array = JsonParser.parseString("[{\"accountId\":\"a\"}, \"not-an-object\", 42]").getAsJsonArray();

        List<JiraUser> users = JiraUserTransformer.transformList(array);

        assertEquals(1, users.size());
        assertEquals("a", users.get(0).accountId);
    }

    @Test
    void transformList_nullArray_returnsEmptyNeverNull() {
        assertTrue(JiraUserTransformer.transformList(null).isEmpty());
    }

    @Test
    void toInlineMap_usesPascalKeysForEntityFieldCompatibility() {
        var json = JsonParser.parseString(
                "{\"accountId\":\"abc123\",\"displayName\":\"Jane Doe\",\"emailAddress\":\"jane@example.com\"}")
                .getAsJsonObject();

        Map<String, Object> map = JiraUserTransformer.toInlineMap(json);

        assertEquals("abc123", map.get("AccountId"));
        assertEquals("Jane Doe", map.get("DisplayName"));
        assertEquals("jane@example.com", map.get("Email"));
    }

    @Test
    void toInlineMap_nullInput_returnsNull() {
        assertNull(JiraUserTransformer.toInlineMap(null));
    }
}
