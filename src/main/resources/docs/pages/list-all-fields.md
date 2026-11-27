# List All Fields

## Overview
Lists every system and custom field defined on this Jira site, independent of any single project.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/field`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Fields | [ Entity(Jira Field Definition) ] | |

## Example
**Input**: (none)
**Output**: `Fields=[{Id: "summary", Name: "Summary", Custom: false}, {Id: "customfield_10050", Name: "Environment", Custom: true}]`

## Error Handling
- **Authentication failure** — see [Authentication](pages/Authentication.md).
