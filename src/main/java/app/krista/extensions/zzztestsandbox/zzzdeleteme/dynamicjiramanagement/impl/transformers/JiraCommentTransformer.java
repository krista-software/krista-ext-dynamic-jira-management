package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraComment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JiraCommentTransformer {

    private JiraCommentTransformer() {
    }

    public static JiraComment transform(JsonObject json) {
        if (json == null) {
            return null;
        }
        JiraComment c = new JiraComment();
        c.id = JsonHelpers.getStringOrNull(json, "id");
        JsonObject author = JsonHelpers.getObjectOrNull(json, "author");
        c.authorDisplayName = author != null ? JsonHelpers.getStringOrNull(author, "displayName") : null;
        c.authorAccountId = author != null ? JsonHelpers.getStringOrNull(author, "accountId") : null;
        c.bodyText = AdfConverter.toPlainText(JsonHelpers.getObjectOrNull(json, "body"));
        c.created = JsonHelpers.getEpochMillisOrNull(json, "created");
        c.updated = JsonHelpers.getEpochMillisOrNull(json, "updated");
        return c;
    }

    public static List<JiraComment> transformList(JsonArray array) {
        List<JiraComment> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraComment c = transform(el.getAsJsonObject());
                if (c != null) {
                    out.add(c);
                }
            }
        }
        return out;
    }
}
