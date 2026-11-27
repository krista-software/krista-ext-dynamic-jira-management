package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraIssue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Maps {@code GET/POST /issue} and {@code /search/jql} issue objects. Every well-known Jira field is
 *  promoted to a typed entity field; everything else (customfield_XXXXX and any field this build does
 *  not special-case) is carried in {@link JiraIssue#customFields} as a JSON string — genuinely dynamic
 *  per project + issue type (see JsonHelpers#toJsonText for why this is JSON text, not FreeForm). */
public final class JiraIssueTransformer {

    private static final Gson GSON = new Gson();

    private static final Set<String> WELL_KNOWN_FIELDS = Set.of(
            "summary", "description", "project", "issuetype", "status", "priority", "reporter",
            "assignee", "labels", "components", "created", "updated", "duedate", "resolution");

    private JiraIssueTransformer() {
    }

    public static JiraIssue transform(JsonObject issueJson, String browseBaseUrl) {
        if (issueJson == null) {
            return null;
        }
        JiraIssue issue = new JiraIssue();
        issue.id = JsonHelpers.getStringOrNull(issueJson, "id");
        issue.key = JsonHelpers.getStringOrNull(issueJson, "key");
        issue.url = browseBaseUrl != null && issue.key != null ? browseBaseUrl + "/browse/" + issue.key : null;

        JsonObject fields = JsonHelpers.getObjectOrNull(issueJson, "fields");
        if (fields == null) {
            issue.customFields = "{}";
            return issue;
        }

        issue.summary = JsonHelpers.getStringOrNull(fields, "summary");
        issue.description = AdfConverter.toPlainText(JsonHelpers.getObjectOrNull(fields, "description"));
        issue.projectKey = JsonHelpers.getNestedString(fields, "project", "key");
        issue.issueTypeName = JsonHelpers.getNestedString(fields, "issuetype", "name");
        issue.statusName = JsonHelpers.getNestedString(fields, "status", "name");
        issue.priorityName = JsonHelpers.getNestedString(fields, "priority", "name");
        issue.reporter = JiraUserTransformer.toInlineMap(JsonHelpers.getObjectOrNull(fields, "reporter"));
        issue.assignee = JiraUserTransformer.toInlineMap(JsonHelpers.getObjectOrNull(fields, "assignee"));
        issue.labels = JsonHelpers.getStringList(fields, "labels");
        issue.componentNames = componentNames(JsonHelpers.getArrayOrEmpty(fields, "components"));
        issue.created = JsonHelpers.getEpochMillisOrNull(fields, "created");
        issue.updated = JsonHelpers.getEpochMillisOrNull(fields, "updated");
        issue.dueDate = JsonHelpers.getEpochMillisOrNull(fields, "duedate");
        issue.resolutionName = JsonHelpers.getNestedString(fields, "resolution", "name");

        Map<String, Object> customFieldValues = new LinkedHashMap<>();
        for (var entry : fields.entrySet()) {
            if (!WELL_KNOWN_FIELDS.contains(entry.getKey())) {
                customFieldValues.put(entry.getKey(), JsonHelpers.jsonToJavaValue(entry.getValue()));
            }
        }
        issue.customFields = GSON.toJson(customFieldValues);
        return issue;
    }

    public static List<JiraIssue> transformList(JsonArray array, String browseBaseUrl) {
        List<JiraIssue> out = new ArrayList<>();
        if (array == null) {
            return out;
        }
        for (var el : array) {
            if (el.isJsonObject()) {
                JiraIssue issue = transform(el.getAsJsonObject(), browseBaseUrl);
                if (issue != null) {
                    out.add(issue);
                }
            }
        }
        return out;
    }

    private static List<String> componentNames(JsonArray components) {
        List<String> out = new ArrayList<>();
        for (var el : components) {
            if (el.isJsonObject()) {
                String name = JsonHelpers.getStringOrNull(el.getAsJsonObject(), "name");
                if (name != null) {
                    out.add(name);
                }
            }
        }
        return out;
    }
}
