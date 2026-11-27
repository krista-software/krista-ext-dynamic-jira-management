package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** An issue type available for a given project (e.g. Bug, Task, Story) — the second axis, along with
 *  the project, that determines the discovered field schema. */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Issue Type",
        id = "localDomainEntity_5d2f0a1b-9d3e-5f5a-c7b8-3d9e0f1a2b33",
        primaryKey = "Id",
        supportStore = false,
        description = "An issue type create-eligible for a specific Jira project (e.g. Bug, Task, Story).")
public class JiraIssueType {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field(name = "Description", type = "RichText", required = false)
    public String description;

    @Field.Boolean(name = "Subtask", required = false)
    public Boolean subtask;

    @Field.Text(name = "Icon Url", required = false)
    public String iconUrl;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        fields.put("Description", description);
        fields.put("Subtask", subtask);
        fields.put("Icon Url", iconUrl);
        return fields;
    }
}
