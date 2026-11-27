package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Jira user (reporter, assignee, authenticated user, comment author, assignable-user candidate). */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira User",
        id = "localDomainEntity_7c9a9b2e-6f7a-5a3e-9a2b-1d6e2f9a7c11",
        primaryKey = "Account Id",
        supportStore = false,
        description = "A Jira Cloud user account — an issue's reporter or assignee, the authenticated "
                + "user, a comment's author, or a candidate returned by assignable-user search.")
public class JiraUser {

    @Searchable
    @Field.Text(name = "Account Id", required = false, description = "Jira's immutable account identifier.")
    public String accountId;

    @Searchable
    @ToString
    @Field.Text(name = "Display Name", required = false)
    public String displayName;

    @Searchable
    @Field(name = "Email", type = "Email", required = false)
    public String email;

    @Field.Boolean(name = "Active", required = false)
    public Boolean active;

    @Field.Text(name = "Account Type", required = false, description = "e.g. atlassian, app, customer.")
    public String accountType;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Account Id", accountId);
        fields.put("Display Name", displayName);
        fields.put("Email", email);
        fields.put("Active", active);
        fields.put("Account Type", accountType);
        return fields;
    }
}
