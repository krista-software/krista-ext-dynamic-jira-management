package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reusable null-safe JSON accessors shared by every DTO(JSON)-&gt;Entity transformer in this extension. */
public final class JsonHelpers {

    private JsonHelpers() {
    }

    public static String getStringOrNull(JsonObject j, String key) {
        return (j == null || !j.has(key) || j.get(key).isJsonNull()) ? null : j.get(key).getAsString();
    }

    public static Boolean getBoolOrNull(JsonObject j, String key) {
        return (j == null || !j.has(key) || j.get(key).isJsonNull()) ? null : j.get(key).getAsBoolean();
    }

    public static JsonObject getObjectOrNull(JsonObject j, String key) {
        return (j == null || !j.has(key) || j.get(key).isJsonNull() || !j.get(key).isJsonObject())
                ? null : j.getAsJsonObject(key);
    }

    public static JsonArray getArrayOrEmpty(JsonObject j, String key) {
        return (j == null || !j.has(key) || j.get(key).isJsonNull() || !j.get(key).isJsonArray())
                ? new JsonArray() : j.getAsJsonArray(key);
    }

    public static String getNestedString(JsonObject j, String obj, String field) {
        JsonObject nested = getObjectOrNull(j, obj);
        return nested == null ? null : getStringOrNull(nested, field);
    }

    /** ISO-8601 date-time (Jira's) -> epoch millis for a Krista @Field.Date. Raw value passes through if unparseable. */
    public static Long getEpochMillisOrNull(JsonObject j, String key) {
        String raw = getStringOrNull(j, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(normalizeOffset(raw)).toEpochMilli();
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /** Jira timestamps are like "2024-01-15T10:30:00.000+0000" — Instant.parse needs "+00:00". */
    private static String normalizeOffset(String raw) {
        if (raw.length() >= 5 && (raw.charAt(raw.length() - 5) == '+' || raw.charAt(raw.length() - 5) == '-')
                && raw.charAt(raw.length() - 3) != ':') {
            return raw.substring(0, raw.length() - 2) + ":" + raw.substring(raw.length() - 2);
        }
        return raw;
    }

    public static List<String> getStringList(JsonObject j, String key) {
        List<String> out = new ArrayList<>();
        for (JsonElement el : getArrayOrEmpty(j, key)) {
            if (el.isJsonPrimitive()) {
                out.add(el.getAsString());
            }
        }
        return out;
    }

    public static Object jsonToJavaValue(JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (el.isJsonPrimitive()) {
            JsonPrimitive p = el.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return p.getAsBoolean();
            }
            if (p.isNumber()) {
                return p.getAsDouble();
            }
            return p.getAsString();
        }
        if (el.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement e : el.getAsJsonArray()) {
                list.add(jsonToJavaValue(e));
            }
            return list;
        }
        if (el.isJsonObject()) {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            for (var entry : el.getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), jsonToJavaValue(entry.getValue()));
            }
            return map;
        }
        return null;
    }

    private static final Gson GSON = new Gson();

    /**
     * Renders any JSON value as compact JSON text. Used instead of {@code app.krista.model.base.FreeForm}
     * for genuinely dynamic output values (custom-field bags, default values of unknown shape) — testing
     * against the real krista-apis 1.0.126 jar confirmed every public FreeForm mutator (`put`, `putAll`,
     * even Gson deserializing straight into a `FreeForm.class` target) throws
     * {@code UnsupportedOperationException} on a normally-constructed instance, so it cannot be populated
     * from extension code for an output value. A JSON-text {@code Text}/{@code Paragraph} field carries
     * the same information losslessly without that restriction.
     */
    public static String toJsonText(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return GSON.toJson(element);
    }
}
