package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.catalog.support;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponse.Error.ExceptionType;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.authentication.AuthorizationExceptionHandler;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration.JiraClient;
import app.krista.ksdk.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared base for every Area. Holds the injected {@link JiraClient}, {@link TelemetryHelper}, the
 * {@code invokerId} (multi-tenant scope), and {@link RequestContext} (needed to dispatch a thrown
 * {@link MustAuthorizeException} correctly — operator vs per-user/MCP, see
 * {@link AuthorizationExceptionHandler}). Owns the one {@code audited(...)} wrapper so each
 * {@code @CatalogRequest} method stays thin: validate &rarr; call client &rarr; transform &rarr; build
 * response.
 */
public abstract class AbstractJiraArea {

    protected static final String PREFIX = "jira";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJiraArea.class);

    protected final JiraClient client;
    protected final TelemetryHelper telemetry;
    protected final RequestContext requestContext;
    protected final String invokerId;

    protected AbstractJiraArea(JiraClient client, TelemetryHelper telemetry, RequestContext requestContext,
                                Invoker invoker) {
        this.client = client;
        this.telemetry = telemetry;
        this.requestContext = requestContext;
        this.invokerId = invoker.getInvokerId();
    }

    @FunctionalInterface
    protected interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /**
     * The uniform boundary: timing + telemetry + try/catch -&gt; classified ExtensionResponse.
     * Declares {@code throws MustAuthorizeException} because {@link AuthorizationExceptionHandler}
     * genuinely rethrows it for a per-user/MCP-attributed request — every {@code @CatalogRequest}
     * method that calls this must declare the same, so the platform's own sign-in-prompt machinery
     * receives the real exception instead of a converted failure response.
     */
    protected ExtensionResponse audited(String name, CheckedSupplier<ExtensionResponse> action)
            throws MustAuthorizeException {
        long t0 = telemetry.start(PREFIX + "." + name);
        LOG.info("Catalog '{}' started for invoker {}", name, invokerId);
        try {
            ExtensionResponse r = action.get();
            LOG.info("Catalog '{}' ok in {}ms for invoker {}", name, System.currentTimeMillis() - t0, invokerId);
            telemetry.recordSuccess(PREFIX + "." + name, t0);
            return r;
        } catch (MustAuthorizeException e) {
            // Never swallow — AuthorizationExceptionHandler rethrows for per-user/MCP so the platform
            // can prompt the end-user to sign in, or converts to a normal failure for the operator.
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (SecurityException e) {
            LOG.error("Catalog '{}' AUTH: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.AUTHENTICATION_ERROR);
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn("Catalog '{}' INPUT: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.INPUT_ERROR);
        } catch (Exception e) {
            LOG.error("Catalog '{}' SYSTEM: {}", name, e.getMessage(), e);
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.SYSTEM_ERROR);
        }
    }

}
