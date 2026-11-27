package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support;

/** Two-tier input validation: a static toolkit. Every failure throws {@link IllegalArgumentException}
 *  with FIELD + WHY (+ EXAMPLE where useful) — the signal {@link AbstractJiraArea#audited} maps to an
 *  INPUT-classified response. Validate before any external call. */
public final class Validation {

    private Validation() {
    }

    public static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required. Please provide a value for " + field + ".");
        }
    }

    public static void requireAtLeastOne(String[] names, String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return;
            }
        }
        throw new IllegalArgumentException(
                "At least one of the following is required: " + String.join(", ", names) + ".");
    }

    /** Fails with the exact missing field names — used after schema-driven mandatory-field checking. */
    public static void requireNoMissingMandatoryFields(java.util.List<String> missingFieldNames) {
        if (!missingFieldNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "The following fields are mandatory for this project and issue type but were not "
                            + "provided: " + String.join(", ", missingFieldNames) + ". Please supply them and retry.");
        }
    }
}
