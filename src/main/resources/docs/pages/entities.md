# Entities

Data structures returned by the requests. Request output tables link here instead of repeating the
full field list.

## Jira User
A Jira Cloud user account — an issue's reporter or assignee, the authenticated user, a comment's
author, or a candidate returned by assignable-user search.

| Field | Type | Description |
|---|---|---|
| Account Id | Text | Jira's immutable account identifier. |
| Display Name | Text | |
| Email | Email | |
| Active | Boolean | |
| Account Type | Text | e.g. atlassian, app, customer. |

## Jira Project
A Jira Cloud project — the unit of schema discovery.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Key | Text | The project key, e.g. KE. |
| Name | Text | |
| Project Type Key | Text | e.g. software, business, service_desk. |
| Style | Text | classic or next-gen (team-managed). |
| Lead | `{ AccountId, DisplayName, Email }` | The project lead. References Jira User. |

## Jira Issue Type
An issue type create-eligible for a specific Jira project (e.g. Bug, Task, Story).

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Name | Text | |
| Description | RichText | |
| Subtask | Boolean | |
| Icon Url | Text | |

## Jira Field Schema
Discovered field metadata for one field on a specific project + issue type's create screen — the core
of dynamic schema discovery.

| Field | Type | Description |
|---|---|---|
| Field Id | Text | e.g. summary, priority, customfield_10050. |
| Name | Text | |
| Required | Boolean | True = mandatory for this project + issue type. |
| Data Type | Text | e.g. string, number, array, option, user. |
| Custom | Boolean | |
| Has Default Value | Boolean | |
| Default Value | FreeForm | Shape depends on Data Type. |
| Allowed Values | `[ { Id, Value, Disabled } ]` | The admin-configured option list, when select-type. |
| Description | Paragraph | |

## Jira Field Definition
One entry from Jira's site-wide field catalog (system or custom), independent of any one project.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Key | Text | |
| Name | Text | |
| Custom | Boolean | |
| Data Type | Text | |

## Jira Field Context
A custom field's configuration context. References Jira Field Definition.

| Field | Type | Description |
|---|---|---|
| Context Id | Text | |
| Name | Text | |
| Description | Paragraph | |

## Jira Field Allowed Value
One admin-configured option for a select-type custom field context. References Jira Field Context.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Value | Text | |
| Disabled | Boolean | |

## Jira Status
A Jira workflow status, scoped to one project's issue type.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Name | Text | |
| Status Category | Text | e.g. To Do, In Progress, Done. |

## Jira Priority
A site-wide Jira priority level.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Name | Text | |

## Jira Resolution
A site-wide Jira resolution.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Name | Text | |

## Jira Transition
A workflow transition available from an issue's current status. References Jira Status.

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Name | Text | |
| To Status Name | Text | |
| Has Screen | Boolean | True if executing this transition requires additional fields. |

## Jira Issue
The central domain object — a Jira issue/ticket. `supportStore = true`: this is the one entity-search-
enabled entity (see [Search Tickets](pages/search-tickets.md)). References Jira User (Reporter, Assignee).

| Field | Type | Description |
|---|---|---|
| Id | Text | Jira's immutable numeric issue id (primary key). |
| Key | Text | The display issue key, e.g. KE-123. |
| Url | Text | Direct browser link to the issue. |
| Summary | Text | |
| Description | RichText | |
| Project Key | Text | |
| Issue Type Name | Text | |
| Status Name | Text | |
| Priority Name | Text | |
| Reporter | `{ AccountId, DisplayName, Email }` | References Jira User. |
| Assignee | `{ AccountId, DisplayName, Email }` | References Jira User. |
| Labels | `[ Text ]` | |
| Component Names | `[ Text ]` | |
| Created | Date | |
| Updated | Date | |
| Due Date | Date | |
| Resolution Name | Text | |
| Custom Fields | FreeForm | Every project-specific custom field, keyed by field id. |

## Jira Comment
A comment on a Jira issue. References Jira User (author).

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Author Display Name | Text | |
| Author Account Id | Text | |
| Body Text | RichText | |
| Created | Date | |
| Updated | Date | |

## Jira Changelog Entry
One history entry on a Jira issue — the field-level changes made in a single edit. References Jira User
(author).

| Field | Type | Description |
|---|---|---|
| Id | Text | |
| Author Display Name | Text | |
| Created | Date | |
| Changes | `[ { Field, FromValue, ToValue } ]` | |
