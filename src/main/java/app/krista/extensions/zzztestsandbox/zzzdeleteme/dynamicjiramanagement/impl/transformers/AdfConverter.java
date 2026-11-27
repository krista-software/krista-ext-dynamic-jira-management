package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Minimal Atlassian Document Format (ADF) &lt;-&gt; plain text conversion. Jira REST API v3 requires ADF
 * for rich-text fields (description, comment body); Krista's {@code RichText}/{@code Text} fields carry
 * plain strings, so every description/comment round-trips through this converter.
 *
 * This is intentionally a plain-text projection, not a full ADF renderer — marks (bold/italic/links)
 * and complex nodes (tables, panels) are flattened to their text content, which is sufficient for
 * display and for re-submitting a user-edited plain-text value.
 */
public final class AdfConverter {

    private AdfConverter() {
    }

    public static String toPlainText(JsonObject adfDocument) {
        if (adfDocument == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendNodeText(adfDocument, sb);
        return sb.toString().strip();
    }

    private static void appendNodeText(JsonObject node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        String type = JsonHelpers.getStringOrNull(node, "type");
        if ("text".equals(type)) {
            sb.append(JsonHelpers.getStringOrNull(node, "text"));
        }
        JsonArray content = JsonHelpers.getArrayOrEmpty(node, "content");
        for (var el : content) {
            if (el.isJsonObject()) {
                appendNodeText(el.getAsJsonObject(), sb);
            }
        }
        if ("paragraph".equals(type) || "heading".equals(type) || "listItem".equals(type)) {
            sb.append('\n');
        }
    }

    /** Wraps a plain-text value in the minimal valid ADF document Jira's create/update/comment APIs require. */
    public static JsonObject fromPlainText(String text) {
        JsonObject doc = new JsonObject();
        doc.addProperty("type", "doc");
        doc.addProperty("version", 1);
        JsonArray content = new JsonArray();
        if (text != null && !text.isBlank()) {
            for (String line : text.split("\n", -1)) {
                JsonObject paragraph = new JsonObject();
                paragraph.addProperty("type", "paragraph");
                JsonArray paragraphContent = new JsonArray();
                if (!line.isEmpty()) {
                    JsonObject textNode = new JsonObject();
                    textNode.addProperty("type", "text");
                    textNode.addProperty("text", line);
                    paragraphContent.add(textNode);
                }
                paragraph.add("content", paragraphContent);
                content.add(paragraph);
            }
        }
        doc.add("content", content);
        return doc;
    }
}
