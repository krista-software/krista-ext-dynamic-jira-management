# Get Issue Details

## Overview
Fetches full details for a Jira issue, including every discovered custom field.

## Request Details
- **Area**: Issues
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/issue/{issueIdOrKey}`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | e.g. `KE-452`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue | Entity(Jira Issue) | |

## Example
**Input**: `Issue Id Or Key="KE-452"`
**Output**: `Issue={Key: "KE-452", Summary: "Checkout button unresponsive", Status Name: "In Progress", ...}`

## Error Handling
- **404 Not Found** — the issue key/id does not exist or is not accessible.
