package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraProject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraProjectTransformer {

    private JiraProjectTransformer() {
    }

    public static JiraProject transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraProject p = new JiraProject();
        p.id = JsonHelpers.getStringOrNull(json, "id");
        p.key = JsonHelpers.getStringOrNull(json, "key");
        p.name = JsonHelpers.getStringOrNull(json, "name");
        p.projectTypeKey = JsonHelpers.getStringOrNull(json, "projectTypeKey");
        p.style = JsonHelpers.getStringOrNull(json, "style");
        p.lead = JiraUserTransformer.toInlineMap(JsonHelpers.getObjectOrNull(json, "lead"));
        return p;
    }

    public static List<JiraProject> transformList(JsonArray array) {
        List<JiraProject> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraProject p = transform(el.getAsJsonObject());
                if (p != null) {
                    out.add(p);
                }
            }
        }
        return out;
    }
}
