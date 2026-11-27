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
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraChangelogTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ticket history / changelog (requirement section 7F). */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class HistoryArea extends AbstractJiraArea {

    @Inject
    public HistoryArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_2d3e4f5a-6b7c-4d23-505b-0b1c2d3e4f22",
            name = "Get Ticket Changelog",
            description = "Retrieve the field-level change history for a Jira issue.",
            area = "History",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Changelog", type = "[ Entity(Jira Changelog Entry) ]", required = false)
    public ExtensionResponse getTicketChangelog(
            @Field.Text(name = "Issue Id Or Key", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String issueIdOrKey,
            @Field(name = "Max Results", type = "Number", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Double maxResults)
            throws MustAuthorizeException {
        return audited("GetTicketChangelog", () -> {
            Validation.requireNonBlank(issueIdOrKey, "Issue Id Or Key");
            String path = "/rest/api/3/issue/" + issueIdOrKey + "/changelog?maxResults="
                    + (maxResults != null ? maxResults.intValue() : 100);
            JsonObject result = client.get(path);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Changelog", JiraChangelogTransformer.transformList(result.getAsJsonArray("values")));
            return ExtensionResponses.ok(out);
        });
    }
}
