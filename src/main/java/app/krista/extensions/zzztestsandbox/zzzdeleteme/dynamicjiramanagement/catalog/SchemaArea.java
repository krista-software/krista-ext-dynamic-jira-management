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
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraFieldAllowedValueTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraFieldContextTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraFieldDefinitionTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraIssueTypeTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic Project Schema Discovery (requirement section 2 — HIGHEST PRIORITY). Every request here reads
 * Jira's own create-metadata / field / field-context APIs at call time — nothing about mandatory fields,
 * allowed values, or custom fields is hardcoded per project. {@link #getFieldSchemaForIssueType} is
 * cached by Project Key + Issue Type Id (see {@link SchemaCache} / requirement section 6) with a
 * configurable TTL and an explicit {@code Force Refresh} override.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class SchemaArea extends AbstractJiraArea {

    private final SchemaCache schemaCache;
    private final DynamicJiraManagementAttributes attributes;

    @Inject
    public SchemaArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker,
                       SchemaCache schemaCache, DynamicJiraManagementAttributes attributes) {
        super(client, telemetry, requestContext, invoker);
        this.schemaCache = schemaCache;
        this.attributes = attributes;
    }

    @CatalogRequest(
            id = "localDomainRequest_7a8b9c0d-1e2f-4a7b-c28d-5e6f7a8b9c07",
            name = "Get Issue Types For Project",
            description = "Discover the issue types a given project allows creating (e.g. Bug, Task, Story).",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue Types", type = "[ Entity(Jira Issue Type) ]", required = false)
    public ExtensionResponse getIssueTypesForProject(
            @Field.Text(name = "Project Key Or Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId)
            throws MustAuthorizeException {
        return audited("GetIssueTypesForProject", () -> {
            Validation.requireNonBlank(projectKeyOrId, "Project Key Or Id");
            JsonObject result = client.get("/rest/api/3/issue/createmeta/" + encode(projectKeyOrId) + "/issuetypes");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issue Types", JiraIssueTypeTransformer.transformList(result.getAsJsonArray("issueTypes")));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_8b9c0d1e-2f3a-4b8c-d39e-6f7a8b9c0d08",
            name = "Get Field Schema For Issue Type",
            description = "Discover the full field schema (mandatory/optional, data type, defaults, "
                    + "allowed values, custom fields) for a specific project + issue type combination — "
                    + "the foundation of dynamic ticket-creation entity generation. Two different "
                    + "project/issue-type combinations return two different schemas.",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Fields", type = "[ Entity(Jira Field Schema) ]", required = false)
    @Field.Boolean(name = "From Cache", required = false)
    public ExtensionResponse getFieldSchemaForIssueType(
            @Field.Text(name = "Project Key Or Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId,
            @Field.Text(name = "Issue Type Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueTypeId,
            @Field.Boolean(name = "Force Refresh", required = false,
                    attributes = {}, options = {}) Boolean forceRefresh)
            throws MustAuthorizeException {
        return audited("GetFieldSchemaForIssueType", () -> {
            Validation.requireNonBlank(projectKeyOrId, "Project Key Or Id");
            Validation.requireNonBlank(issueTypeId, "Issue Type Id");
            if (Boolean.TRUE.equals(forceRefresh)) {
                schemaCache.invalidate(projectKeyOrId, issueTypeId);
            }
            boolean fromCache = schemaCache.get(projectKeyOrId, issueTypeId, attributes.getSchemaCacheMinutes()) != null;
            List<JiraFieldSchema> fields = SchemaLookup.get(
                    client, schemaCache, attributes.getSchemaCacheMinutes(), projectKeyOrId, issueTypeId);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Fields", fields);
            out.put("From Cache", fromCache);
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_9c0d1e2f-3a4b-4c9d-e40f-7a8b9c0d1e09",
            name = "List All Fields",
            description = "List every system and custom field defined on this Jira site.",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Fields", type = "[ Entity(Jira Field Definition) ]", required = false)
    public ExtensionResponse listAllFields() throws MustAuthorizeException {
        return audited("ListAllFields", () -> {
            var result = client.getArray("/rest/api/3/field");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Fields", JiraFieldDefinitionTransformer.transformList(result));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_0d1e2f3a-4b5c-4d0e-f51a-8b9c0d1e2f10",
            name = "Get Field Contexts",
            description = "Discover the configuration context(s) a custom field is scoped to.",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Contexts", type = "[ Entity(Jira Field Context) ]", required = false)
    public ExtensionResponse getFieldContexts(
            @Field.Text(name = "Field Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String fieldId)
            throws MustAuthorizeException {
        return audited("GetFieldContexts", () -> {
            Validation.requireNonBlank(fieldId, "Field Id");
            JsonObject result = client.get("/rest/api/3/field/" + encode(fieldId) + "/context");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Contexts", JiraFieldContextTransformer.transformList(result.getAsJsonArray("values")));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_1e2f3a4b-5c6d-4e1f-a62b-9c0d1e2f3a11",
            name = "Get Field Context Allowed Values",
            description = "Discover the admin-configured option list for a single/multi-select custom field.",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Allowed Values", type = "[ Entity(Jira Field Allowed Value) ]", required = false)
    public ExtensionResponse getFieldContextAllowedValues(
            @Field.Text(name = "Field Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String fieldId,
            @Field.Text(name = "Context Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String contextId)
            throws MustAuthorizeException {
        return audited("GetFieldContextAllowedValues", () -> {
            Validation.requireNonBlank(fieldId, "Field Id");
            Validation.requireNonBlank(contextId, "Context Id");
            JsonObject result = client.get("/rest/api/3/field/" + encode(fieldId) + "/context/"
                    + encode(contextId) + "/option");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Allowed Values", JiraFieldAllowedValueTransformer.transformList(result.getAsJsonArray("values")));
            return ExtensionResponses.ok(out);
        });
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
