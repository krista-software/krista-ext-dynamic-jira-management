# Release Notes

- **Developer Name** : Krista Extension Builder
- **Krista Service APIs Java** : 1.0.126
- **Global Catalog Version** : Not Available

## Version 1.0.0 (November 2026)

Initial release — dynamic, schema-driven Jira ticket management.

### New Features
* Dynamic project + issue-type schema discovery (Get Issue Types For Project, Get Field Schema For
  Issue Type, List All Fields, Get Field Contexts, Get Field Context Allowed Values), cached by
  Project Key + Issue Type with a configurable TTL and an explicit Force Refresh override.
* Create/Update/Get Issue with schema-driven mandatory-field validation — missing mandatory fields are
  reported by name instead of a partial ticket being submitted.
* Comment management (add, retrieve with ordering/pagination).
* Workflow management (discover + execute validated transitions; status is never written directly).
* Assignment management (assign/reassign/unassign, find assignable users).
* JQL-based search with combinable filters, sorting, and pagination, plus entity search over Jira Issue
  for KME/Studio.
* Dual authentication: API Token (Basic auth) or OAuth 2.0 (3-legged), selectable per invoker.

### Resolved Bugs
* Not Available

### Dependency Updates
* Not Available

### Known Issues
* Jira Cloud's enhanced JQL search endpoint paginates by opaque token, not a numeric offset — Search
  Tickets' `Page Token` field and the generic entity-search store honor this; the entity-search store
  returns an empty page rather than silently repeating page 1 for any `pageIndex > 1`.

### Limitations and Caveats
* Requires a Jira Cloud site (REST API v3) — Jira Server/Data Center is not supported.
* OAuth 2.0 requires an admin-registered Atlassian OAuth 2.0 (3LO) app (no Krista-managed shared app is
  available for Jira today).
