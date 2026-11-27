package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraStatusTransformer {

    private JiraStatusTransformer() {
    }

    public static JiraStatus transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraStatus s = new JiraStatus();
        s.id = JsonHelpers.getStringOrNull(json, "id");
        s.name = JsonHelpers.getStringOrNull(json, "name");
        s.statusCategory = JsonHelpers.getNestedString(json, "statusCategory", "name");
        return s;
    }

    public static List<JiraStatus> transformList(JsonArray array) {
        List<JiraStatus> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraStatus s = transform(el.getAsJsonObject());
                if (s != null) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    /** {@code GET /project/{key}/statuses} nests statuses under each issue type; flatten + de-duplicate by id. */
    public static List<JiraStatus> transformProjectStatuses(JsonArray issueTypeGroups) {
        java.util.LinkedHashMap<String, JiraStatus> byId = new java.util.LinkedHashMap<>();
        if (issueTypeGroups == null) {
            return new ArrayList<>();
        }
        for (var group : issueTypeGroups) {
            if (!group.isJsonObject()) {
                continue;
            }
            for (JiraStatus s : transformList(JsonHelpers.getArrayOrEmpty(group.getAsJsonObject(), "statuses"))) {
                byId.putIfAbsent(s.id, s);
            }
        }
        return new ArrayList<>(byId.values());
    }
}
