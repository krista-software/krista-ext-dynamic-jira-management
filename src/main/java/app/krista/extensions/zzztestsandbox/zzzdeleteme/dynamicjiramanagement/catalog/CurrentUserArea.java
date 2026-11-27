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
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraUserTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovers the authenticated Jira user — used to auto-populate Reporter (always) and Assignee (when
 * the user asks to assign a ticket to themselves), per the requirement's field auto-population priority
 * order (authenticated-user information ranks above request-text inference and defaults).
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class CurrentUserArea extends AbstractJiraArea {

    @Inject
    public CurrentUserArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_3c4d5e6f-7a8b-4c3d-ae4f-1a2b3c4d5e03",
            name = "Get Authenticated User",
            description = "Return the Jira user identified by the current connection (operator) or the "
                    + "invoking end-user (per-user/MCP).",
            area = "Current User",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "User", type = "Entity(Jira User)", required = false)
    public ExtensionResponse getAuthenticatedUser() throws MustAuthorizeException {
        return audited("GetAuthenticatedUser", () -> {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("User", JiraUserTransformer.transform(client.get("/rest/api/3/myself")));
            return ExtensionResponses.ok(out);
        });
    }
}
