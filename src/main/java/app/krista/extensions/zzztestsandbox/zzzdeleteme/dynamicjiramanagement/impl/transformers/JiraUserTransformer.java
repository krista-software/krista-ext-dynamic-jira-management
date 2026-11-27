package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JiraUserTransformer {

    private JiraUserTransformer() {
    }

    public static JiraUser transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraUser u = new JiraUser();
        u.accountId = JsonHelpers.getStringOrNull(json, "accountId");
        u.displayName = JsonHelpers.getStringOrNull(json, "displayName");
        u.email = JsonHelpers.getStringOrNull(json, "emailAddress");
        u.active = JsonHelpers.getBoolOrNull(json, "active");
        u.accountType = JsonHelpers.getStringOrNull(json, "accountType");
        return u;
    }

    public static List<JiraUser> transformList(JsonArray array) {
        List<JiraUser> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraUser u = transform(el.getAsJsonObject());
                if (u != null) {
                    out.add(u);
                }
            }
        }
        return out;
    }

    /** Inline { AccountId, DisplayName, Email } map form — used for JiraIssue.reporter/assignee and
     *  JiraProject.lead (an @Entity's own fields can never reference another @Entity). */
    public static Map<String, Object> toInlineMap(JsonObject json) {
        if (json == null) {
            return null;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("AccountId", JsonHelpers.getStringOrNull(json, "accountId"));
        m.put("DisplayName", JsonHelpers.getStringOrNull(json, "displayName"));
        m.put("Email", JsonHelpers.getStringOrNull(json, "emailAddress"));
        return m;
    }
}
