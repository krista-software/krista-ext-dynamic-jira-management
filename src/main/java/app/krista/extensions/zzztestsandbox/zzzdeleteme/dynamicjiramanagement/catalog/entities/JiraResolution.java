package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** A site-wide Jira resolution (e.g. Done, Won't Fix, Duplicate). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Resolution",
        id = "localDomainEntity_ce9f7182-0dae-c0c1-3e4f-a40f7a8b9caa",
        primaryKey = "Id",
        supportStore = false,
        description = "A site-wide Jira resolution.")
public class JiraResolution {

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
