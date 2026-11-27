package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Jira project — the unit of schema discovery; every project can define its own mandatory/optional
 *  fields, issue types, and workflow. References Jira User (lead). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Project",
        id = "localDomainEntity_3b1e9f0a-8c2d-5e4f-b6a7-2c8d9e0f1a22",
        primaryKey = "Id",
        supportStore = false,
        description = "A Jira Cloud project. References Jira User (Lead).")
public class JiraProject {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Key", required = false, description = "The project key, e.g. KE.")
    public String key;

    @Searchable
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "Project Type Key", required = false, description = "e.g. software, business, service_desk.")
    public String projectTypeKey;

    @Field.Text(name = "Style", required = false, description = "classic or next-gen (team-managed).")
    public String style;

    @Field.Desc(name = "Lead", type = "{ AccountId: Text, DisplayName: Text, Email: Text }", required = false,
            description = "The project lead. References Jira User.")
    public Map<String, Object> lead;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Key", key);
        fields.put("Name", name);
        fields.put("Project Type Key", projectTypeKey);
        fields.put("Style", style);
        fields.put("Lead", lead);
        return fields;
    }
}
