package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.authentication;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes a {@link MustAuthorizeException} from ANY real catalog request based on
 * {@code RequestContext#invokeAsUser()} (see authentication-patterns.md's Pattern C per-user dispatch
 * note, generalized from adding-krista-msauth-tabs/SKILL.md Trap 0.12 — this class is token/credential
 * -shape generic, not Microsoft/Graph-specific).
 *
 * Call this from every {@code @CatalogRequest} method's {@code catch (MustAuthorizeException e)} block,
 * passing {@code requestContext.invokeAsUser()}. Do NOT swallow/convert the exception when invokeAsUser
 * is true — the platform needs the real exception to generate a sign-in prompt for the end-user;
 * converting it to a normal response there silently breaks per-user/MCP authentication.
 */
public final class AuthorizationExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationExceptionHandler.class);

    private AuthorizationExceptionHandler() {
    }

    public static ExtensionResponse handleAuthorizationException(MustAuthorizeException exception, boolean invokeAsUser)
            throws MustAuthorizeException {
        LOG.info("Handling MustAuthorizeException, invokeAsUser={}, message={}", invokeAsUser, exception.getMessage());
        if (invokeAsUser) {
            // Per-user/MCP context — let the platform's own broker turn this into a real sign-in prompt
            // for the invoking end-user.
            throw exception;
        }
        // Operator context — no end-user to redirect; surface a normal business failure instead.
        return new ExtensionResponseBuilder()
                .failure("Authentication error: " + exception.getMessage())
                .build();
    }
}
