package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** A site-wide Jira priority level (e.g. Highest, High, Medium, Low, Lowest). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Priority",
        id = "localDomainEntity_bd8e6071-fc9e-bfb0-2d3e-93fe6f7a8b99",
        primaryKey = "Id",
        supportStore = false,
        description = "A site-wide Jira priority level.")
public class JiraPriority {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        return fields;
    }
}
