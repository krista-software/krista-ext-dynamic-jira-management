# Get Issue Types For Project

## Overview
Discovers the issue types a given project allows creating (e.g. Bug, Task, Story) — step two of ticket
creation, after project selection.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/issue/createmeta/{projectIdOrKey}/issuetypes`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | Yes | |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue Types | [ Entity(Jira Issue Type) ] | Only create-eligible issue types for this project. |

## Example
**Input**: `Project Key Or Id="KE"`
**Output**: `Issue Types=[{Name: "Bug", ...}, {Name: "Task", ...}, {Name: "Story", ...}]`

## Error Handling
- **404 Not Found** — the project key/id does not exist or is not accessible.
