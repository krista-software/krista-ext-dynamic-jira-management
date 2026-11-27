package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement.integration;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * ScribeJava API definition for Atlassian's OAuth 2.0 (3LO) authorization-code flow.
 * scribejava-apis does not bundle an Atlassian provider, so this defines the two fixed endpoints
 * directly from Atlassian's own published documentation for "OAuth 2.0 (3LO) apps":
 * authorize at https://auth.atlassian.com/authorize, exchange/refresh at
 * https://auth.atlassian.com/oauth/token.
 */
public class AtlassianApi extends DefaultApi20 {

    private static final AtlassianApi INSTANCE = new AtlassianApi();

    protected AtlassianApi() {
    }

    public static AtlassianApi instance() {
        return INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://auth.atlassian.com/oauth/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://auth.atlassian.com/authorize";
    }
}
