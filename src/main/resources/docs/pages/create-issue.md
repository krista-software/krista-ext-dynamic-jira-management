# Create Issue

## Overview
Creates a Jira ticket once every mandatory field discovered for the selected project and issue type is
present and validated — the schema-driven alternative to a fixed ticket form. Well-known fields are
typed inputs; anything else the discovered schema requires (including every custom field) goes through
**Additional Fields**.

## Request Details
- **Area**: Issues
- **Type**: CHANGE_SYSTEM
- **External API**: `POST /rest/api/3/issue`, then `GET /rest/api/3/issue/{key}` to verify creation
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | Yes | |
| Issue Type Id | Text | Yes | From [Get Issue Types For Project](pages/get-issue-types-for-project.md). |
| Summary | Text | Yes | |
| Description | Paragraph | No | Converted to Jira's Atlassian Document Format automatically. |
| Priority Id | Text | No | From [List Priorities](pages/list-priorities.md). |
| Assignee Account Id | Text | No | From [Find Assignable Users](pages/find-assignable-users.md) or [Get Authenticated User](pages/get-authenticated-user.md). |
| Labels | [ Text ] | No | |
| Component Ids | [ Text ] | No | |
| Due Date | Date | No | |
| Environment | Paragraph | No | Jira's standard Environment field (common on Bug issue types). |
| Additional Fields | FreeForm | No | `fieldId -> value` for any other field the discovered schema needs. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issue | Entity(Jira Issue) | The created issue, including its Key and Url. |

## Example
**Input**: `Project Key Or Id="KE"`, `Issue Type Id="10004"`, `Summary="Checkout button unresponsive"`,
`Environment="Chrome 128 on macOS"`
**Output**: `Issue={Key: "KE-452", Url: "https://your-company.atlassian.net/browse/KE-452", ...}`

## Error Handling
- **Missing mandatory field** — "The following fields are mandatory for this project and issue type but
  were not provided: Environment. Please supply them and retry." The ticket is never submitted partially.
- **404 Not Found** — the project key or issue type id does not exist.
- **400/422 Jira validation error** — a supplied value doesn't match the field's discovered schema
  (e.g. an invalid priority id or option value).

## Technical Implementation
Validates discovered mandatory fields against which well-known inputs were actually supplied (not just
declared) and which `fieldId`s appear in Additional Fields, before ever calling Jira's create endpoint —
see [Get Field Schema For Issue Type](pages/get-field-schema-for-issue-type.md) for the schema this
validates against.
