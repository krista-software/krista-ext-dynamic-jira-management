# List Resolutions

## Overview
Lists the site's configured resolutions (e.g. Done, Won't Fix, Duplicate).

## Request Details
- **Area**: Reference Data
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/resolution/search`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Resolutions | [ Entity(Jira Resolution) ] | |

## Example
**Input**: (none)
**Output**: `Resolutions=[{Id: "1", Name: "Done"}, {Id: "2", Name: "Won't Fix"}]`

## Error Handling
- **Authentication failure** — see [Authentication](pages/Authentication.md).
