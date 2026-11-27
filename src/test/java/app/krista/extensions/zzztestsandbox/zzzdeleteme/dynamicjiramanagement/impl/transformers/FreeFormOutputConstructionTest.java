package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import app.krista.model.base.FreeForm;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Documents a real, verified krista-apis 1.0.126 limitation that shaped this extension's design:
 * {@code app.krista.model.base.FreeForm} cannot be populated from extension code for an OUTPUT value —
 * every public mutator on a normally-constructed instance throws {@link UnsupportedOperationException}.
 * This is why {@link JiraIssueTransformer} (Custom Fields) and {@link JiraFieldSchemaTransformer}
 * (Default Value) render dynamic data as JSON text via {@link JsonHelpers#toJsonText} instead of
 * FreeForm, even though FreeForm is field-types.md's documented type for "genuinely unstructured" data.
 *
 * If a future krista-apis upgrade makes FreeForm populatable, this test will start failing — that is
 * the intended signal to revisit the JSON-text workaround.
 */
class FreeFormOutputConstructionTest {

    @Test
    void put_onNormallyConstructedInstance_throwsUnsupportedOperationException() {
        FreeForm freeForm = new FreeForm();

        assertThrows(UnsupportedOperationException.class, () -> freeForm.put("key", "value"));
    }

    @Test
    void putAll_onNormallyConstructedInstance_throwsUnsupportedOperationException() {
        FreeForm freeForm = new FreeForm();

        assertThrows(UnsupportedOperationException.class, () -> freeForm.putAll(Map.of("key", "value")));
    }
}
