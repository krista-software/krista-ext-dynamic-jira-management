package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdfConverterTest {

    @Test
    void toPlainText_extractsTextFromParagraphs() {
        JsonObject adf = JsonParser.parseString(
                "{\"type\":\"doc\",\"version\":1,\"content\":["
                        + "{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Line one\"}]},"
                        + "{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Line two\"}]}"
                        + "]}").getAsJsonObject();

        String text = AdfConverter.toPlainText(adf);

        assertTrue(text.contains("Line one"));
        assertTrue(text.contains("Line two"));
    }

    @Test
    void toPlainText_nullInput_returnsNull() {
        assertNull(AdfConverter.toPlainText(null));
    }

    @Test
    void fromPlainText_producesMinimalValidAdfDocument() {
        JsonObject adf = AdfConverter.fromPlainText("Hello\nWorld");

        assertEquals("doc", adf.get("type").getAsString());
        assertEquals(1, adf.get("version").getAsInt());
        assertEquals(2, adf.getAsJsonArray("content").size());
    }

    @Test
    void fromPlainText_roundTripsBackToOriginalText() {
        String original = "First paragraph\nSecond paragraph";

        JsonObject adf = AdfConverter.fromPlainText(original);
        String roundTripped = AdfConverter.toPlainText(adf);

        assertEquals(original, roundTripped);
    }

    @Test
    void fromPlainText_blankInput_stillProducesValidEmptyDocument() {
        JsonObject adf = AdfConverter.fromPlainText("");

        assertEquals("doc", adf.get("type").getAsString());
        assertEquals(0, adf.getAsJsonArray("content").size());
    }
}
