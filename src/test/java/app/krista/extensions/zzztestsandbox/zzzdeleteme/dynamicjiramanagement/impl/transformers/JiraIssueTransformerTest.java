package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraIssue;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraIssueTransformerTest {

    private static final String ISSUE_JSON = """
            {
              "id": "10042",
              "key": "KE-452",
              "fields": {
                "summary": "Checkout button unresponsive",
                "description": {"type":"doc","version":1,"content":[
                    {"type":"paragraph","content":[{"type":"text","text":"Steps to reproduce"}]}]},
                "project": {"key": "KE"},
                "issuetype": {"name": "Bug"},
                "status": {"name": "In Progress"},
                "priority": {"name": "High"},
                "reporter": {"accountId": "r1", "displayName": "Reporter Name"},
                "assignee": {"accountId": "a1", "displayName": "Assignee Name"},
                "labels": ["frontend", "urgent"],
                "components": [{"name": "Web"}],
                "created": "2024-01-15T10:30:00.000+0000",
                "updated": "2024-02-01T08:00:00.000+0000",
                "duedate": "2024-02-15",
                "resolution": null,
                "customfield_10050": "Chrome 128 on macOS"
              }
            }
            """;

    @Test
    void transform_mapsWellKnownFields() {
        var json = JsonParser.parseString(ISSUE_JSON).getAsJsonObject();

        JiraIssue issue = JiraIssueTransformer.transform(json, "https://example.atlassian.net");

        assertEquals("10042", issue.id);
        assertEquals("KE-452", issue.key);
        assertEquals("https://example.atlassian.net/browse/KE-452", issue.url);
        assertEquals("Checkout button unresponsive", issue.summary);
        assertTrue(issue.description.contains("Steps to reproduce"));
        assertEquals("KE", issue.projectKey);
        assertEquals("Bug", issue.issueTypeName);
        assertEquals("In Progress", issue.statusName);
        assertEquals("High", issue.priorityName);
        assertEquals("r1", issue.reporter.get("AccountId"));
        assertEquals("a1", issue.assignee.get("AccountId"));
        assertEquals(2, issue.labels.size());
        assertEquals("Web", issue.componentNames.get(0));
        assertNull(issue.resolutionName);
    }

    @Test
    void transform_putsUnknownFieldsInCustomFieldsBucket_notInWellKnownFields() {
        var json = JsonParser.parseString(ISSUE_JSON).getAsJsonObject();

        JiraIssue issue = JiraIssueTransformer.transform(json, "https://example.atlassian.net");

        assertTrue(issue.customFields.contains("customfield_10050"));
        assertTrue(issue.customFields.contains("Chrome 128 on macOS"));
        assertTrue(!issue.customFields.contains("Checkout button"),
                "well-known fields must not duplicate into customFields");
    }

    @Test
    void transform_missingFieldsObject_stillReturnsIssueWithEmptyCustomFields() {
        var json = JsonParser.parseString("{\"id\":\"1\",\"key\":\"KE-1\"}").getAsJsonObject();

        JiraIssue issue = JiraIssueTransformer.transform(json, "https://example.atlassian.net");

        assertEquals("KE-1", issue.key);
        assertEquals("{}", issue.customFields);
    }

    @Test
    void transform_nullInput_returnsNull() {
        assertNull(JiraIssueTransformer.transform(null, "https://example.atlassian.net"));
    }
}
