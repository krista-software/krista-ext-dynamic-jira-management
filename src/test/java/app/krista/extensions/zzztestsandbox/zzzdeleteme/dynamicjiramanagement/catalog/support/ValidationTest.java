package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    @Test
    void requireNonBlank_blankValue_throwsWithFieldName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNonBlank("  ", "Project Key Or Id"));
        assertTrue(ex.getMessage().contains("Project Key Or Id"));
    }

    @Test
    void requireNonBlank_nullValue_throws() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requireNonBlank(null, "Summary"));
    }

    @Test
    void requireNonBlank_nonBlankValue_doesNotThrow() {
        assertDoesNotThrow(() -> Validation.requireNonBlank("KE", "Project Key Or Id"));
    }

    @Test
    void requireAtLeastOne_bothBlank_throwsListingBothFields() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validation.requireAtLeastOne(new String[]{"Project Key Or Id", "Issue Id Or Key"}, null, " "));
        assertTrue(ex.getMessage().contains("Project Key Or Id"));
        assertTrue(ex.getMessage().contains("Issue Id Or Key"));
    }

    @Test
    void requireAtLeastOne_oneSupplied_doesNotThrow() {
        assertDoesNotThrow(() ->
                Validation.requireAtLeastOne(new String[]{"Project Key Or Id", "Issue Id Or Key"}, "KE", null));
    }

    @Test
    void requireNoMissingMandatoryFields_namesMissingFieldsInMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNoMissingMandatoryFields(List.of("Environment", "Root Cause")));
        assertTrue(ex.getMessage().contains("Environment"));
        assertTrue(ex.getMessage().contains("Root Cause"));
    }

    @Test
    void requireNoMissingMandatoryFields_emptyList_doesNotThrow() {
        assertDoesNotThrow(() -> Validation.requireNoMissingMandatoryFields(List.of()));
    }
}
