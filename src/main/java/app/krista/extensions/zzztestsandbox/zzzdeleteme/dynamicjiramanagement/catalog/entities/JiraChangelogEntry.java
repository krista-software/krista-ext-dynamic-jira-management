package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One changelog entry (a single edit event) in an issue's history — one or more field-level changes
 *  made at the same time by the same author. References Jira User (author). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Changelog Entry",
        id = "localDomainEntity_023db5c6-41ec-0405-7c8d-e84dbcdeffee",
        primaryKey = "Id",
        supportStore = false,
        description = "One history entry on a Jira issue — the field-level changes made in a single "
                + "edit. References Jira User (author).")
public class JiraChangelogEntry {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Author Display Name", required = false)
    public String authorDisplayName;

    @Searchable
    @Field.Date(name = "Created", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long created;

    @Field.Desc(name = "Changes", type = "[ { Field: Text, FromValue: Text, ToValue: Text } ]", required = false)
    public List<Map<String, Object>> changes;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Author Display Name", authorDisplayName);
        fields.put("Created", created);
        fields.put("Changes", changes);
        return fields;
    }
}
