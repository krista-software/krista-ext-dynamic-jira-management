# Add Comment

## Overview
Adds a comment to a Jira issue.

## Request Details
- **Area**: Comments
- **Type**: CHANGE_SYSTEM
- **External API**: `POST /rest/api/3/issue/{issueIdOrKey}/comment`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |
| Comment Text | Paragraph | Yes | Converted to Jira's Atlassian Document Format automatically. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Comment | Entity(Jira Comment) | The created comment, confirmed by Jira. |

## Example
**Input**: `Issue Id Or Key="KE-452"`, `Comment Text="Reproduced on staging."`
**Output**: `Comment={Author Display Name: "Jane Doe", Body Text: "Reproduced on staging.", ...}`

## Error Handling
- **404 Not Found** — the issue key/id does not exist.
- **403 Permission Denied** — this Jira account cannot comment on the issue (project permissions).
