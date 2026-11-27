# Get Available Transitions

## Overview
Discovers the workflow transitions valid from an issue's current status — the only legal way to change
status (never a direct field write).

## Request Details
- **Area**: Workflow
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/issue/{issueIdOrKey}/transitions`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Transitions | [ Entity(Jira Transition) ] | |

## Example
**Input**: `Issue Id Or Key="KE-452"`
**Output**: `Transitions=[{Name: "Start Progress", To Status Name: "In Progress"}, {Name: "Done", To Status Name: "Done"}]`

## Error Handling
- **404 Not Found** — the issue key/id does not exist.
