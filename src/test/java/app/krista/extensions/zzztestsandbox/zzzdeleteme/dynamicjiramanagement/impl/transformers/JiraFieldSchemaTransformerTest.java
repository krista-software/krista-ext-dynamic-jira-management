package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldSchema;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraFieldSchemaTransformerTest {

    @Test
    void transform_mandatoryFieldWithAllowedValues() {
        var json = JsonParser.parseString("""
                {
                  "fieldId": "customfield_10050",
                  "name": "Environment",
                  "required": true,
                  "schema": {"type": "option", "custom": "com.atlassian.jira.plugin.system.customfieldtypes:select"},
                  "hasDefaultValue": false,
                  "allowedValues": [
                    {"id": "10001", "value": "Production", "disabled": false},
                    {"id": "10002", "value": "Staging"}
                  ]
                }
                """).getAsJsonObject();

        JiraFieldSchema schema = JiraFieldSchemaTransformer.transform(json);

        assertEquals("customfield_10050", schema.fieldId);
        assertEquals("Environment", schema.name);
        assertTrue(schema.required);
        assertEquals("option", schema.dataType);
        assertTrue(schema.custom);
        assertEquals(2, schema.allowedValues.size());
        assertEquals("10001", schema.allowedValues.get(0).get("Id"));
        assertEquals("Production", schema.allowedValues.get(0).get("Value"));
    }

    @Test
    void transform_optionalSystemField_notCustom() {
        var json = JsonParser.parseString(
                "{\"fieldId\":\"summary\",\"name\":\"Summary\",\"required\":true,"
                        + "\"schema\":{\"type\":\"string\"}}").getAsJsonObject();

        JiraFieldSchema schema = JiraFieldSchemaTransformer.transform(json);

        assertFalse(schema.custom);
        assertEquals("string", schema.dataType);
    }

    @Test
    void transformList_twoDifferentIssueTypes_produceDifferentSchemas() {
        var bugFields = JsonParser.parseString(
                "[{\"fieldId\":\"customfield_10050\",\"name\":\"Environment\",\"required\":true,"
                        + "\"schema\":{\"type\":\"string\"}}]").getAsJsonArray();
        var taskFields = JsonParser.parseString(
                "[{\"fieldId\":\"summary\",\"name\":\"Summary\",\"required\":true,"
                        + "\"schema\":{\"type\":\"string\"}}]").getAsJsonArray();

        List<JiraFieldSchema> bugSchema = JiraFieldSchemaTransformer.transformList(bugFields);
        List<JiraFieldSchema> taskSchema = JiraFieldSchemaTransformer.transformList(taskFields);

        assertEquals("customfield_10050", bugSchema.get(0).fieldId);
        assertEquals("summary", taskSchema.get(0).fieldId);
    }
}
