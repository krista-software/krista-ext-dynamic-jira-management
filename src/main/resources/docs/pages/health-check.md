# Health Check

## Overview
Reports JVM heap usage and whether the extension can currently reach Jira.

## Request Details
- **Area**: Setup
- **Type**: CHANGE_SYSTEM
- **External API**: `GET /rest/api/3/myself` (as a lightweight connectivity probe)
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
None.

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Is Healthy | Boolean | |
| Heap Used MB | Number | |
| Details | Text | |

## Example
**Input**: (none)
**Output**: `Is Healthy=true`, `Heap Used MB=128.0`, `Details="Jira API reachable."`

## Error Handling
- Connectivity failures are reported as `Is Healthy=false` with the underlying error message in
  **Details**, rather than failing the request outright.
