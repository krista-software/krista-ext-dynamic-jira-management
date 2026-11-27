package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.api;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS application hosting the OAuth 2.0 (3LO) callback (Pattern C). Full callback route:
 * {@code rest/<jaxrsId>/jira/callback} — {@code jaxrsId} is left at its default ("rest"), so the route
 * is {@code rest/rest/jira/callback}... actually the extension does not set a custom jaxrsId, so the
 * platform's default "rest" prefix applies once, giving {@code rest/jira/callback} (this
 * {@code @ApplicationPath}). See custom-tabs.md's routing rule — this path is also what must be
 * registered as the Redirect URI on the Atlassian OAuth 2.0 (3LO) app (the
 * {@code DynamicJiraManagementAttributes#OAUTH_REDIRECT_URL} Setup field).
 */
@Service
@ApplicationPath("jira")
@ContractsProvided(Application.class)
public class JiraApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(JiraAuthCallbackResource.class);
        return classes;
    }
}
