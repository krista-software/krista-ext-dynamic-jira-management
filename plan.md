# Dynamic Jira Management — Implementation Plan

## Summary

A Krista Global Catalog extension that manages Jira Cloud tickets across projects via natural-language
instructions, without assuming a fixed ticket schema. Instead of hardcoding mandatory/optional fields per
project, it calls Jira's own metadata APIs (create-metadata, field, field-context) at request time to
discover each project + issue type's actual schema, and uses that discovery to drive field
auto-population, mandatory-field prompting, and validation before every create/update.

- **Extension:** Dynamic Jira Management (`dynamic-jira-management`), v1.0.0
- **Package:** `app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement`
- **Ecosystem / Domain:** ZZZ Test Sandbox / ZZZ Delete Me ⚠️ **placeholder — see below**
- **Endpoints covered:** 22 of 22 user-facing Jira REST v3 endpoints identified in `api-model.json`
  (the 23rd, `getAccessibleResources`, is internal OAuth cloud-ID plumbing used by every request's HTTP
  client, not a standalone catalog request — not a gap).

## Dynamic schema & catalog-entity strategy (the core architectural requirement)

Krista's extension framework compiles `@Entity`/`@Field` shapes ahead of time — it has no mechanism to
mint a brand-new Java class per Jira project at runtime. So "dynamic entity generation" is implemented
as a **schema-driven, not class-generating**, design:

1. **Discovery requests** (`Get Issue Types For Project`, `Get Field Schema For Issue Type`, `List All
   Fields`, `Get Field Contexts`, `Get Field Context Allowed Values`) expose Jira's metadata as the
   typed `JiraFieldSchema` / `JiraFieldAllowedValue` entities — field id, name, data type, required flag,
   default, allowed values, description — so a caller (a human or an agent building the create request)
   can inspect exactly what a given project + issue type needs *before* calling Create/Update.
2. **Create Issue / Update Issue** declare the well-known fields Jira ships on nearly every project as
   typed inputs (summary, description, priority, assignee, labels, components, due date), plus one
   generic escape hatch — `additionalFields: [ Entity(JiraFieldValue) ]` (fieldId + a schema-shaped
   value) — for whatever custom/project-specific fields discovery turned up. This is the one place a
   genuinely-dynamic value legitimately needs a loosely-typed carrier (`JiraFieldValue.value` is
   `FreeForm`), because its real shape depends entirely on the field's own discovered type.
3. **Business-logic phase** (not this phase) owns: caching discovered schema by project key + issue
   type, validating every supplied value (well-known or `additionalFields`) against that schema,
   auto-populating from authenticated-user/defaults/request text per the priority order in the
   requirement, and asking only for mandatory fields still missing. This plan fixes the *shape* of that
   contract; the logic fulfilling it is built in the next phase.

## Authentication

**User's confirmed choice:** `api-key,oauth2-interactive` → both mechanisms must be implemented as
admin-selectable Setup-tab options (same split servicenow uses for `AuthType = Oauth | BasicAuth`, per
`authentication-patterns.md`), not just the first one.

- **Primary pattern recorded in implementation-plan.json: `A`** (API key — first-listed mechanism).
- **Pattern A (API key / token)** — a secured `@Field.Text` for an Atlassian API token (used as Basic
  auth together with the account email, Jira's standard token-auth convention). No browser flow; `TEST
  CONNECTION` calls `GET /rest/api/3/myself`.
- **Pattern C (OAuth 2.0, 3-legged)** — `RequestAuthenticator` + `AuthCallbackResource` + `AttributeStore`
  + scribejava, per the standard interactive flow. Jira Cloud OAuth apps call through
  `https://api.atlassian.com/ex/jira/{cloudId}/...`; `cloudId` is resolved once via `GET
  https://api.atlassian.com/oauth/token/accessible-resources` and cached with the token.
- A `@Field.PickOne(name="AuthType", values={"ApiKey","OAuth"})` on the Setup tab selects which secured
  fields/flow apply, exactly like the servicenow reference.

## Domain reuse

`domain-catalog.json` reports `"found": false` for ZZZ Test Sandbox / ZZZ Delete Me — this
ecosystem/domain has never been published, so there is nothing to match against. Every area, request,
and entity below is net new (`reuse.status = "new"` throughout). Per the skill's own guidance, naming
stays vendor-neutral style (`JiraUser`, `JiraIssue`, `JiraComment`, …) rather than something like
"Dynamic Jira Ticket Object", so a future extension in the same domain can reuse these shapes the same
way this build would have reused anything pre-existing.

## Areas, requests, and entities

### Setup
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Test Connection | — | `Entity(JiraUser)` | `getCurrentUser` | New |

### Current User
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Get Authenticated User | — | `Entity(JiraUser)` | `getCurrentUser` | New |

### Projects
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| List Projects | searchText?, maxResults?, startAt? | `[ Entity(JiraProject) ]` | `getProjects` | New |
| Get Project Details | projectKeyOrId | `Entity(JiraProject)` | `getProject` | New |
| Get Project Statuses | projectKeyOrId | `[ Entity(JiraStatus) ]` | `getProjectStatuses` | New |

### Issue Types & Schema
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Get Issue Types For Project | projectKeyOrId | `[ Entity(JiraIssueType) ]` | `getCreateMetaIssueTypes` | New |
| Get Field Schema For Issue Type | projectKeyOrId, issueTypeId | `[ Entity(JiraFieldSchema) ]` | `getCreateMetaFieldsForIssueType` | New |
| List All Fields | — | `[ Entity(JiraFieldDefinition) ]` | `getFields` | New |
| Get Field Contexts | fieldId | `[ Entity(JiraFieldContext) ]` | `getFieldContexts` | New |
| Get Field Context Allowed Values | fieldId, contextId | `[ Entity(JiraFieldAllowedValue) ]` | `getFieldContextOptions` | New |

### Issues
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Create Issue | projectKeyOrId, issueTypeId, summary, description?, priorityId?, assigneeAccountId?, labels?, componentIds?, dueDate?, additionalFields? | `Entity(JiraIssue)` | `createIssue` | New |
| Get Issue Details | issueIdOrKey, includeChangelog? | `Entity(JiraIssue)` | `getIssue` | New |
| Update Issue | issueIdOrKey, summary?, description?, priorityId?, labels?, componentIds?, dueDate?, additionalFields? | `Entity(JiraIssue)` | `updateIssue` | New |

### Comments
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Get Comments | issueIdOrKey, maxResults?, orderBy? | `[ Entity(JiraComment) ]` | `getComments` | New |
| Add Comment | issueIdOrKey, commentText | `Entity(JiraComment)` | `addComment` | New |

### Workflow
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Get Available Transitions | issueIdOrKey | `[ Entity(JiraTransition) ]` | `getTransitions` | New |
| Change Ticket Status | issueIdOrKey, transitionId, comment? | `Entity(JiraIssue)` | `doTransition` | New |

*Status is never written directly — only via a validated transition id, then the issue is re-fetched to
verify the resulting status, per the requirement.*

### Assignment
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Assign Ticket | issueIdOrKey, assigneeAccountId?, unassign? | `Entity(JiraIssue)` | `assignIssue` | New |
| Find Assignable Users | projectKeyOrId?, issueIdOrKey?, searchText? | `[ Entity(JiraUser) ]` | `findAssignableUsers` | New |

### Search
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Search Tickets | projectKeyOrId?, issueKey?, status?, reporterAccountId?, assigneeAccountId?, priorityId?, issueTypeId?, labels?, componentIds?, createdAfter?, updatedAfter?, dueBefore?, resolutionId?, freeTextSearch?, sortBy?, maxResults?, pageToken? | `[ Entity(JiraIssue) ]` | `searchIssuesJql` | New |

*Classified `QUERY_SYSTEM` despite being a `POST` — JQL search has no side effects; POST is only used
because JQL strings can exceed GET URL-length limits.*

### History
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| Get Ticket Changelog | issueIdOrKey, maxResults? | `[ Entity(JiraChangelogEntry) ]` | `getChangelog` | New |

### Reference Data
| Request | Inputs | Output | Endpoint | Reused? |
|---|---|---|---|---|
| List Priorities | — | `[ Entity(JiraPriority) ]` | `getPriorities` | New |
| List Resolutions | — | `[ Entity(JiraResolution) ]` | `getResolutions` | New |

## Entities (16, all new)

`JiraUser`, `JiraProject`, `JiraIssueType`, `JiraFieldSchema`, `JiraFieldDefinition`, `JiraFieldContext`,
`JiraFieldAllowedValue`, `JiraFieldValue`, `JiraStatus`, `JiraPriority`, `JiraResolution`,
`JiraTransition`, `JiraIssue`, `JiraComment`, `JiraChangelogEntry`, `JiraChangeItem`.

Two fields are deliberately `FreeForm` (justified, not a shortcut):
- `JiraFieldSchema.defaultValue` — shape depends on the field's own discovered data type.
- `JiraFieldValue.value` — the generic custom-field carrier for Create/Update's `additionalFields`;
  shape is set by whichever field schema it targets at call time.

Everything else — including every list of options, every list of change items, every user/project/issue
reference — is a typed `Entity(...)`, not a raw payload dump.

## Capabilities

| Capability | Enabled | Why |
|---|---|---|
| Entity search | ✅ | Jira issues (and projects) are first-class searchable objects; the requirement's whole "Search and List Tickets" section maps directly onto `entity-search-implementation`. |
| Webhooks / WAIT_FOR_EVENT | ❌ | No inbound-notification/event-wait use case was requested. |
| MCP (`tool = true`) | ✅ | The requirement is built around natural-language/agent-driven ticket management; search, create, update, comment, assign, and transition requests are agent-callable. |
| Solutions tab | ❌ | Not requested. |
| File handling | ❌ | No attachment upload/download was requested; out of scope for this build. |

## ⚠️ Placeholders that must be replaced before deploy

- `@Domain` id, `ecosystemId`, and `ecosystemName` for **ZZZ Test Sandbox / ZZZ Delete Me** — this
  ecosystem/domain has no production registration (`domain-catalog.json` confirms `found: false`); stable
  placeholder UUIDs will be minted so the code compiles, but the extension **will not bind** to the real
  Krista catalog until swapped for the actual registered ecosystem/domain IDs (or this is pointed at a
  real ecosystem/domain instead of the sandbox one).
- No other placeholders are anticipated — auth, endpoints, and entities are all derived from
  `api-model.json` / `requirement-spec.json`, not invented.
