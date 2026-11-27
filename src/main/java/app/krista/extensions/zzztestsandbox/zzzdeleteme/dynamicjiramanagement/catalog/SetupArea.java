package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.entities.JiraUser;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.AbstractJiraArea;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.ExtensionResponses;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support.TelemetryHelper;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.impl.transformers.JiraUserTransformer;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/** Setup area — the standard Test Connection + Health Check requests. Area name is exactly "Setup"
 *  (capital S, lowercase u) per the registered-area naming rule. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0807ef5d-4442-4852-a367-edbaf38e01ee")
public class SetupArea extends AbstractJiraArea {

    @Inject
    public SetupArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext, Invoker invoker) {
        super(client, telemetry, requestContext, invoker);
    }

    @CatalogRequest(
            id = "localDomainRequest_1a2b3c4d-5e6f-4a1b-8c2d-9e0f1a2b3c01",
            name = "Test Connection",
            description = "Validate connectivity and authentication against Jira Cloud.",
            area = "SetUp",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Current User", type = "Entity(Jira User)", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse testConnection() throws MustAuthorizeException {
        return audited("TestConnection", () -> {
            Map<String, Object> out = new LinkedHashMap<>();
            JsonObject me = client.get("/rest/api/3/myself");
            JiraUser user = JiraUserTransformer.transform(me);
            out.put("Current User", user);
            out.put("Success", true);
            out.put("Message", "Connected to Jira as " + (user != null ? user.displayName : "unknown"));
            return ExtensionResponses.ok(out);
        });
    }

    @CatalogRequest(
            id = "localDomainRequest_2b3c4d5e-6f7a-4b2c-9d3e-0f1a2b3c4d02",
            name = "Health Check",
            description = "Check extension and Jira connectivity health.",
            area = "SetUp",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Is Healthy", required = false)
    @Field(name = "Heap Used MB", type = "Number", required = false,
            attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "Details", required = false)
    public ExtensionResponse healthCheck() throws MustAuthorizeException {
        return audited("HealthCheck", () -> {
            Map<String, Object> out = new LinkedHashMap<>();
            MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
            long heapUsedMb = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            boolean healthy;
            String details;
            try {
                client.get("/rest/api/3/myself");
                healthy = true;
                details = "Jira API reachable.";
            } catch (MustAuthorizeException e) {
                throw e;
            } catch (Exception e) {
                healthy = false;
                details = "Jira API unreachable: " + e.getMessage();
            }
            out.put("Is Healthy", healthy);
            out.put("Heap Used MB", (double) heapUsedMb);
            out.put("Details", details);
            return ExtensionResponses.ok(out);
        });
    }
}
