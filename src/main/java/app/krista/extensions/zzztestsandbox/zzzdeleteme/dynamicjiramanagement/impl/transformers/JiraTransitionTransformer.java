package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraTransition;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraTransitionTransformer {

    private JiraTransitionTransformer() {
    }

    public static JiraTransition transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraTransition t = new JiraTransition();
        t.id = JsonHelpers.getStringOrNull(json, "id");
        t.name = JsonHelpers.getStringOrNull(json, "name");
        t.toStatusName = JsonHelpers.getNestedString(json, "to", "name");
        t.hasScreen = JsonHelpers.getBoolOrNull(json, "hasScreen");
        return t;
    }

    public static List<JiraTransition> transformList(JsonArray array) {
        List<JiraTransition> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraTransition t = transform(el.getAsJsonObject());
                if (t != null) {
                    out.add(t);
                }
            }
        }
        return out;
    }
}
