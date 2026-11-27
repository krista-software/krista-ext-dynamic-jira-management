# Get Field Contexts

## Overview
Discovers the configuration context(s) a custom field is scoped to — needed before fetching that
field's allowed values.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/field/{fieldId}/context`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Field Id | Text | Yes | e.g. `customfield_10050`. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Contexts | [ Entity(Jira Field Context) ] | |

## Example
**Input**: `Field Id="customfield_10051"`
**Output**: `Contexts=[{Context Id: "10100", Name: "Default Configuration Scheme"}]`

## Error Handling
- **404 Not Found** — the field id does not exist.
