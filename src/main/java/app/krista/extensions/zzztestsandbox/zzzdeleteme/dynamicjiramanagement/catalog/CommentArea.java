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
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.AdfConverter;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraCommentTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Add/retrieve comments, including the latest-N and pagination requirements (section 7B). */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class CommentArea extends AbstractJiraArea {

    @Inject
    public CommentArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_5c6d7e8f-9a0b-4c5d-ea6f-3a4b5c6d7e15",
            name = "Get Comments",
            description = "Retrieve comments for an issue — supports ordering and a maximum-results cap "
                    + "(e.g. 'the latest 5 comments') and pagination via Start At / Max Results.",
            area = "Comments",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Comments", type = "[ Entity(Jira Comment) ]", required = false)
    @Field(name = "Total", type = "Number", required = false,
            attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse getComments(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field(name = "Max Results", type = "Number", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double maxResults,
            @Field(name = "Start At", type = "Number", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double startAt,
            @Field.PickOne(name = "Order By", values = {"Oldest First", "Newest First"}, required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String orderBy)
            throws MustAuthorizeException {
        return audited("GetComments", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            StringBuilder path = new StringBuilder("/rest/api/3/issue/").append(issueIdOrKey).append("/comment?");
            path.append("maxResults=").append(maxResults != null ? maxResults.intValue() : 50);
            path.append("&startAt=").append(startAt != null ? startAt.intValue() : 0);
            path.append("&orderBy=").append("Newest First".equalsIgnoreCase(orderBy) ? "-created" : "created");
            JsonObject result = client.get(path.toString());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Comments", JiraCommentTransformer.transformList(result.getAsJsonArray("comments")));
            out.put("Total", result.has("total") ? result.get("total").getAsDouble() : 0.0);
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_6d7e8f9a-0b1c-4d6e-fb70-4b5c6d7e8f16",
            name = "Add Comment",
            description = "Add a comment to a Jira issue.",
            area = "Comments",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Comment", type = "Entity(Jira Comment)", required = false)
    public ExtensionResponse addComment(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field(name = "Comment Text", type = "Paragraph", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String commentText)
            throws MustAuthorizeException {
        return audited("AddComment", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            Validation.requireNonBlank(commentText, "Comment Text");
            JsonObject body = new JsonObject();
            body.add("body", AdfConverter.fromPlainText(commentText));
            JsonObject created = client.post("/rest/api/3/issue/" + issueIdOrKey + "/comment", body);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Comment", JiraCommentTransformer.transform(created));
            return ExtensionResponses.ok(out);
        });
    }
}
