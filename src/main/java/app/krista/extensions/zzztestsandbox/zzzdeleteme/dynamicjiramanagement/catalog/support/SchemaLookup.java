package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldSchema;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraFieldSchemaTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Shared cache-aware schema fetch used by both {@code SchemaArea} (explicit discovery requests) and
 *  {@code IssueArea} (create/update validation) — so Create/Update Issue validate against the SAME
 *  cached schema a caller may have just fetched via Get Field Schema For Issue Type, not a second,
 *  independent copy. */
public final class SchemaLookup {

    private SchemaLookup() {
    }

    public static List<JiraFieldSchema> get(JiraClient client, SchemaCache cache, int ttlMinutes,
                                             String projectKeyOrId, String issueTypeId) throws IOException {
        List<JiraFieldSchema> cached = cache.get(projectKeyOrId, issueTypeId, ttlMinutes);
        if (cached != null) {
            return cached;
        }
        JsonObject result = client.get("/rest/api/3/issue/createmeta/"
                + URLEncoder.encode(projectKeyOrId, StandardCharsets.UTF_8) + "/issuetypes/"
                + URLEncoder.encode(issueTypeId, StandardCharsets.UTF_8));
        List<JiraFieldSchema> fields = JiraFieldSchemaTransformer.transformList(result.getAsJsonArray("fields"));
        cache.put(projectKeyOrId, issueTypeId, fields);
        return fields;
    }

    public static JiraFieldSchema findByName(List<JiraFieldSchema> schema, String displayName) {
        for (JiraFieldSchema s : schema) {
            if (displayName.equalsIgnoreCase(s.name)) {
                return s;
            }
        }
        return null;
    }
}
