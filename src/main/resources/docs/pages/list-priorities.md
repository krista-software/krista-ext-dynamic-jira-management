# List Priorities

## Overview
Lists the site's configured priority levels — used to resolve a Priority Name to the Priority Id
[Create Issue](pages/create-issue.md)/[Update Issue](pages/update-issue.md) need.

## Request Details
- **Area**: Reference Data
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/priority/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Priorities | [ Entity(Jira Priority) ] | |

## Example
**Input**: (none)
**Output**: `Priorities=[{Id: "1", Name: "Highest"}, {Id: "3", Name: "Medium"}, {Id: "5", Name: "Lowest"}]`

## Error Handling
- **Authentication failure** — see [Authentication](pages/Authentication.md).
