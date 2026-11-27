package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.AbstractJiraArea;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.ExtensionResponses;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.TelemetryHelper;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraIssueTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JQL search with dynamic filter combinations (requirement section 7E). Builds JQL directly from the
 * typed filter inputs below (a separate, simpler path from {@link
 * app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.stores.JiraIssueStore},
 * which serves Krista's generic entity-search/KME path against the same Jira Issue entity).
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class SearchArea extends AbstractJiraArea {

    private static final DateTimeFormatter JQL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    @Inject
    public SearchArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_1c2d3e4f-5a6b-4c12-4f25-9a0b1c2d3e21",
            name = "Search Tickets",
            description = "Search Jira issues across projects with combinable filters, sorting, and "
                    + "pagination (e.g. 'unresolved bugs in project KE', 'high-priority tickets assigned "
                    + "to me', 'tickets created in the last seven days').",
            area = "Search",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issues", type = "[ Entity(Jira Issue) ]", required = false)
    @Field(name = "Count", type = "Number", required = false,
            attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "Next Page Token", required = false)
    public ExtensionResponse searchTickets(
            @Field.Text(name = "Project Key Or Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId,
            @Field.Text(name = "Issue Key", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueKey,
            @Field.Text(name = "Status", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String status,
            @Field.Boolean(name = "Unresolved Only", required = false,
                    attributes = {}, options = {}) Boolean unresolvedOnly,
            @Field.Text(name = "Reporter Account Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String reporterAccountId,
            @Field.Boolean(name = "Reported By Me", required = false, attributes = {}, options = {}) Boolean reportedByMe,
            @Field.Text(name = "Assignee Account Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String assigneeAccountId,
            @Field.Boolean(name = "Assigned To Me", required = false, attributes = {}, options = {}) Boolean assignedToMe,
            @Field.Text(name = "Priority Name", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String priorityName,
            @Field.Text(name = "Issue Type Name", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueTypeName,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Component Names", type = "[ Text ]", required = false) List<String> componentNames,
            @Field.Date(name = "Created After", required = false, includeTimeOfDay = true,
                    allowPast = true, allowToday = true, allowFuture = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) Long createdAfter,
            @Field.Date(name = "Updated After", required = false, includeTimeOfDay = true,
                    allowPast = true, allowToday = true, allowFuture = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) Long updatedAfter,
            @Field.Date(name = "Due Before", required = false, includeTimeOfDay = false,
                    allowPast = true, allowToday = true, allowFuture = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) Long dueBefore,
            @Field.Text(name = "Resolution Name", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String resolutionName,
            @Field.Text(name = "Free Text Search", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String freeTextSearch,
            @Field.PickOne(name = "Sort By", values = {"Updated", "Created", "Priority", "Due Date"}, required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String sortBy,
            @Field.PickOne(name = "Sort Direction", values = {"Descending", "Ascending"}, required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String sortDirection,
            @Field(name = "Max Results", type = "Number", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double maxResults,
            @Field.Text(name = "Page Token", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String pageToken,
            @Field(name = "Raw JQL", type = "Paragraph", required = false, attributes = {}, options = {}) String rawJql)
            throws MustAuthorizeException {
        return audited("SearchTickets", () -> {
            if (rawJql != null && !rawJql.isBlank()) {
                JsonObject rawBody = new JsonObject();
                rawBody.addProperty("jql", rawJql);
                rawBody.addProperty("maxResults", maxResults != null ? maxResults.intValue() : 50);
                if (pageToken != null && !pageToken.isBlank()) {
                    rawBody.addProperty("nextPageToken", pageToken);
                }
                JsonObject rawResult = client.post("/rest/api/3/search/jql", rawBody);
                var rawIssues = JiraIssueTransformer.transformList(
                        rawResult.getAsJsonArray("issues"), client.getBrowseBaseUrl());
                Map<String, Object> rawOut = new LinkedHashMap<>();
                rawOut.put("Issues", rawIssues);
                rawOut.put("Count", (double) rawIssues.size());
                rawOut.put("Next Page Token", rawResult.has("nextPageToken")
                        ? rawResult.get("nextPageToken").getAsString() : null);
                return ExtensionResponses.ok(rawOut);
            }
            List<String> clauses = new ArrayList<>();
            if (projectKeyOrId != null && !projectKeyOrId.isBlank()) {
                clauses.add("project = " + quote(projectKeyOrId));
            }
            if (issueKey != null && !issueKey.isBlank()) {
                clauses.add("key = " + quote(issueKey));
            }
            if (status != null && !status.isBlank()) {
                clauses.add("status = " + quote(status));
            }
            if (Boolean.TRUE.equals(unresolvedOnly)) {
                clauses.add("resolution = Unresolved");
            }
            if (Boolean.TRUE.equals(reportedByMe)) {
                clauses.add("reporter = currentUser()");
            } else if (reporterAccountId != null && !reporterAccountId.isBlank()) {
                clauses.add("reporter = " + quote(reporterAccountId));
            }
            if (Boolean.TRUE.equals(assignedToMe)) {
                clauses.add("assignee = currentUser()");
            } else if (assigneeAccountId != null && !assigneeAccountId.isBlank()) {
                clauses.add("assignee = " + quote(assigneeAccountId));
            }
            if (priorityName != null && !priorityName.isBlank()) {
                clauses.add("priority = " + quote(priorityName));
            }
            if (issueTypeName != null && !issueTypeName.isBlank()) {
                clauses.add("issuetype = " + quote(issueTypeName));
            }
            if (labels != null && !labels.isEmpty()) {
                clauses.add("labels IN (" + joinQuoted(labels) + ")");
            }
            if (componentNames != null && !componentNames.isEmpty()) {
                clauses.add("component IN (" + joinQuoted(componentNames) + ")");
            }
            if (createdAfter != null) {
                clauses.add("created >= " + quote(JQL_DATE_FORMAT.format(Instant.ofEpochMilli(createdAfter))));
            }
            if (updatedAfter != null) {
                clauses.add("updated >= " + quote(JQL_DATE_FORMAT.format(Instant.ofEpochMilli(updatedAfter))));
            }
            if (dueBefore != null) {
                clauses.add("duedate <= " + quote(JQL_DATE_FORMAT.format(Instant.ofEpochMilli(dueBefore))));
            }
            if (resolutionName != null && !resolutionName.isBlank()) {
                clauses.add("resolution = " + quote(resolutionName));
            }
            if (freeTextSearch != null && !freeTextSearch.isBlank()) {
                clauses.add("text ~ " + quote(freeTextSearch));
            }

            String sortField = switch (sortBy == null ? "Updated" : sortBy) {
                case "Created" -> "created";
                case "Priority" -> "priority";
                case "Due Date" -> "duedate";
                default -> "updated";
            };
            String direction = "Ascending".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";

            String jql = clauses.isEmpty() ? "" : String.join(" AND ", clauses);
            jql = (jql.isEmpty() ? "" : jql + " ") + "ORDER BY " + sortField + " " + direction;

            JsonObject body = new JsonObject();
            body.addProperty("jql", jql);
            body.addProperty("maxResults", maxResults != null ? maxResults.intValue() : 50);
            if (pageToken != null && !pageToken.isBlank()) {
                body.addProperty("nextPageToken", pageToken);
            }
            JsonObject result = client.post("/rest/api/3/search/jql", body);
            var issues = JiraIssueTransformer.transformList(result.getAsJsonArray("issues"), client.getBrowseBaseUrl());

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issues", issues);
            out.put("Count", (double) issues.size());
            out.put("Next Page Token", result.has("nextPageToken") ? result.get("nextPageToken").getAsString() : null);
            return ExtensionResponses.ok(out);
        });
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String joinQuoted(List<String> values) {
        List<String> quoted = new ArrayList<>();
        for (String v : values) {
            quoted.add(quote(v));
        }
        return String.join(", ", quoted);
    }
}
