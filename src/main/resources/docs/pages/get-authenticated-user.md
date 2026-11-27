# Get Authenticated User

## Overview
Returns the Jira user identified by the current connection (operator) or the invoking end-user
(per-user/MCP) — used to auto-populate Reporter and, when requested, Assignee.

## Request Details
- **Area**: Current User
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/myself`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| User | Entity(Jira User) | |

## Example
**Input**: (none)
**Output**: `User={Account Id: "...", Display Name: "Jane Doe", Email: "jane@example.com", Active: true}`

## Error Handling
- **Authentication failure** — see [Authentication](pages/Authentication.md).
