package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** One workflow transition available from an issue's CURRENT status — the only legal way to change
 *  status (never a direct field write). References Jira Status (To Status Name). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Transition",
        id = "localDomainEntity_df0a8293-1ebf-d1d2-4f5a-b51a8b9cadbb",
        primaryKey = "Id",
        supportStore = false,
        description = "A workflow transition available from an issue's current status. References Jira Status.")
public class JiraTransition {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "To Status Name", required = false)
    public String toStatusName;

    @Field.Boolean(name = "Has Screen", required = false,
            description = "True if executing this transition requires additional fields.")
    public Boolean hasScreen;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        fields.put("To Status Name", toStatusName);
        fields.put("Has Screen", hasScreen);
        return fields;
    }
}
