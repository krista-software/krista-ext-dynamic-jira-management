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
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.Validation;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraProjectTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraStatusTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Project Discovery (requirement section 2, Step 1): every ticket-creation flow starts by letting the
 *  user pick a Jira project accessible to them. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class ProjectArea extends AbstractJiraArea {

    @Inject
    public ProjectArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_4d5e6f7a-8b9c-4d4e-bf5a-2b3c4d5e6f04",
            name = "List Projects",
            description = "List all Jira projects accessible to the authenticated user.",
            area = "Projects",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Projects", type = "[ Entity(Jira Project) ]", required = false)
    @Field(name = "Count", type = "Number", required = false,
            attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse listProjects(
            @Field.Text(name = "Search Text", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String searchText,
            @Field(name = "Max Results", type = "Number", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double maxResults)
            throws MustAuthorizeException {
        return audited("ListProjects", () -> {
            StringBuilder path = new StringBuilder("/rest/api/3/project/search?maxResults=")
                    .append(maxResults != null ? maxResults.intValue() : 50);
            if (searchText != null && !searchText.isBlank()) {
                path.append("&query=").append(java.net.URLEncoder.encode(searchText.trim(), java.nio.charset.StandardCharsets.UTF_8));
            }
            JsonObject result = client.get(path.toString());
            var projects = JiraProjectTransformer.transformList(result.getAsJsonArray("values"));
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Projects", projects);
            out.put("Count", (double) projects.size());
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_5e6f7a8b-9c0d-4e5f-a06b-3c4d5e6f7a05",
            name = "Get Project Details",
            description = "Fetch full details for one Jira project.",
            area = "Projects",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Project", type = "Entity(Jira Project)", required = false)
    public ExtensionResponse getProjectDetails(
            @Field.Text(name = "Project Key Or Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId)
            throws MustAuthorizeException {
        return audited("GetProjectDetails", () -> {
            Validation.requireNonBlank(projectKeyOrId, "Project Key Or Id");
            JsonObject result = client.get("/rest/api/3/project/" + encode(projectKeyOrId));
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Project", JiraProjectTransformer.transform(result));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_6f7a8b9c-0d1e-4f60-b17c-4d5e6f7a8b06",
            name = "Get Project Statuses",
            description = "List the distinct workflow statuses configured across this project's issue types.",
            area = "Projects",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Statuses", type = "[ Entity(Jira Status) ]", required = false)
    public ExtensionResponse getProjectStatuses(
            @Field.Text(name = "Project Key Or Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId)
            throws MustAuthorizeException {
        return audited("GetProjectStatuses", () -> {
            Validation.requireNonBlank(projectKeyOrId, "Project Key Or Id");
            var result = client.getArray("/rest/api/3/project/" + encode(projectKeyOrId) + "/statuses");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Statuses", JiraStatusTransformer.transformProjectStatuses(result));
            return ExtensionResponses.ok(out);
        });
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
