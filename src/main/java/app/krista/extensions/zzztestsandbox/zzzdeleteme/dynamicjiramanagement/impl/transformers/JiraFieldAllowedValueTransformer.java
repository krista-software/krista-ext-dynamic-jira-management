package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldAllowedValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraFieldAllowedValueTransformer {

    private JiraFieldAllowedValueTransformer() {
    }

    public static JiraFieldAllowedValue transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraFieldAllowedValue v = new JiraFieldAllowedValue();
        v.id = JsonHelpers.getStringOrNull(json, "id");
        String value = JsonHelpers.getStringOrNull(json, "value");
        v.value = value != null ? value : JsonHelpers.getStringOrNull(json, "name");
        v.disabled = JsonHelpers.getBoolOrNull(json, "disabled");
        return v;
    }

    public static List<JiraFieldAllowedValue> transformList(JsonArray array) {
        List<JiraFieldAllowedValue> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraFieldAllowedValue v = transform(el.getAsJsonObject());
                if (v != null) {
                    out.add(v);
                }
            }
        }
        return out;
    }
}
