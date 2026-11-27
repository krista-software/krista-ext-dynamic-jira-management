package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JiraFieldMappingRegistryTest {

    @Test
    void forField_knownField_returnsMappingCaseInsensitively() {
        var mapping = JiraFieldMappingRegistry.forField("status name");
        assertEquals("status", mapping.jqlFieldName());
        assertEquals(FieldType.EXACT, mapping.type());

        var upperCase = JiraFieldMappingRegistry.forField("STATUS NAME");
        assertEquals("status", upperCase.jqlFieldName());
    }

    @Test
    void forField_unknownField_returnsNull() {
        assertNull(JiraFieldMappingRegistry.forField("Not A Real Field"));
    }

    @Test
    void forField_nullInput_returnsNull() {
        assertNull(JiraFieldMappingRegistry.forField(null));
    }

    @Test
    void orderByField_knownField_returnsJqlName() {
        assertEquals("updated", JiraFieldMappingRegistry.orderByField("Updated"));
    }

    @Test
    void orderByField_fieldNotSortable_returnsNull() {
        // "Labels" is filterable (forField) but intentionally not in the ORDER-BY map.
        assertNull(JiraFieldMappingRegistry.orderByField("Labels"));
    }

    @Test
    void dateFieldType_isCorrectlyClassified() {
        assertEquals(FieldType.DATE, JiraFieldMappingRegistry.forField("Created").type());
        assertEquals(FieldType.DATE, JiraFieldMappingRegistry.forField("Due Date").type());
    }

    @Test
    void identityFieldType_isCorrectlyClassified() {
        assertEquals(FieldType.IDENTITY, JiraFieldMappingRegistry.forField("Key").type());
    }
}
