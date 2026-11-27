# Get Comments

## Overview
Retrieves comments for an issue, with ordering and a max-results cap (e.g. "the latest 5 comments")
plus pagination.

## Request Details
- **Area**: Comments
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/issue/{issueIdOrKey}/comment`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |
| Max Results | Number | No | Defaults to 50; set low for "latest N comments". |
| Start At | Number | No | Defaults to 0. |
| Order By | PickOne (Oldest First / Newest First) | No | Defaults to Oldest First. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Comments | [ Entity(Jira Comment) ] | |
| Total | Number | Total comment count on the issue (not just this page). |

## Example
**Input**: `Issue Id Or Key="KE-452"`, `Max Results=5`, `Order By="Newest First"`
**Output**: `Comments=[{Author Display Name: "Jane Doe", Body Text: "Reproduced on staging.", ...}]`, `Total=12`

## Error Handling
- **404 Not Found** — the issue key/id does not exist.
