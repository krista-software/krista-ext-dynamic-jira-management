package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * The discovered field metadata for ONE field, scoped to a specific project + issue type (the create
 * metadata contract). This is the core of "dynamic schema discovery": mandatory/optional, data type,
 * default, allowed values, and whether it is a custom field — all read from Jira at request time, never
 * hardcoded per project.
 */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
@Entity(name = "Jira Field Schema",
        id = "localDomainEntity_6e3f1b2c-ad4f-6a6b-d8c9-4eaf1a2b3c44",
        primaryKey = "Field Id",
        supportStore = false,
        description = "Discovered field metadata for one field on a specific project + issue type's "
                + "create screen — mandatory/optional status, data type, default, and allowed values.")
public class JiraFieldSchema {

    @Searchable
    @Field.Text(name = "Field Id", required = false, description = "e.g. summary, priority, customfield_10050.")
    public String fieldId;

    @Searchable
    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Searchable
    @Field.Boolean(name = "Required", required = false, description = "True = mandatory for this project + issue type.")
    public Boolean required;

    @Field.Text(name = "Data Type", required = false, description = "e.g. string, number, array, option, user.")
    public String dataType;

    @Searchable
    @Field.Boolean(name = "Custom", required = false)
    public Boolean custom;

    @Field.Boolean(name = "Has Default Value", required = false)
    public Boolean hasDefaultValue;

    // Shape depends on Data Type; carried as its JSON text representation (krista-apis 1.0.126's
    // FreeForm has no public, populatable mutator for output construction — see JsonHelpers#toJsonText).
    @Field(name = "Default Value", type = "Text", required = false)
    public String defaultValue;

    @Field.Desc(name = "Allowed Values", type = "[ { Id: Text, Value: Text, Disabled: Boolean } ]", required = false,
            description = "The admin-configured option list, when the field is single/multi-select.")
    public List<Map<String, Object>> allowedValues;

    @Field(name = "Description", type = "Paragraph", required = false)
    public String description;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Field Id", fieldId);
        fields.put("Name", name);
        fields.put("Required", required);
        fields.put("Data Type", dataType);
        fields.put("Custom", custom);
        fields.put("Has Default Value", hasDefaultValue);
        fields.put("Default Value", defaultValue);
        fields.put("Allowed Values", allowedValues);
        fields.put("Description", description);
        return fields;
    }
}
