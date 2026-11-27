# Update Issue

## Overview
Updates only the fields explicitly supplied on an existing issue — every other field is left untouched.

## Request Details
- **Area**: Issues
- **Type**: CHANGE_SYSTEM
- **External API**: `PUT /rest/api/3/issue/{issueIdOrKey}`, then `GET /rest/api/3/issue/{issueIdOrKey}` to return the updated issue
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Issue Id Or Key | Text | Yes | |
| Summary | Text | No | |
| Description | Paragraph | No | |
| Priority Id | Text | No | |
| Labels | [ Text ] | No | Replaces the full label set. |
| Component Ids | [ Text ] | No | Replaces the full component set. |
| Due Date | Date | No | |
| Environment | Paragraph | No | |
| Additional Fields | FreeForm | No | `fieldId -> value` for any other field. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue | Entity(Jira Issue) | The updated issue, re-fetched from Jira. |

## Example
**Input**: `Issue Id Or Key="KE-452"`, `Priority Id="2"`
**Output**: `Issue={Key: "KE-452", Priority Name: "High", ...}` (Summary/Description/etc. unchanged)

## Error Handling
- **No fields supplied** — "No fields were supplied to update. Provide at least one field to change."
- **404 Not Found** — the issue key/id does not exist.
- **400/422 Jira validation error** — a supplied value doesn't match the field's discovered schema.
