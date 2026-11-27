package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** A comment on a Jira issue. References Jira User (author). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Comment",
        id = "localDomainEntity_f12ca4b5-30db-f3f4-6b7c-d73cabcdfedd",
        primaryKey = "Id",
        supportStore = false,
        description = "A comment on a Jira issue. References Jira User (author).")
public class JiraComment {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Author Display Name", required = false)
    public String authorDisplayName;

    @Field.Text(name = "Author Account Id", required = false)
    public String authorAccountId;

    @Field(name = "Body Text", type = "RichText", required = false)
    public String bodyText;

    @Searchable
    @Field.Date(name = "Created", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long created;

    @Field.Date(name = "Updated", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long updated;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Author Display Name", authorDisplayName);
        fields.put("Author Account Id", authorAccountId);
        fields.put("Body Text", bodyText);
        fields.put("Created", created);
        fields.put("Updated", updated);
        return fields;
    }
}
