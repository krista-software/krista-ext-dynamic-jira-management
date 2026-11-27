package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraResolution;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraResolutionTransformer {

    private JiraResolutionTransformer() {
    }

    public static JiraResolution transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraResolution r = new JiraResolution();
        r.id = JsonHelpers.getStringOrNull(json, "id");
        r.name = JsonHelpers.getStringOrNull(json, "name");
        return r;
    }

    public static List<JiraResolution> transformList(JsonArray array) {
        List<JiraResolution> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraResolution r = transform(el.getAsJsonObject());
                if (r != null) {
                    out.add(r);
                }
            }
        }
        return out;
    }
}
