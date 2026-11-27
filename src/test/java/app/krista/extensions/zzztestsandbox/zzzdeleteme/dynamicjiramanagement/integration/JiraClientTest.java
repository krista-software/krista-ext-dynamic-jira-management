package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.DynamicJiraManagementAttributes;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Status->exception mapping, retry, and header/auth behavior — via MockWebServer, no real network.
 *  Attributes are configured in API Token mode (the simplest session to set up); OAuth dispatch is
 *  covered by {@link JiraOAuthServiceTest}-style unit tests on the pure logic pieces, not here, since
 *  exercising the real browser flow needs the Krista runtime (see krista-extension-testing's
 *  what-to-test.md: Krista-framework auth plumbing is neglected by unit tests). */
class JiraClientTest {

    private MockWebServer server;
    private JiraClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(DynamicJiraManagementAttributes.AUTH_TYPE, DynamicJiraManagementAttributes.AUTH_TYPE_API_TOKEN);
        attrs.put(DynamicJiraManagementAttributes.SITE_URL, server.url("/").toString().replaceAll("/$", ""));
        attrs.put(DynamicJiraManagementAttributes.ACCOUNT_EMAIL, "test@example.com");
        attrs.put(DynamicJiraManagementAttributes.API_TOKEN, "test-token");
        attrs.put(DynamicJiraManagementAttributes.MAX_RETRY_ATTEMPTS, 3);
        attrs.put(DynamicJiraManagementAttributes.RETRY_DELAY_MILLIS, 10);
        DynamicJiraManagementAttributes attributes = new DynamicJiraManagementAttributes(attrs);

        RequestContext requestContext = mock(RequestContext.class);
        when(requestContext.invokeAsUser()).thenReturn(false);
        AuthorizationContext authorizationContext = mock(AuthorizationContext.class);
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInvokerId()).thenReturn("test-invoker");

        client = new JiraClient(attributes, mock(JiraOAuthTokenStore.class), mock(JiraOAuthService.class),
                requestContext, authorizationContext, invoker);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void get_successfulResponse_parsesJsonBody() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"accountId\":\"abc\"}"));

        JsonObject result = client.get("/rest/api/3/myself");

        assertEquals("abc", result.get("accountId").getAsString());
        var recorded = server.takeRequest();
        assertEquals("Basic dGVzdEBleGFtcGxlLmNvbTp0ZXN0LXRva2Vu", recorded.getHeader("Authorization"));
    }

    @Test
    void get_401_throwsSecurityExceptionForApiTokenMode() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"message\":\"Unauthorized\"}"));

        SecurityException ex = assertThrows(SecurityException.class, () -> client.get("/rest/api/3/myself"));
        assertEquals(true, ex.getMessage().contains("Account Email"));
    }

    @Test
    void get_404_throwsIllegalArgumentException() {
        server.enqueue(new MockResponse().setResponseCode(404)
                .setBody("{\"errorMessages\":[\"Issue does not exist\"]}"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> client.get("/rest/api/3/issue/MISSING-1"));
        assertEquals(true, ex.getMessage().contains("Issue does not exist"));
    }

    @Test
    void get_429ThenSuccess_retriesAndEventuallySucceeds() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{}"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"accountId\":\"abc\"}"));

        JsonObject result = client.get("/rest/api/3/myself");

        assertEquals("abc", result.get("accountId").getAsString());
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void get_409_throwsIllegalStateException() {
        server.enqueue(new MockResponse().setResponseCode(409).setBody("{\"message\":\"Conflict\"}"));

        assertThrows(IllegalStateException.class, () -> client.get("/rest/api/3/issue/KE-1"));
    }

    @Test
    void putNoContent_204_doesNotThrow() {
        server.enqueue(new MockResponse().setResponseCode(204));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> client.putNoContent("/rest/api/3/issue/KE-1", new JsonObject()));
    }
}
