package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraJqlQueryBuilderTest {

    @Test
    void buildWhereClause_exactEquals_quotesValue() {
        // EXACT/IDENTITY equals conditions are grouped into IN(...) uniformly — even a single value —
        // so there is one code path instead of a separate "=" vs "IN" branch (see the repeated-equals
        // grouping test below for the multi-value case this generalizes).
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.equals, "In Progress", "Status Name"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("status IN (\"In Progress\")", result.clause());
        assertTrue(result.droppedFields().isEmpty());
    }

    @Test
    void buildWhereClause_repeatedEqualsOnSameField_groupsIntoIn() {
        var conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "Bug", "Issue Type Name"),
                new SearchCondition(SearchCondition.Operator.equals, "Task", "Issue Type Name"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("issuetype IN (\"Bug\", \"Task\")", result.clause());
    }

    @Test
    void buildWhereClause_notEqualsGroup_buildsNotIn() {
        var conditions = List.of(
                new SearchCondition(SearchCondition.Operator.notequals, "Done", "Status Name"),
                new SearchCondition(SearchCondition.Operator.notequals, "Closed", "Status Name"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("status NOT IN (\"Done\", \"Closed\")", result.clause());
    }

    @Test
    void buildWhereClause_textContains_usesTildeOperator() {
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.contains, "checkout", "Summary"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("summary ~ \"checkout\"", result.clause());
    }

    @Test
    void buildWhereClause_textStartswith_appendsWildcard() {
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.startswith, "check", "Summary"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("summary ~ \"check*\"", result.clause());
    }

    @Test
    void buildWhereClause_unknownField_isDroppedAndReported() {
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.equals, "x", "Not A Real Field"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("", result.clause());
        assertEquals(List.of("Not A Real Field"), result.droppedFields());
    }

    @Test
    void buildWhereClause_dateCondition_convertsEpochMillisToJqlLiteral() {
        long epochMillis = 1705314600000L; // 2024-01-15T10:30:00Z
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.gte, epochMillis, "Created"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("created >= \"2024-01-15 10:30\"", result.clause());
    }

    @Test
    void buildWhereClause_quotedValueEscapesQuotesAndBackslashes() {
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.equals, "a \"quoted\" value", "Status Name"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("status IN (\"a \\\"quoted\\\" value\")", result.clause());
    }

    @Test
    void buildWhereClause_orderbyConditionIsSkipped() {
        var conditions = List.of(new SearchCondition(SearchCondition.Operator.orderby, "desc", "Updated"));

        var result = JiraJqlQueryBuilder.buildWhereClause(conditions);

        assertEquals("", result.clause());
        assertTrue(result.droppedFields().isEmpty());
    }

    @Test
    void buildWhereClause_nullOrEmptyConditions_returnsEmptyClause() {
        assertEquals("", JiraJqlQueryBuilder.buildWhereClause(null).clause());
        assertEquals("", JiraJqlQueryBuilder.buildWhereClause(List.of()).clause());
    }

    @Test
    void buildOrderByClause_mappedField_usesDirectionFromOperand() {
        var clauses = List.of(new QueryClause(QueryClause.Operator.orderby, "asc", "Created"));

        assertEquals("created ASC", JiraJqlQueryBuilder.buildOrderByClause(clauses));
    }

    @Test
    void buildOrderByClause_defaultsToDescendingWhenDirectionMissing() {
        var clauses = List.of(new QueryClause(QueryClause.Operator.orderby, null, "Created"));

        assertEquals("created DESC", JiraJqlQueryBuilder.buildOrderByClause(clauses));
    }

    @Test
    void buildOrderByClause_unmappedField_fallsBackToDefaultSort() {
        var clauses = List.of(new QueryClause(QueryClause.Operator.orderby, "asc", "Not A Real Field"));

        assertEquals(JiraFieldMappingRegistry.DEFAULT_ORDER_BY, JiraJqlQueryBuilder.buildOrderByClause(clauses));
    }

    @Test
    void buildOrderByClause_noClauses_returnsDefaultSort() {
        assertEquals(JiraFieldMappingRegistry.DEFAULT_ORDER_BY, JiraJqlQueryBuilder.buildOrderByClause(null));
        assertEquals(JiraFieldMappingRegistry.DEFAULT_ORDER_BY, JiraJqlQueryBuilder.buildOrderByClause(List.of()));
    }
}
