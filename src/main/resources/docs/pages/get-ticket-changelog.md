# Get Ticket Changelog

## Overview
Retrieves the field-level change history for a Jira issue.

## Request Details
- **Area**: History
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/issue/{issueIdOrKey}/changelog`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |
| Max Results | Number | No | Defaults to 100. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Changelog | [ Entity(Jira Changelog Entry) ] | In chronological order, as returned by Jira. |

## Example
**Input**: `Issue Id Or Key="KE-452"`
**Output**: `Changelog=[{Author Display Name: "Jane Doe", Changes: [{Field: "status", FromValue: "To Do", ToValue: "In Progress"}]}]`

## Error Handling
- **404 Not Found** — the issue key/id does not exist.
