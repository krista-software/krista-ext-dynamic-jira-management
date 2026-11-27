package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraChangelogEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JiraChangelogTransformer {

    private JiraChangelogTransformer() {
    }

    public static JiraChangelogEntry transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraChangelogEntry e = new JiraChangelogEntry();
        e.id = JsonHelpers.getStringOrNull(json, "id");
        e.authorDisplayName = JsonHelpers.getNestedString(json, "author", "displayName");
        e.created = JsonHelpers.getEpochMillisOrNull(json, "created");
        e.changes = new ArrayList<>();
        for (var item : JsonHelpers.getArrayOrEmpty(json, "items")) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject i = item.getAsJsonObject();
            Map<String, Object> change = new LinkedHashMap<>();
            change.put("Field", JsonHelpers.getStringOrNull(i, "field"));
            change.put("FromValue", JsonHelpers.getStringOrNull(i, "fromString"));
            change.put("ToValue", JsonHelpers.getStringOrNull(i, "toString"));
            e.changes.add(change);
        }
        return e;
    }

    public static List<JiraChangelogEntry> transformList(JsonArray array) {
        List<JiraChangelogEntry> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraChangelogEntry e = transform(el.getAsJsonObject());
                if (e != null) {
                    out.add(e);
                }
            }
        }
        return out;
    }
}
