package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraPriority;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraPriorityTransformer {

    private JiraPriorityTransformer() {
    }

    public static JiraPriority transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraPriority p = new JiraPriority();
        p.id = JsonHelpers.getStringOrNull(json, "id");
        p.name = JsonHelpers.getStringOrNull(json, "name");
        return p;
    }

    public static List<JiraPriority> transformList(JsonArray array) {
        List<JiraPriority> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraPriority p = transform(el.getAsJsonObject());
                if (p != null) {
                    out.add(p);
                }
            }
        }
        return out;
    }
}
