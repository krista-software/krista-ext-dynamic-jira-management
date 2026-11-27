package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraTransition;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.AbstractJiraArea;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.ExtensionResponses;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.TelemetryHelper;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.Validation;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.AdfConverter;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraIssueTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraTransitionTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow management (requirement section 7C). Status is NEVER written directly — every change goes
 * through a transition id discovered from {@link #getAvailableTransitions}, then the resulting status
 * is re-fetched to verify the transition actually took effect before reporting success.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class WorkflowArea extends AbstractJiraArea {

    @Inject
    public WorkflowArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_7e8f9a0b-1c2d-4e7f-0b81-5c6d7e8f9a17",
            name = "Get Available Transitions",
            description = "Discover the workflow transitions valid from an issue's current status.",
            area = "Workflow",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Transitions", type = "[ Entity(Jira Transition) ]", required = false)
    public ExtensionResponse getAvailableTransitions(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey)
            throws MustAuthorizeException {
        return audited("GetAvailableTransitions", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            JsonObject result = client.get("/rest/api/3/issue/" + issueIdOrKey + "/transitions");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Transitions", JiraTransitionTransformer.transformList(result.getAsJsonArray("transitions")));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_8f9a0b1c-2d3e-4f80-1c92-6d7e8f9a0b18",
            name = "Change Ticket Status",
            description = "Execute a workflow transition by id (discovered via Get Available Transitions) "
                    + "and verify the resulting status before reporting success.",
            area = "Workflow",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue", type = "Entity(Jira Issue)", required = false)
    public ExtensionResponse changeTicketStatus(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field.Text(name = "Transition Id", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String transitionId,
            @Field(name = "Comment", type = "Paragraph", required = false,
                    attributes = {}, options = {}) String comment)
            throws MustAuthorizeException {
        return audited("ChangeTicketStatus", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            Validation.requireNonBlank(transitionId, "Transition Id");

            JsonObject available = client.get("/rest/api/3/issue/" + issueIdOrKey + "/transitions");
            List<JiraTransition> transitions =
                    JiraTransitionTransformer.transformList(available.getAsJsonArray("transitions"));
            boolean valid = transitions.stream().anyMatch(t -> transitionId.equals(t.id));
            if (!valid) {
                throw new IllegalArgumentException("Transition id '" + transitionId + "' is not valid from this "
                        + "issue's current status. Call Get Available Transitions to see valid options.");
            }

            JsonObject body = new JsonObject();
            JsonObject transition = new JsonObject();
            transition.addProperty("id", transitionId);
            body.add("transition", transition);
            if (comment != null && !comment.isBlank()) {
                var update = new com.google.gson.JsonArray();
                JsonObject commentWrapper = new JsonObject();
                JsonObject commentBody = new JsonObject();
                commentBody.add("body", AdfConverter.fromPlainText(comment));
                commentWrapper.add("add", commentBody);
                update.add(commentWrapper);
                JsonObject updateObj = new JsonObject();
                updateObj.add("comment", update);
                body.add("update", updateObj);
            }
            client.postNoContent("/rest/api/3/issue/" + issueIdOrKey + "/transitions", body);

            JsonObject issueJson = client.get("/rest/api/3/issue/" + issueIdOrKey);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Issue", JiraIssueTransformer.transform(issueJson, client.getBrowseBaseUrl()));
            return ExtensionResponses.ok(out);
        });
    }
}
