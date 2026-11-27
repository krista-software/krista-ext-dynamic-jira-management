package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraFieldSchema;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.AbstractJiraArea;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.ExtensionResponses;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.SchemaCache;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.SchemaLookup;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.TelemetryHelper;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.Validation;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.AdfConverter;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraIssueTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.FreeForm;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create / Get / Update Issue — the heart of the requirement's "dynamic mandatory field handling"
 * (section 4) and "ticket creation" (section 5). Well-known Jira fields (summary, description,
 * priority, assignee, labels, components, due date, environment) are typed inputs; any other field the
 * discovered schema requires (including every custom field) is supplied via {@code Additional Fields} —
 * a {@code fieldId -&gt; value} FreeForm map, matching the shape returned by Get Field Schema For Issue
 * Type's {@code Field Id}. This is the schema-driven alternative to generating a new compiled entity per
 * project: the create screen's full set of possible values is discovered and validated at runtime,
 * rather than assumed from a fixed, hardcoded ticket schema.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class IssueArea extends AbstractJiraArea {

    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter DUE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    /** Jira field ids this Area already exposes as typed inputs — anything else the discovered schema
     *  requires must come through Additional Fields. */
    private static final Set<String> WELL_KNOWN_FIELD_IDS = Set.of(
            "summary", "description", "priority", "assignee", "labels", "components", "duedate",
            "environment", "project", "issuetype");

    private final SchemaCache schemaCache;
    private final DynamicJiraManagementAttributes attributes;

    @Inject
    public IssueArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker,
                      SchemaCache schemaCache, DynamicJiraManagementAttributes attributes) {
        super(client, telemetry, requestContext, invoker);
        this.schemaCache = schemaCache;
        this.attributes = attributes;
    }

    @CatalogRequest(
            id = "localDomainRequest_2f3a4b5c-6d7e-4f2a-b73c-0d1e2f3a4b12",
            name = "Create Issue",
            description = "Create a Jira ticket once every mandatory field discovered for the selected "
                    + "project and issue type is present and validated.",
            area = "Issues",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue", type = "Entity(Jira Issue)", required = false)
    public ExtensionResponse createIssue(
            @Field.Text(name = "Project Key Or Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId,
            @Field.Text(name = "Issue Type Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueTypeId,
            @Field.Text(name = "Summary", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String summary,
            @Field(name = "Description", type = "Paragraph", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String description,
            @Field.Text(name = "Priority Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String priorityId,
            @Field.Text(name = "Assignee Account Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String assigneeAccountId,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Component Ids", type = "[ Text ]", required = false) List<String> componentIds,
            @Field.Date(name = "Due Date", required = false, includeTimeOfDay = false,
                    allowPast = false, allowToday = true, allowFuture = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) Long dueDate,
            @Field(name = "Environment", type = "Paragraph", required = false,
                    attributes = {}, options = {}) String environment,
            @Field(name = "Additional Fields", type = "FreeForm", required = false,
                    attributes = {}, options = {}) FreeForm additionalFields)
            throws MustAuthorizeException {
        return audited("CreateIssue", () -> {
            Validation.requireNonBlank(projectKeyOrId, "Project Key Or Id");
            Validation.requireNonBlank(issueTypeId, "Issue Type Id");
            Validation.requireNonBlank(summary, "Summary");

            List<JiraFieldSchema> schema = SchemaLookup.get(
                    client, schemaCache, attributes.getSchemaCacheMinutes(), projectKeyOrId, issueTypeId);

            JsonObject fields = new JsonObject();
            JsonObject project = new JsonObject();
            project.addProperty("key", projectKeyOrId);
            fields.add("project", project);
            JsonObject issuetype = new JsonObject();
            issuetype.addProperty("id", issueTypeId);
            fields.add("issuetype", issuetype);
            fields.addProperty("summary", summary);
            if (description != null && !description.isBlank()) {
                fields.add("description", AdfConverter.fromPlainText(description));
            }
            if (priorityId != null && !priorityId.isBlank()) {
                JsonObject priority = new JsonObject();
                priority.addProperty("id", priorityId);
                fields.add("priority", priority);
            }
            if (assigneeAccountId != null && !assigneeAccountId.isBlank()) {
                JsonObject assignee = new JsonObject();
                assignee.addProperty("accountId", assigneeAccountId);
                fields.add("assignee", assignee);
            }
            if (labels != null && !labels.isEmpty()) {
                JsonArray labelArray = new JsonArray();
                labels.forEach(labelArray::add);
                fields.add("labels", labelArray);
            }
            if (componentIds != null && !componentIds.isEmpty()) {
                JsonArray componentArray = new JsonArray();
                for (String id : componentIds) {
                    JsonObject c = new JsonObject();
                    c.addProperty("id", id);
                    componentArray.add(c);
                }
                fields.add("components", componentArray);
            }
            if (dueDate != null) {
                fields.addProperty("duedate", DUE_DATE_FORMAT.format(Instant.ofEpochMilli(dueDate)));
            }
            if (environment != null && !environment.isBlank()) {
                fields.add("environment", AdfConverter.fromPlainText(environment));
            }
            Set<String> suppliedFieldIds = new java.util.HashSet<>(WELL_KNOWN_FIELD_IDS);
            if (additionalFields != null) {
                for (var entry : additionalFields.entrySet()) {
                    fields.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
                    suppliedFieldIds.add(entry.getKey());
                }
            }
            // Drop well-known ids whose typed input was left blank — they are NOT "supplied" for
            // mandatory-field purposes even though they are well-known fields this Area understands.
            if (description == null || description.isBlank()) { suppliedFieldIds.remove("description"); }
            if (priorityId == null || priorityId.isBlank()) { suppliedFieldIds.remove("priority"); }
            if (assigneeAccountId == null || assigneeAccountId.isBlank()) { suppliedFieldIds.remove("assignee"); }
            if (labels == null || labels.isEmpty()) { suppliedFieldIds.remove("labels"); }
            if (componentIds == null || componentIds.isEmpty()) { suppliedFieldIds.remove("components"); }
            if (dueDate == null) { suppliedFieldIds.remove("duedate"); }
            if (environment == null || environment.isBlank()) { suppliedFieldIds.remove("environment"); }

            List<String> missing = new ArrayList<>();
            for (JiraFieldSchema fieldSchema : schema) {
                if (Boolean.TRUE.equals(fieldSchema.required) && !suppliedFieldIds.contains(fieldSchema.fieldId)) {
                    missing.add(fieldSchema.name);
                }
            }
            Validation.requireNoMissingMandatoryFields(missing);

            JsonObject body = new JsonObject();
            body.add("fields", fields);
            JsonObject created = client.post("/rest/api/3/issue", body);
            String key = created.get("key").getAsString();
            JsonObject issueJson = client.get("/rest/api/3/issue/" + key);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issue", JiraIssueTransformer.transform(issueJson, client.getBrowseBaseUrl()));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_3a4b5c6d-7e8f-4a3b-c84d-1e2f3a4b5c13",
            name = "Get Issue Details",
            description = "Fetch full details for a Jira issue, including custom fields.",
            area = "Issues",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue", type = "Entity(Jira Issue)", required = false)
    public ExtensionResponse getIssueDetails(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey)
            throws MustAuthorizeException {
        return audited("GetIssueDetails", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            JsonObject issueJson = client.get("/rest/api/3/issue/" + issueIdOrKey);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issue", JiraIssueTransformer.transform(issueJson, client.getBrowseBaseUrl()));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_4b5c6d7e-8f9a-4b4c-d95e-2f3a4b5c6d14",
            name = "Update Issue",
            description = "Update only the fields explicitly supplied on an existing issue, leaving every "
                    + "other field untouched.",
            area = "Issues",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue", type = "Entity(Jira Issue)", required = false)
    public ExtensionResponse updateIssue(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field.Text(name = "Summary", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String summary,
            @Field(name = "Description", type = "Paragraph", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String description,
            @Field.Text(name = "Priority Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String priorityId,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Component Ids", type = "[ Text ]", required = false) List<String> componentIds,
            @Field.Date(name = "Due Date", required = false, includeTimeOfDay = false,
                    allowPast = false, allowToday = true, allowFuture = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) Long dueDate,
            @Field(name = "Environment", type = "Paragraph", required = false,
                    attributes = {}, options = {}) String environment,
            @Field(name = "Additional Fields", type = "FreeForm", required = false,
                    attributes = {}, options = {}) FreeForm additionalFields)
            throws MustAuthorizeException {
        return audited("UpdateIssue", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            JsonObject fields = new JsonObject();
            if (summary != null && !summary.isBlank()) {
                fields.addProperty("summary", summary);
            }
            if (description != null) {
                fields.add("description", AdfConverter.fromPlainText(description));
            }
            if (priorityId != null && !priorityId.isBlank()) {
                JsonObject priority = new JsonObject();
                priority.addProperty("id", priorityId);
                fields.add("priority", priority);
            }
            if (labels != null) {
                JsonArray labelArray = new JsonArray();
                labels.forEach(labelArray::add);
                fields.add("labels", labelArray);
            }
            if (componentIds != null) {
                JsonArray componentArray = new JsonArray();
                for (String id : componentIds) {
                    JsonObject c = new JsonObject();
                    c.addProperty("id", id);
                    componentArray.add(c);
                }
                fields.add("components", componentArray);
            }
            if (dueDate != null) {
                fields.addProperty("duedate", DUE_DATE_FORMAT.format(Instant.ofEpochMilli(dueDate)));
            }
            if (environment != null) {
                fields.add("environment", AdfConverter.fromPlainText(environment));
            }
            if (additionalFields != null) {
                for (var entry : additionalFields.entrySet()) {
                    fields.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
                }
            }
            if (fields.size() == 0) {
                throw new IllegalArgumentException(
                        "No fields were supplied to update. Provide at least one field to change.");
            }
            JsonObject body = new JsonObject();
            body.add("fields", fields);
            client.putNoContent("/rest/api/3/issue/" + issueIdOrKey, body);

            JsonObject issueJson = client.get("/rest/api/3/issue/" + issueIdOrKey);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issue", JiraIssueTransformer.transform(issueJson, client.getBrowseBaseUrl()));
            return ExtensionResponses.ok(out);
        });
    }
}
