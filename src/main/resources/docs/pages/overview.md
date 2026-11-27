# Overview

Dynamic Jira Management connects Krista to Jira Cloud via its REST API v3. Use it to create, update,
comment on, transition, assign, and search Jira tickets directly from Krista workflows and agents —
across any number of projects, each with its own fields, issue types, and workflow.

## The core idea: nothing about a project's schema is hardcoded

Jira projects differ — one project's "Bug" issue type might require an Environment field and a
Component, while another project's "Task" has neither. Instead of assuming one fixed set of fields,
this extension discovers the real schema from Jira itself every time:

1. Pick a project ([List Projects](pages/list-projects.md)).
2. Pick an issue type ([Get Issue Types For Project](pages/get-issue-types-for-project.md)).
3. Discover that combination's actual field schema — mandatory/optional, data type, defaults, allowed
   values, custom fields — via [Get Field Schema For Issue Type](pages/get-field-schema-for-issue-type.md).
4. [Create Issue](pages/create-issue.md) auto-populates what it can (summary/description from your
   request, reporter from your Jira identity, defaults from Jira itself) and only asks for whatever
   mandatory field is still missing.

## Use cases

- **Create tickets with the right fields for the right project** — no hardcoded ticket schema.
- **Update, comment, transition, assign, and search** existing tickets across every project.
- **Discover custom fields** per project/issue type without code changes when Jira configuration changes.

## What you can do

The extension exposes 24 requests across 11 areas — see
[Supported Requests](pages/supported-requests.md) for the full list, or the sidebar for each request's
detailed page.

## Before you start

1. Obtain credentials — see [Authentication](pages/Authentication.md).
2. Configure the Setup tab — see [Configuration](pages/ExtensionConfiguration.md).
3. Run **Test Connection** (Setup area) to verify connectivity.
