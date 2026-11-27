package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.stores;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extension.util.EntityStore;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraIssue;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search.JiraFieldMappingRegistry;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.search.JiraJqlQueryBuilder;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.AdfConverter;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraIssueTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * {@code EntityStore<JiraIssue>} — the ONE searchable entity in this extension, discovered and bound
 * automatically by HK2 (no parallel search-provider infrastructure). Follows the canonical 12-step
 * search sequence from entity-search-implementation's guide, with JQL (via
 * {@link JiraJqlQueryBuilder}/{@link app.krista.extensions.zzztestsandbox.zzzdeleteme
 * .dynamicjiramanagement.catalog.search.JiraFieldMappingRegistry}) standing in for SOQL/CQL/OData.
 *
 * <p><b>Known pagination limitation:</b> Jira Cloud's current {@code /search/jql} endpoint paginates by
 * opaque {@code nextPageToken}, not a numeric offset — it has no equivalent of SOQL's
 * {@code OFFSET (page-1)*size}. This store honors {@code pageIndex <= 1} (fetch the first page, capped
 * at {@code pageSize}); for any deeper {@code pageIndex} it logs and returns an empty list rather than
 * silently returning page-1 data again under a different page number.
 */
@Service
public class JiraIssueStore implements EntityStore<JiraIssue> {

    private static final Logger LOG = LoggerFactory.getLogger(JiraIssueStore.class);
    private static final String ISSUE_PATH = "/rest/api/3/issue/";

    private final JiraClient client;

    @Inject
    public JiraIssueStore(JiraClient client) {
        this.client = client;
    }

    @Override
    public JiraIssue create(Map<String, Object> entityAttributeFields) throws IOException {
        if (entityAttributeFields == null || entityAttributeFields.isEmpty()) {
            LOG.warn("Jira Issue create: attribute field map is null/empty");
            return new JiraIssue();
        }
        JsonObject fields = new JsonObject();
        putIfPresent(fields, entityAttributeFields, "Summary", "summary");
        JsonObject project = new JsonObject();
        project.addProperty("key", String.valueOf(entityAttributeFields.get("Project Key")));
        fields.add("project", project);
        JsonObject issuetype = new JsonObject();
        issuetype.addProperty("name", String.valueOf(entityAttributeFields.get("Issue Type Name")));
        fields.add("issuetype", issuetype);
        JsonObject body = new JsonObject();
        body.add("fields", fields);
        JsonObject created = client.post("/rest/api/3/issue", body);
        String key = created.has("key") ? created.get("key").getAsString() : null;
        return key != null ? get(key) : new JiraIssue();
    }

    @Override
    public JiraIssue get(String primaryKey) throws IOException {
        if (primaryKey == null || primaryKey.isEmpty()) {
            LOG.warn("Jira Issue get: primary key is null/empty");
            return null;
        }
        JsonObject issue = client.get(ISSUE_PATH + primaryKey);
        return JiraIssueTransformer.transform(issue, client.getBrowseBaseUrl());
    }

    @Override
    public JiraIssue update(JiraIssue entity) throws IOException {
        if (entity == null || entity.key == null) {
            LOG.warn("Jira Issue update: entity or key is null");
            return null;
        }
        JsonObject fields = new JsonObject();
        if (entity.summary != null) {
            fields.addProperty("summary", entity.summary);
        }
        if (entity.description != null) {
            fields.add("description", AdfConverter.fromPlainText(entity.description));
        }
        JsonObject body = new JsonObject();
        body.add("fields", fields);
        client.putNoContent(ISSUE_PATH + entity.key, body);
        return get(entity.key);
    }

    @Override
    public void delete(String primaryKey) throws IOException {
        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("Issue key/id is required for deletion.");
        }
        client.delete(ISSUE_PATH + primaryKey);
    }

    @Override
    public boolean contains(String primaryKey) throws IOException {
        return get(primaryKey) != null;
    }

    @Override
    public List<JiraIssue> search(SearchQuery searchQuery, long pageIndex, int pageSize) throws IOException {
        try {
            int effectivePageSize = pageSize > 0 ? pageSize : 50;
            if (searchQuery == null || searchQuery.getSearchConditions() == null
                    || searchQuery.getSearchConditions().isEmpty()) {
                return defaultPage(effectivePageSize);
            }
            List<SearchCondition> conditions = searchQuery.getSearchConditions();
            List<QueryClause> clauses = searchQuery.getQueryClauses();

            // 4. ID/KEY fast-path
            for (SearchCondition sc : conditions) {
                if (sc.getOperator() == SearchCondition.Operator.equals && sc.getOperand() != null
                        && ("Id".equalsIgnoreCase(sc.getFieldName()) || "Key".equalsIgnoreCase(sc.getFieldName()))) {
                    JiraIssue found = get(String.valueOf(sc.getOperand()).trim());
                    return found != null ? List.of(found) : List.of();
                }
            }

            JiraJqlQueryBuilder.WhereResult where = JiraJqlQueryBuilder.buildWhereClause(conditions);
            if (!where.droppedFields().isEmpty()) {
                LOG.info("Jira Issue search dropped unmapped/invalid condition fields: {}", where.droppedFields());
            }
            String orderBy = JiraJqlQueryBuilder.buildOrderByClause(clauses);

            if (pageIndex > 1) {
                LOG.warn("Jira Issue search requested pageIndex={} — the enhanced JQL search API has no "
                        + "numeric-offset pagination; returning an empty page rather than repeating page 1.",
                        pageIndex);
                return List.of();
            }

            if (where.clause().isEmpty()) {
                return defaultPage(effectivePageSize, orderBy);
            }

            String jql = where.clause() + " ORDER BY " + orderBy;
            return executeJql(jql, effectivePageSize);
        } catch (Exception e) {
            LOG.error("Jira Issue search failed", e);
            return List.of();
        }
    }

    @Override
    public List<JiraIssue> search(List<SearchCondition> conditions, long pageIndex, int pageSize) {
        return List.of(); // legacy overload
    }

    @Override
    public long count(List<SearchCondition> conditions) {
        return 0; // not cheaply available without a second JQL round-trip; stubbed per the guide
    }

    @Override
    public List<String> lookup(Map<String, Object> fields, List<String> fieldNames) {
        return List.of();
    }

    private List<JiraIssue> defaultPage(int pageSize) throws IOException {
        return defaultPage(pageSize, JiraFieldMappingRegistry.DEFAULT_ORDER_BY);
    }

    private List<JiraIssue> defaultPage(int pageSize, String orderBy) throws IOException {
        return executeJql("ORDER BY " + orderBy, pageSize);
    }

    private List<JiraIssue> executeJql(String jql, int maxResults) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("jql", jql);
        body.addProperty("maxResults", maxResults);
        JsonObject result = client.post("/rest/api/3/search/jql", body);
        JsonArray issues = result.has("issues") ? result.getAsJsonArray("issues") : new JsonArray();
        return JiraIssueTransformer.transformList(issues, client.getBrowseBaseUrl());
    }

    private static void putIfPresent(JsonObject target, Map<String, Object> source, String sourceKey, String jsonKey) {
        Object value = source.get(sourceKey);
        if (value != null && !String.valueOf(value).isBlank()) {
            target.addProperty(jsonKey, String.valueOf(value));
        }
    }
}
