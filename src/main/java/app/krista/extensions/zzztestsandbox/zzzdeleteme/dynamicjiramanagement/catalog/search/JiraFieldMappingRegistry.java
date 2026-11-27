package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search;

import java.util.Locale;
import java.util.Map;

/**
 * The single source of truth mapping Jira Issue's {@code @Field(name=...)} display strings (what
 * arrives in {@code SearchCondition.getFieldName()}) to the backend JQL field name + {@link FieldType}.
 * Null-returning lookups; unknown fields are skipped by the caller, never an error (see
 * entity-search-implementation's canonical registry pattern). Case-insensitive.
 */
public final class JiraFieldMappingRegistry {

    private static final Map<String, FieldMapping> FIELD_MAP = Map.ofEntries(
            Map.entry("key", new FieldMapping("key", FieldType.IDENTITY)),
            Map.entry("summary", new FieldMapping("summary", FieldType.TEXT)),
            Map.entry("project key", new FieldMapping("project", FieldType.EXACT)),
            Map.entry("issue type name", new FieldMapping("issuetype", FieldType.EXACT)),
            Map.entry("status name", new FieldMapping("status", FieldType.EXACT)),
            Map.entry("priority name", new FieldMapping("priority", FieldType.EXACT)),
            Map.entry("labels", new FieldMapping("labels", FieldType.EXACT)),
            Map.entry("component names", new FieldMapping("component", FieldType.EXACT)),
            Map.entry("created", new FieldMapping("created", FieldType.DATE)),
            Map.entry("updated", new FieldMapping("updated", FieldType.DATE)),
            Map.entry("due date", new FieldMapping("duedate", FieldType.DATE)),
            Map.entry("resolution name", new FieldMapping("resolution", FieldType.EXACT))
    );

    private static final Map<String, String> ORDER_BY_MAP = Map.of(
            "key", "key",
            "summary", "summary",
            "project key", "project",
            "status name", "status",
            "priority name", "priority",
            "created", "created",
            "updated", "updated",
            "due date", "duedate"
    );

    public static final String DEFAULT_ORDER_BY = "updated DESC";

    private JiraFieldMappingRegistry() {
    }

    public static FieldMapping forField(String displayFieldName) {
        if (displayFieldName == null) {
            return null;
        }
        return FIELD_MAP.get(displayFieldName.toLowerCase(Locale.ROOT).trim());
    }

    public static String orderByField(String displayFieldName) {
        if (displayFieldName == null) {
            return null;
        }
        return ORDER_BY_MAP.get(displayFieldName.toLowerCase(Locale.ROOT).trim());
    }

    public record FieldMapping(String jqlFieldName, FieldType type) {
    }
}
