package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraFieldContextTransformer {

    private JiraFieldContextTransformer() {
    }

    public static JiraFieldContext transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraFieldContext c = new JiraFieldContext();
        c.contextId = JsonHelpers.getStringOrNull(json, "id");
        c.name = JsonHelpers.getStringOrNull(json, "name");
        c.description = JsonHelpers.getStringOrNull(json, "description");
        return c;
    }

    public static List<JiraFieldContext> transformList(JsonArray array) {
        List<JiraFieldContext> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraFieldContext c = transform(el.getAsJsonObject());
                if (c != null) {
                    out.add(c);
                }
            }
        }
        return out;
    }
}
