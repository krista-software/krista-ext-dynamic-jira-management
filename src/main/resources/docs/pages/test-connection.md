# Test Connection

## Overview
Validates that the configured credentials can reach Jira Cloud, and returns the authenticated user —
the same check the Setup tab's Test Connection button runs.

## Request Details
- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/myself`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Current User | Entity(Jira User) | The authenticated Jira user. |
| Success | Boolean | |
| Message | Text | |

See [Entities](pages/entities.md) for the Jira User field list.

## Example
**Input**: (none)
**Output**: `Success=true`, `Message="Connected to Jira as Jane Doe"`, `Current User={Account Id: "...", Display Name: "Jane Doe", ...}`

## Error Handling
- **Missing/invalid credentials** — fails with an actionable message pointing at the Setup tab fields.
- **OAuth 2.0 with no stored token yet** — triggers the Atlassian authorization popup instead of failing.
