# Find Assignable Users

## Overview
Finds users permitted to be assigned on a project or a specific issue.

## Request Details
- **Area**: Assignment
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/user/assignable/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | One of Project Key Or Id / Issue Id Or Key required | |
| Issue Id Or Key | Text | One of Project Key Or Id / Issue Id Or Key required | |
| Search Text | Text | No | Filters by name/email fragment. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Users | [ Entity(Jira User) ] | |

## Example
**Input**: `Project Key Or Id="KE"`, `Search Text="jane"`
**Output**: `Users=[{Display Name: "Jane Doe", Account Id: "abc123", ...}]`

## Error Handling
- **Neither Project Key Or Id nor Issue Id Or Key supplied** — "At least one of the following is
  required: Project Key Or Id, Issue Id Or Key."
