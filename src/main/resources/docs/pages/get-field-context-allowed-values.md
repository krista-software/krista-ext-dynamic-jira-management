# Get Field Context Allowed Values

## Overview
Discovers the admin-configured option list for a single/multi-select custom field context — the same
data [Get Field Schema For Issue Type](pages/get-field-schema-for-issue-type.md) inlines under
**Allowed Values**, available standalone for a field/context you already know the ids of.

## Request Details
- **Area**: Issue Types & Schema
- **Type**: QUERY_SYSTEM
- **External API**: `GET /rest/api/3/field/{fieldId}/context/{contextId}/option`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Field Id | Text | Yes | |
| Context Id | Text | Yes | From [Get Field Contexts](pages/get-field-contexts.md). |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Allowed Values | [ Entity(Jira Field Allowed Value) ] | |

## Example
**Input**: `Field Id="customfield_10051"`, `Context Id="10100"`
**Output**: `Allowed Values=[{Value: "Production"}, {Value: "Staging"}, {Value: "Development"}]`

## Error Handling
- **404 Not Found** — the field id or context id does not exist.
