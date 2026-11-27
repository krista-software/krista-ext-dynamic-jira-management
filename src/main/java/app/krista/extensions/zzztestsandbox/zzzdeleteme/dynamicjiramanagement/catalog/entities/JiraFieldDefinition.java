package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** One entry from Jira's global field catalog (system + custom), independent of any one project. */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Field Definition",
        id = "localDomainEntity_7f4a2c3d-be5a-7b7c-e9da-5fba2b3c4d55",
        primaryKey = "Id",
        supportStore = false,
        description = "One entry from Jira's site-wide field catalog (system or custom), independent of "
                + "any single project's create-metadata scoping.")
public class JiraFieldDefinition {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Field.Text(name = "Key", required = false)
    public String key;

    @Searchable
    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Searchable
    @Field.Boolean(name = "Custom", required = false)
    public Boolean custom;

    @Field.Text(name = "Data Type", required = false)
    public String dataType;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Key", key);
        fields.put("Name", name);
        fields.put("Custom", custom);
        fields.put("Data Type", dataType);
        return fields;
    }
}
