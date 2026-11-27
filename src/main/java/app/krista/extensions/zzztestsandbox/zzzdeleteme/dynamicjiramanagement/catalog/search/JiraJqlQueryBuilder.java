package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Translates Krista {@link SearchCondition}/{@link QueryClause} lists into a JQL WHERE + ORDER BY
 * clause, via {@link JiraFieldMappingRegistry}. Dispatches per {@link FieldType} (the operator×type
 * matrix) — only the shapes the registry currently maps ({@code IDENTITY}, {@code TEXT}, {@code EXACT},
 * {@code DATE}) are implemented; {@code COMPARABLE} is included for completeness (no numeric Jira Issue
 * field is registered today, but Jira custom number fields could be added to the registry later without
 * touching this class).
 *
 * Injection defense: quoted (TEXT/EXACT/IDENTITY/DATE) values are JQL-escaped (backslash, then double
 * quote); unquoted COMPARABLE operands are regex-validated and dropped (never interpolated) if invalid.
 */
public final class JiraJqlQueryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JiraJqlQueryBuilder.class);
    private static final Pattern NUMERIC = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final DateTimeFormatter JQL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private JiraJqlQueryBuilder() {
    }

    /** Result of building a WHERE clause: the clause text (possibly empty) and any field names this
     *  extension could not map (reported, never silently miscounted as "no filters requested"). */
    public record WhereResult(String clause, List<String> droppedFields) {
    }

    public static WhereResult buildWhereClause(List<SearchCondition> conditions) {
        List<String> dropped = new ArrayList<>();
        if (conditions == null || conditions.isEmpty()) {
            return new WhereResult("", dropped);
        }
        List<String> andClauses = new ArrayList<>();
        // equals/notequals on EXACT/IDENTITY group by field into IN/NOT IN (two ANDed equals on the same
        // field is otherwise an unsatisfiable AND — see entity-search-implementation's grouping rule).
        Map<String, List<String>> eqGroups = new LinkedHashMap<>();
        Map<String, List<String>> neqGroups = new LinkedHashMap<>();
        Map<String, JiraFieldMappingRegistry.FieldMapping> groupMapping = new LinkedHashMap<>();

        for (SearchCondition sc : conditions) {
            if (sc.getOperator() == SearchCondition.Operator.orderby) {
                continue; // handled by buildOrderByClause
            }
            if (sc.getOperand() == null) {
                continue;
            }
            JiraFieldMappingRegistry.FieldMapping mapping = JiraFieldMappingRegistry.forField(sc.getFieldName());
            if (mapping == null) {
                dropped.add(sc.getFieldName());
                LOG.info("Search condition on unmapped field '{}' skipped.", sc.getFieldName());
                continue;
            }
            boolean groupable = (mapping.type() == FieldType.EXACT || mapping.type() == FieldType.IDENTITY)
                    && (sc.getOperator() == SearchCondition.Operator.equals
                        || sc.getOperator() == SearchCondition.Operator.notequals);
            if (groupable) {
                groupMapping.put(mapping.jqlFieldName(), mapping);
                Map<String, List<String>> target =
                        sc.getOperator() == SearchCondition.Operator.equals ? eqGroups : neqGroups;
                target.computeIfAbsent(mapping.jqlFieldName(), k -> new ArrayList<>())
                        .add(String.valueOf(sc.getOperand()));
                continue;
            }
            String clause = clauseFor(mapping, sc.getOperator(), sc.getOperand());
            if (clause == null) {
                dropped.add(sc.getFieldName());
            } else {
                andClauses.add(clause);
            }
        }
        for (var entry : eqGroups.entrySet()) {
            andClauses.add(inClause(entry.getKey(), entry.getValue(), false));
        }
        for (var entry : neqGroups.entrySet()) {
            andClauses.add(inClause(entry.getKey(), entry.getValue(), true));
        }
        return new WhereResult(String.join(" AND ", andClauses), dropped);
    }

    public static String buildOrderByClause(List<QueryClause> clauses) {
        if (clauses != null) {
            for (QueryClause qc : clauses) {
                if (qc.getOperator() != QueryClause.Operator.orderby) {
                    continue;
                }
                String field = JiraFieldMappingRegistry.orderByField(qc.getFieldName());
                if (field == null) {
                    LOG.info("ORDER BY on unmapped field '{}' ignored; using default sort.", qc.getFieldName());
                    continue;
                }
                return field + " " + direction(qc.getOperand());
            }
        }
        return JiraFieldMappingRegistry.DEFAULT_ORDER_BY;
    }

    private static String direction(Object operand) {
        String v = operand == null ? "" : String.valueOf(operand).toLowerCase(Locale.ROOT);
        return v.startsWith("asc") ? "ASC" : "DESC";
    }

    private static String clauseFor(JiraFieldMappingRegistry.FieldMapping mapping,
                                     SearchCondition.Operator op, Object operand) {
        return switch (mapping.type()) {
            case TEXT -> textClause(mapping.jqlFieldName(), op, String.valueOf(operand));
            case EXACT, IDENTITY -> exactClause(mapping.jqlFieldName(), op, String.valueOf(operand));
            case COMPARABLE -> comparableClause(mapping.jqlFieldName(), op, String.valueOf(operand));
            case DATE -> dateClause(mapping.jqlFieldName(), op, operand);
        };
    }

    private static String textClause(String field, SearchCondition.Operator op, String value) {
        String escaped = escape(value);
        return switch (op) {
            case contains, equals -> field + " ~ \"" + escaped + "\"";
            case startswith -> field + " ~ \"" + escaped + "*\"";
            case endswith -> field + " ~ \"*" + escaped + "\"";
            case notequals -> field + " !~ \"" + escaped + "\"";
            default -> null; // gt/gte/lt/lte not meaningful for free text
        };
    }

    private static String exactClause(String field, SearchCondition.Operator op, String value) {
        String escaped = escape(value);
        return switch (op) {
            case equals -> field + " = \"" + escaped + "\"";
            case notequals -> field + " != \"" + escaped + "\"";
            default -> null; // gt/gte/lt/lte/contains not meaningful for an exact-match field
        };
    }

    private static String comparableClause(String field, SearchCondition.Operator op, String value) {
        if (!NUMERIC.matcher(value.trim()).matches()) {
            LOG.info("Comparable condition on '{}' dropped — '{}' is not a valid number.", field, value);
            return null;
        }
        String v = value.trim();
        return switch (op) {
            case equals -> field + " = " + v;
            case notequals -> field + " != " + v;
            case gt -> field + " > " + v;
            case gte -> field + " >= " + v;
            case lt -> field + " < " + v;
            case lte -> field + " <= " + v;
            default -> null;
        };
    }

    private static String dateClause(String field, SearchCondition.Operator op, Object operand) {
        Long epochMillis = toEpochMillis(operand);
        if (epochMillis == null) {
            LOG.info("Date condition on '{}' dropped — '{}' is not a valid epoch-millis value.", field, operand);
            return null;
        }
        String literal = "\"" + JQL_DATE_FORMAT.format(Instant.ofEpochMilli(epochMillis)) + "\"";
        return switch (op) {
            case equals -> field + " = " + literal;
            case notequals -> field + " != " + literal;
            case gt -> field + " > " + literal;
            case gte -> field + " >= " + literal;
            case lt -> field + " < " + literal;
            case lte -> field + " <= " + literal;
            default -> null;
        };
    }

    private static Long toEpochMillis(Object operand) {
        try {
            if (operand instanceof Number n) {
                return n.longValue();
            }
            return (long) Double.parseDouble(String.valueOf(operand).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String inClause(String field, List<String> values, boolean negate) {
        StringBuilder sb = new StringBuilder(field).append(negate ? " NOT IN (" : " IN (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('"').append(escape(values.get(i))).append('"');
        }
        return sb.append(')').toString();
    }

    /** JQL quoted-literal escaping: backslash first, then double quote. */
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
