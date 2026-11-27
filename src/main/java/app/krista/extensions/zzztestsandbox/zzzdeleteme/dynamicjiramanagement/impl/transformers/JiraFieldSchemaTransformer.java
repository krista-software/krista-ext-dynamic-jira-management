package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldSchema;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps one entry of {@code GET /issue/createmeta/{project}/issuetypes/{issueTypeId}} "fields". */
public final class JiraFieldSchemaTransformer {

    private JiraFieldSchemaTransformer() {
    }

    public static JiraFieldSchema transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraFieldSchema f = new JiraFieldSchema();
        f.fieldId = JsonHelpers.getStringOrNull(json, "fieldId");
        f.name = JsonHelpers.getStringOrNull(json, "name");
        f.required = JsonHelpers.getBoolOrNull(json, "required");
        JsonObject schema = JsonHelpers.getObjectOrNull(json, "schema");
        f.dataType = schema != null ? JsonHelpers.getStringOrNull(schema, "type") : null;
        f.custom = schema != null && schema.has("custom");
        f.hasDefaultValue = JsonHelpers.getBoolOrNull(json, "hasDefaultValue");
        f.defaultValue = json.has("defaultValue") ? JsonHelpers.toJsonText(json.get("defaultValue")) : null;
        f.allowedValues = toAllowedValueMaps(JsonHelpers.getArrayOrEmpty(json, "allowedValues"));
        f.description = JsonHelpers.getStringOrNull(json, "description");
        return f;
    }

    public static List<JiraFieldSchema> transformList(JsonArray array) {
        List<JiraFieldSchema> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraFieldSchema f = transform(el.getAsJsonObject());
                if (f != null) {
                    out.add(f);
                }
            }
        }
        return out;
    }

    private static List<Map<String, Object>> toAllowedValueMaps(JsonArray allowedValues) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var el : allowedValues) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject v = el.getAsJsonObject();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("Id", JsonHelpers.getStringOrNull(v, "id"));
            String value = JsonHelpers.getStringOrNull(v, "value");
            if (value == null) {
                value = JsonHelpers.getStringOrNull(v, "name");
            }
            m.put("Value", value);
            m.put("Disabled", JsonHelpers.getBoolOrNull(v, "disabled"));
            out.add(m);
        }
        return out;
    }
}
