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
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraIssueTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraUserTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Assign / reassign / unassign (requirement section 7D). */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class AssignmentArea extends AbstractJiraArea {

    @Inject
    public AssignmentArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_9a0b1c2d-3e4f-4a90-2d03-7e8f9a0b1c19",
            name = "Assign Ticket",
            description = "Assign, reassign, or unassign a Jira issue.",
            area = "Assignment",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue", type = "Entity(Jira Issue)", required = false)
    public ExtensionResponse assignTicket(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field.Text(name = "Assignee Account Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String assigneeAccountId,
            @Field.Boolean(name = "Unassign", required = false,
                    attributes = {}, options = {}) Boolean unassign)
            throws MustAuthorizeException {
        return audited("AssignTicket", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            boolean doUnassign = Boolean.TRUE.equals(unassign);
            if (!doUnassign) {
                Validation.requireNonBlank(assigneeAccountId, "Assignee Account Id (or set Unassign = true)");
            }
            JsonObject body = new JsonObject();
            if (doUnassign) {
                body.add("accountId", com.google.gson.JsonNull.INSTANCE);
            } else {
                body.addProperty("accountId", assigneeAccountId);
            }
            client.putNoContent("/rest/api/3/issue/" + issueIdOrKey + "/assignee", body);

            JsonObject issueJson = client.get("/rest/api/3/issue/" + issueIdOrKey);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issue", JiraIssueTransformer.transform(issueJson, client.getBrowseBaseUrl()));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_0b1c2d3e-4f5a-4b01-3e14-8f9a0b1c2d20",
            name = "Find Assignable Users",
            description = "Find users permitted to be assigned on a project or a specific issue.",
            area = "Assignment",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Users", type = "[ Entity(Jira User) ]", required = false)
    public ExtensionResponse findAssignableUsers(
            @Field.Text(name = "Project Key Or Id", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String projectKeyOrId,
            @Field.Text(name = "Issue Id Or Key", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field.Text(name = "Search Text", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String searchText)
            throws MustAuthorizeException {
        return audited("FindAssignableUsers", () -> {
            Validation.requireAtLeastOne(new String[]{"Project Key Or Id", "Issue Id Or Key"},
                    projectKeyOrId, issueIdOrKey);
            StringBuilder path = new StringBuilder("/rest/api/3/user/assignable/search?");
            boolean any = false;
            if (projectKeyOrId != null && !projectKeyOrId.isBlank()) {
                path.append("project=").append(encode(projectKeyOrId));
                any = true;
            }
            if (issueIdOrKey != null && !issueIdOrKey.isBlank()) {
                path.append(any ? "&" : "").append("issueKey=").append(encode(issueIdOrKey));
                any = true;
            }
            if (searchText != null && !searchText.isBlank()) {
                path.append(any ? "&" : "").append("query=").append(encode(searchText));
            }
            var result = client.getArray(path.toString());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Users", JiraUserTransformer.transformList(result));
            return ExtensionResponses.ok(out);
        });
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
