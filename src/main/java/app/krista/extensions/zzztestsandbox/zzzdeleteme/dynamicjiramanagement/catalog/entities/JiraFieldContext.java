package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** A custom field's context — the scope (which projects/issue types) one configuration of that field
 *  applies to. References Jira Field Definition. */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Field Context",
        id = "localDomainEntity_8a5b3d4e-cf6b-8c8d-fa0b-60cb3c4d5e66",
        primaryKey = "Context Id",
        supportStore = false,
        description = "A custom field's configuration context. References Jira Field Definition.")
public class JiraFieldContext {

    @Searchable
    @Field.Text(name = "Context Id", required = false)
    public String contextId;

    @Searchable
    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field(name = "Description", type = "Paragraph", required = false)
    public String description;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Context Id", contextId);
        fields.put("Name", name);
        fields.put("Description", description);
        return fields;
    }
}
