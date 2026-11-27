package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraIssueType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraIssueTypeTransformer {

    private JiraIssueTypeTransformer() {
    }

    public static JiraIssueType transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraIssueType t = new JiraIssueType();
        t.id = JsonHelpers.getStringOrNull(json, "id");
        t.name = JsonHelpers.getStringOrNull(json, "name");
        t.description = JsonHelpers.getStringOrNull(json, "description");
        t.subtask = JsonHelpers.getBoolOrNull(json, "subtask");
        t.iconUrl = JsonHelpers.getStringOrNull(json, "iconUrl");
        return t;
    }

    public static List<JiraIssueType> transformList(JsonArray array) {
        List<JiraIssueType> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraIssueType t = transform(el.getAsJsonObject());
                if (t != null) {
                    out.add(t);
                }
            }
        }
        return out;
    }
}
