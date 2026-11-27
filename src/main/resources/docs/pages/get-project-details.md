# Get Project Details

## Overview
Fetches full details for one Jira project.

## Request Details
- **Area**: Projects
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/project/{projectIdOrKey}`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | Yes | e.g. `KE` or the numeric project id. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Project | Entity(Jira Project) | |

## Example
**Input**: `Project Key Or Id="KE"`
**Output**: `Project={Key: "KE", Name: "Krista Essentials", ...}`

## Error Handling
- **404 Not Found** — the project key/id does not exist or is not accessible to this account.
