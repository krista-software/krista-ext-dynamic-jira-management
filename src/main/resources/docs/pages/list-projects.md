# List Projects

## Overview
Lists every Jira project accessible to the authenticated user — the first step of ticket creation
(project selection).

## Request Details
- **Area**: Projects
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/project/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Search Text | Text | No | Filters projects by name/key. |
| Max Results | Number | No | Defaults to 50. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Projects | [ Entity(Jira Project) ] | |
| Count | Number | |

See [Entities](pages/entities.md) for the Jira Project field list.

## Example
**Input**: `Search Text="Support"`
**Output**: `Projects=[{Key: "SUP", Name: "Support", ...}]`, `Count=1`

## Error Handling
- **Authentication failure** — see [Authentication](pages/Authentication.md).
