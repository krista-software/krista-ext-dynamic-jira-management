package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.AbstractJiraArea;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.ExtensionResponses;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.TelemetryHelper;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraPriorityTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraResolutionTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Site-wide reference lookups used to auto-populate/validate Priority and Resolution values
 *  (requirement section 3 — "Jira project defaults"). */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class ReferenceDataArea extends AbstractJiraArea {

    @Inject
    public ReferenceDataArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_3e4f5a6b-7c8d-4e34-615c-1c2d3e4f5a23",
            name = "List Priorities",
            description = "List the site's configured priority levels.",
            area = "Reference Data",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Priorities", type = "[ Entity(Jira Priority) ]", required = false)
    public ExtensionResponse listPriorities() throws MustAuthorizeException {
        return audited("ListPriorities", () -> {
            var result = client.get("/rest/api/3/priority/search");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Priorities", JiraPriorityTransformer.transformList(result.getAsJsonArray("values")));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_4f5a6b7c-8d9e-4f45-726d-2d3e4f5a6b24",
            name = "List Resolutions",
            description = "List the site's configured resolutions.",
            area = "Reference Data",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Resolutions", type = "[ Entity(Jira Resolution) ]", required = false)
    public ExtensionResponse listResolutions() throws MustAuthorizeException {
        return audited("ListResolutions", () -> {
            var result = client.get("/rest/api/3/resolution/search");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("Resolutions", JiraResolutionTransformer.transformList(result.getAsJsonArray("values")));
            return ExtensionResponses.ok(out);
        });
    }
}
