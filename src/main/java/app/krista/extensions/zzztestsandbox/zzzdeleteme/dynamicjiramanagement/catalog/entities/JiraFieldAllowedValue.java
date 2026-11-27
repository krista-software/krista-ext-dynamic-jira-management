package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/** One admin-configured option for a single/multi-select custom field context. References Jira Field
 *  Context. (Also used, in inline-map form, as {@code JiraFieldSchema.allowedValues}.) */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Field Allowed Value",
        id = "localDomainEntity_9b6c4e5f-da7c-9d9e-0b1c-71dc4d5e6f77",
        primaryKey = "Id",
        supportStore = false,
        description = "One admin-configured option value for a select-type custom field context. "
                + "References Jira Field Context.")
public class JiraFieldAllowedValue {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Value", required = false)
    public String value;

    @Field.Boolean(name = "Disabled", required = false)
    public Boolean disabled;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Value", value);
        fields.put("Disabled", disabled);
        return fields;
    }
}
