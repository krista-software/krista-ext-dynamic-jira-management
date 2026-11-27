# Authentication

Dynamic Jira Management supports two authentication mechanisms, selected via **Authentication Method**
on the Setup tab.

## Option A — API Token (Basic auth)

The simpler option: a pre-issued Atlassian API token sent as HTTP Basic auth alongside your account
email. No browser sign-in flow.

1. Go to https://id.atlassian.com/manage-profile/security/api-tokens and create an API token.
2. Copy the token into the **API Token** field on the Setup tab.
3. Set **Account Email** to the Atlassian account email the token belongs to, and **Jira Site URL** to
   your site (e.g. `https://your-company.atlassian.net`).

## Option B — OAuth 2.0 (3-legged / authorization code)

For a shared connection that doesn't rely on one person's personal token, or for the Krista platform to
request authorization on behalf of a specific user (per-user / MCP-attributed requests).

1. Register an OAuth 2.0 (3LO) app at https://developer.atlassian.com/console/myapps/.
2. Add the Jira API with these scopes: `read:jira-user`, `read:jira-work`, `write:jira-work`,
   `manage:jira-project`, `offline_access` (for refresh tokens).
3. Add a Redirect URI matching your Krista appliance's callback route (the extension exposes
   `rest/jira/callback`); copy that exact URL into both the Atlassian app and the Setup tab's
   **OAuth Redirect URL** field.
4. Copy the app's **Client ID** / **Client Secret** into the matching Setup-tab fields.
5. Set **Authentication Method** to `OAuth 2.0` and save — clicking **Test Connection** opens the
   Atlassian authorization popup.

## Required scopes / permissions

| Scope | Why it's needed |
|---|---|
| `read:jira-user` | Get Authenticated User, Find Assignable Users, comment/changelog authors. |
| `read:jira-work` | All read requests — projects, schema discovery, issues, comments, transitions, search, changelog. |
| `write:jira-work` | Create Issue, Update Issue, Add Comment, Change Ticket Status, Assign Ticket. |
| `manage:jira-project` | Project/field/field-context metadata used by schema discovery. |
| `offline_access` | Refresh tokens for OAuth 2.0 — without it, the user must re-authorize every hour. |

## Verifying

Run **Test Connection** in the Setup area (or the [Test Connection](pages/test-connection.md) catalog
request). A successful result confirms the credentials and connectivity and returns the authenticated
Jira user; a failure returns an actionable message (e.g. missing credentials, 401 unauthorized, or —
for OAuth 2.0 — opens the authorization popup if no valid token is stored yet).
