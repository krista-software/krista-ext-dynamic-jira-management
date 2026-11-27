package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldDefinition;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** Maps one entry of {@code GET /field} (the site-wide field catalog). */
public final class JiraFieldDefinitionTransformer {

    private JiraFieldDefinitionTransformer() {
    }

    public static JiraFieldDefinition transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraFieldDefinition f = new JiraFieldDefinition();
        f.id = JsonHelpers.getStringOrNull(json, "id");
        f.key = JsonHelpers.getStringOrNull(json, "key");
        f.name = JsonHelpers.getStringOrNull(json, "name");
        f.custom = JsonHelpers.getBoolOrNull(json, "custom");
        JsonObject schema = JsonHelpers.getObjectOrNull(json, "schema");
        f.dataType = schema != null ? JsonHelpers.getStringOrNull(schema, "type") : null;
        return f;
    }

    public static List<JiraFieldDefinition> transformList(JsonArray array) {
        List<JiraFieldDefinition> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraFieldDefinition f = transform(el.getAsJsonObject());
                if (f != null) {
                    out.add(f);
                }
            }
        }
        return out;
    }
}
