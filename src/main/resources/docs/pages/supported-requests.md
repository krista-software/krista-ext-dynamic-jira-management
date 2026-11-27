# Supported Requests

Dynamic Jira Management exposes **24 catalog requests** across **11 areas**. Each links to its detailed page.

## Setup
| Request | Type | Description |
|---|---|---|
| [Test Connection](pages/test-connection.md) | QUERY_SYSTEM | Validate connectivity and authentication against Jira Cloud. |
| [Health Check](pages/health-check.md) | CHANGE_SYSTEM | Check extension and Jira connectivity health. |

## Current User
| Request | Type | Description |
|---|---|---|
| [Get Authenticated User](pages/get-authenticated-user.md) | QUERY_SYSTEM | Return the Jira user for the current connection or invoking end-user. |

## Projects
| Request | Type | Description |
|---|---|---|
| [List Projects](pages/list-projects.md) | QUERY_SYSTEM | List all Jira projects accessible to the authenticated user. |
| [Get Project Details](pages/get-project-details.md) | QUERY_SYSTEM | Fetch full details for one Jira project. |
| [Get Project Statuses](pages/get-project-statuses.md) | QUERY_SYSTEM | List the distinct workflow statuses for a project. |

## Issue Types & Schema
| Request | Type | Description |
|---|---|---|
| [Get Issue Types For Project](pages/get-issue-types-for-project.md) | QUERY_SYSTEM | Discover issue types a project allows creating. |
| [Get Field Schema For Issue Type](pages/get-field-schema-for-issue-type.md) | QUERY_SYSTEM | Discover the full, cached field schema for a project + issue type. |
| [List All Fields](pages/list-all-fields.md) | QUERY_SYSTEM | List every system and custom field on the site. |
| [Get Field Contexts](pages/get-field-contexts.md) | QUERY_SYSTEM | Discover a custom field's configuration contexts. |
| [Get Field Context Allowed Values](pages/get-field-context-allowed-values.md) | QUERY_SYSTEM | Discover a select-type custom field's option list. |

## Issues
| Request | Type | Description |
|---|---|---|
| [Create Issue](pages/create-issue.md) | CHANGE_SYSTEM | Create a ticket once all discovered mandatory fields are present. |
| [Get Issue Details](pages/get-issue-details.md) | QUERY_SYSTEM | Fetch full issue details including custom fields. |
| [Update Issue](pages/update-issue.md) | CHANGE_SYSTEM | Update only the fields explicitly supplied. |

## Comments
| Request | Type | Description |
|---|---|---|
| [Get Comments](pages/get-comments.md) | QUERY_SYSTEM | Retrieve comments, with ordering and pagination. |
| [Add Comment](pages/add-comment.md) | CHANGE_SYSTEM | Add a comment to an issue. |

## Workflow
| Request | Type | Description |
|---|---|---|
| [Get Available Transitions](pages/get-available-transitions.md) | QUERY_SYSTEM | Discover valid workflow transitions from the current status. |
| [Change Ticket Status](pages/change-ticket-status.md) | CHANGE_SYSTEM | Execute a validated transition and verify the resulting status. |

## Assignment
| Request | Type | Description |
|---|---|---|
| [Assign Ticket](pages/assign-ticket.md) | CHANGE_SYSTEM | Assign, reassign, or unassign an issue. |
| [Find Assignable Users](pages/find-assignable-users.md) | QUERY_SYSTEM | Find users permitted to be assigned. |

## Search
| Request | Type | Description |
|---|---|---|
| [Search Tickets](pages/search-tickets.md) | QUERY_SYSTEM | JQL search with combinable filters, sorting, and pagination. |

## History
| Request | Type | Description |
|---|---|---|
| [Get Ticket Changelog](pages/get-ticket-changelog.md) | QUERY_SYSTEM | Retrieve field-level change history. |

## Reference Data
| Request | Type | Description |
|---|---|---|
| [List Priorities](pages/list-priorities.md) | QUERY_SYSTEM | List the site's configured priority levels. |
| [List Resolutions](pages/list-resolutions.md) | QUERY_SYSTEM | List the site's configured resolutions. |
