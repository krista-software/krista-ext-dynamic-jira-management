# Search Tickets

## Overview
Searches Jira issues across projects with combinable filters, sorting, and pagination — e.g. "unresolved
bugs in project KE", "high-priority tickets assigned to me", "tickets created in the last seven days".

## Request Details
- **Area**: Search
- **Type**: QUERY_SYSTEM
- **External API**: `POST /rest/api/3/search/jql`
- **Ask Agent (MCP)**: Enabled (callable by the Krista AI agent)

## Input Parameters
| Parameter | Type | Required | Description |
|---|---|---|---|
| Project Key Or Id | Text | No | |
| Issue Key | Text | No | |
| Status | Text | No | |
| Unresolved Only | Boolean | No | Adds `resolution = Unresolved`. |
| Reporter Account Id | Text | No | Ignored if Reported By Me is true. |
| Reported By Me | Boolean | No | Uses Jira's `currentUser()`. |
| Assignee Account Id | Text | No | Ignored if Assigned To Me is true. |
| Assigned To Me | Boolean | No | Uses Jira's `currentUser()`. |
| Priority Name | Text | No | |
| Issue Type Name | Text | No | |
| Labels | [ Text ] | No | Matches any (JQL `IN`). |
| Component Names | [ Text ] | No | Matches any (JQL `IN`). |
| Created After | Date | No | |
| Updated After | Date | No | |
| Due Before | Date | No | |
| Resolution Name | Text | No | |
| Free Text Search | Text | No | JQL `text ~` full-text search. |
| Sort By | PickOne (Updated / Created / Priority / Due Date) | No | Defaults to Updated. |
| Sort Direction | PickOne (Descending / Ascending) | No | Defaults to Descending. |
| Max Results | Number | No | Defaults to 50. |
| Page Token | Text | No | From a previous call's **Next Page Token**. |
| Raw JQL | Paragraph | No | Advanced escape hatch — when supplied, used verbatim instead of the structured filters above. |

## Output Parameters
| Parameter | Type | Description |
|---|---|---|
| Issues | [ Entity(Jira Issue) ] | |
| Count | Number | Issues returned in this page. |
| Next Page Token | Text | Pass to **Page Token** to fetch the next page; absent on the last page. |

## Example
**Input**: `Project Key Or Id="KE"`, `Issue Type Name="Bug"`, `Unresolved Only=true`
**Output**: `Issues=[{Key: "KE-452", Status Name: "In Progress", ...}, ...]`, `Count=7`

## Error Handling
- **400 Jira validation error** — an unrecognized status/priority/issue-type name in a filter produces
  an invalid JQL clause; Jira's own error message is surfaced.

## Technical Implementation
Jira Cloud's current `/search/jql` endpoint paginates by opaque `nextPageToken`, not a numeric offset —
always follow **Next Page Token** rather than trying to compute a page number. The same Jira Issue
entity is also searchable through Krista's generic entity-search/KME path (see
[Entities](pages/entities.md)); that path translates Studio/KME search conditions into the same JQL
dialect independently of this request.
