# Configuration

The extension is configured through the **Setup** tab in Krista. Configuration is stored per-invoker —
each invoker keeps its own independent connection and settings.

## Setup tab fields

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| Authentication Method | Yes | `API Token` | `API Token` or `OAuth 2.0` — selects which of the field groups below apply. |
| Jira Site URL | Required for API Token | -- | e.g. `https://your-company.atlassian.net`. Must start with `https://`. |
| Account Email | Required for API Token | -- | The Atlassian account email the API token belongs to. |
| API Token | Required for API Token (secured) | -- | Created at id.atlassian.com/manage-profile/security/api-tokens. |
| OAuth Client ID | Required for OAuth 2.0 | -- | From your Atlassian OAuth 2.0 (3LO) app. |
| OAuth Client Secret | Required for OAuth 2.0 (secured) | -- | From your Atlassian OAuth 2.0 (3LO) app. |
| OAuth Redirect URL | Required for OAuth 2.0 | -- | Must exactly match a Redirect URI registered on the app. |
| Default Assignee Is Requesting User | No | `false` | When true, leaving Assignee blank on Create Issue assigns the requesting user instead of leaving it unset. |
| Timeout Seconds | No | `30` | HTTP connect/read/write timeout for Jira API calls. |
| Max Retry Attempts | No | `3` | Retries for transient errors (429/502/503/504) with exponential backoff. |
| Retry Delay Millis | No | `500` | Base delay between retry attempts. |
| Schema Cache Minutes | No | `30` | How long a discovered project + issue type field schema is cached before being re-fetched from Jira. |

## Required vs optional

The extension validates these fields on save:

1. **Jira Site URL** — must start with `https://`, required when Authentication Method is API Token.
2. **Account Email** / **API Token** — required when Authentication Method is API Token.
3. **OAuth Client ID** / **OAuth Client Secret** / **OAuth Redirect URL** — required when
   Authentication Method is OAuth 2.0.

All other fields use the defaults above if omitted.

## Notes

- Switching Authentication Method does not clear the other group's fields; only the active group is
  validated and used.
- Schema Cache Minutes only affects [Get Field Schema For Issue Type](pages/get-field-schema-for-issue-type.md)
  and [Create Issue](pages/create-issue.md)/[Update Issue](pages/update-issue.md)'s mandatory-field
  validation — every other request always calls Jira directly.
