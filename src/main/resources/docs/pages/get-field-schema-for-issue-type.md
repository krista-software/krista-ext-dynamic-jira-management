# Get Field Schema For Issue Type

## Overview
Discovers the full field schema — mandatory/optional status, data type, default, allowed values, and
custom fields — for one project + issue type combination. This is the foundation of dynamic ticket
creation: two different project/issue-type combinations return two different schemas, nothing is
hardcoded.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/issue/createmeta/{projectIdOrKey}/issuetypes/{issueTypeId}`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | Yes | |
| Issue Type Id | Text | Yes | From [Get Issue Types For Project](pages/get-issue-types-for-project.md). |
| Force Refresh | Boolean | No | Bypasses the schema cache and re-fetches from Jira. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Fields | [ Entity(Jira Field Schema) ] | |
| From Cache | Boolean | True if this result came from the schema cache rather than a fresh Jira call. |

See [Entities](pages/entities.md) for the Jira Field Schema field list.

## Example
**Input**: `Project Key Or Id="KE"`, `Issue Type Id="10004"`
**Output**: `Fields=[{Field Id: "summary", Required: true, ...}, {Field Id: "customfield_10050", Name: "Environment", Required: true, ...}]`

## Error Handling
- **404 Not Found** — the project or issue type id does not exist or is not accessible.

## Technical Implementation
Cached by Project Key + Issue Type Id for `Schema Cache Minutes` (Setup tab, default 30); `Force
Refresh=true` or a cache miss re-fetches from Jira and refreshes the cache entry (requirement section
6 — "Schema Caching and Refresh"). [Create Issue](pages/create-issue.md) and
[Update Issue](pages/update-issue.md) read from the SAME cache, so a schema just fetched here is what
validation uses next.
