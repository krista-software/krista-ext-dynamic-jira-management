# Get Project Statuses

## Overview
Lists the distinct workflow statuses configured across a project's issue types (e.g. to show "tickets
currently in Code Review").

## Request Details
- **Area**: Projects
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/project/{projectIdOrKey}/statuses`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | Yes | |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Statuses | [ Entity(Jira Status) ] | De-duplicated across every issue type in the project. |

## Example
**Input**: `Project Key Or Id="KE"`
**Output**: `Statuses=[{Name: "To Do", ...}, {Name: "Code Review", ...}, {Name: "Done", ...}]`

## Error Handling
- **404 Not Found** — the project key/id does not exist or is not accessible.
