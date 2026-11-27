# Change Ticket Status

## Overview
Executes a workflow transition by id and verifies the resulting status before reporting success.
Status is never written directly to a field — only via a transition discovered from
[Get Available Transitions](pages/get-available-transitions.md).

## Request Details
- **Area**: Workflow
- **Type**: CHANGE_SYSTEM
- **External API**: `POST /rest/api/3/issue/{issueIdOrKey}/transitions`, then `GET /rest/api/3/issue/{issueIdOrKey}` to verify
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |
| Transition Id | Text | Yes | From [Get Available Transitions](pages/get-available-transitions.md). |
| Comment | Paragraph | No | Added to the issue as part of the transition. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue | Entity(Jira Issue) | Re-fetched after the transition, so Status Name reflects the actual result. |

## Example
**Input**: `Issue Id Or Key="KE-452"`, `Transition Id="21"`
**Output**: `Issue={Key: "KE-452", Status Name: "Code Review", ...}`

## Error Handling
- **Invalid transition id** — "Transition id '…' is not valid from this issue's current status. Call
  Get Available Transitions to see valid options." (checked before calling Jira).
- **404 Not Found** — the issue key/id does not exist.
