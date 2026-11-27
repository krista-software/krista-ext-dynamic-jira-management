package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Jira issue/ticket — the central domain object. {@code supportStore = true}: this is the one
 * searchable entity in this extension (backed by {@link app.krista.extensions.zzztestsandbox
 * .zzzdeleteme.dynamicjiramanagement.catalog.stores.JiraIssueStore}, which translates Krista search
 * conditions into JQL). References Jira User (Reporter, Assignee).
 */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Issue",
        id = "localDomainEntity_e01b93a4-2fca-e2e3-5a6b-c62b9cadbecc",
        primaryKey = "Id",
        supportStore = true,
        description = "A Jira issue/ticket, with its discovered custom fields carried in Custom Fields. "
                + "References Jira User (Reporter, Assignee).")
public class JiraIssue {

    @Searchable
    @Field.Text(name = "Id", required = false, description = "Jira's immutable numeric issue id.")
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Key", required = false, description = "The display issue key, e.g. KE-123.")
    public String key;

    @Field.Text(name = "Url", required = false, description = "Direct browser link to this issue.")
    public String url;

    @Searchable
    @Field.Text(name = "Summary", required = false)
    public String summary;

    @Field(name = "Description", type = "RichText", required = false)
    public String description;

    @Searchable
    @Field.Text(name = "Project Key", required = false)
    public String projectKey;

    @Searchable
    @Field.Text(name = "Issue Type Name", required = false)
    public String issueTypeName;

    @Searchable
    @Field.Text(name = "Status Name", required = false)
    public String statusName;

    @Searchable
    @Field.Text(name = "Priority Name", required = false)
    public String priorityName;

    @Searchable
    @Field.Desc(name = "Reporter", type = "{ AccountId: Text, DisplayName: Text, Email: Text }", required = false,
            description = "References Jira User.")
    public Map<String, Object> reporter;

    @Searchable
    @Field.Desc(name = "Assignee", type = "{ AccountId: Text, DisplayName: Text, Email: Text }", required = false,
            description = "References Jira User.")
    public Map<String, Object> assignee;

    @Searchable
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public List<String> labels;

    @Searchable
    @Field.Desc(name = "Component Names", type = "[ Text ]", required = false)
    public List<String> componentNames;

    @Searchable
    @Field.Date(name = "Created", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long created;

    @Searchable
    @Field.Date(name = "Updated", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long updated;

    @Searchable
    @Field.Date(name = "Due Date", required = false, includeTimeOfDay = false,
            allowPast = true, allowToday = true, allowFuture = true)
    public Long dueDate;

    @Searchable
    @Field.Text(name = "Resolution Name", required = false)
    public String resolutionName;

    // Every project-specific custom field discovered for this issue's project + issue type, keyed by
    // field id — shape varies per field, so the bag is carried as a JSON string (krista-apis 1.0.126's
    // FreeForm has no public, populatable mutator for output construction — see JsonHelpers#toJsonText).
    @Field(name = "Custom Fields", type = "Paragraph", required = false)
    public String customFields;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Key", key);
        fields.put("Url", url);
        fields.put("Summary", summary);
        fields.put("Description", description);
        fields.put("Project Key", projectKey);
        fields.put("Issue Type Name", issueTypeName);
        fields.put("Status Name", statusName);
        fields.put("Priority Name", priorityName);
        fields.put("Reporter", reporter);
        fields.put("Assignee", assignee);
        fields.put("Labels", labels);
        fields.put("Component Names", componentNames);
        fields.put("Created", created);
        fields.put("Updated", updated);
        fields.put("Due Date", dueDate);
        fields.put("Resolution Name", resolutionName);
        fields.put("Custom Fields", customFields);
        return fields;
    }
}
