# Assign Ticket

## Overview
Assigns, reassigns, or unassigns a Jira issue.

## Request Details
- **Area**: Assignment
- **Type**: CHANGE_SYSTEM
- **External API**: `PUT /rest/api/3/issue/{issueIdOrKey}/assignee`, then `GET /rest/api/3/issue/{issueIdOrKey}` to verify
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |
| Assignee Account Id | Text | Required unless Unassign=true | From [Find Assignable Users](pages/find-assignable-users.md) or [Get Authenticated User](pages/get-authenticated-user.md). |
| Unassign | Boolean | No | When true, removes the current assignee instead. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue | Entity(Jira Issue) | Re-fetched after the assignment change. |

## Example
**Input**: `Issue Id Or Key="KE-452"`, `Assignee Account Id="abc123"`
**Output**: `Issue={Key: "KE-452", Assignee: {Display Name: "Jane Doe", ...}, ...}`

## Error Handling
- **Missing Assignee Account Id** — "Assignee Account Id (or set Unassign = true) is required…".
- **403 Permission Denied** — the target user cannot be assigned to this project (Jira permissions).
- **404 Not Found** — the issue key/id does not exist.
