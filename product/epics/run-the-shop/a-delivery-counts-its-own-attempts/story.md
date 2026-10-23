---
id: a-delivery-counts-its-own-attempts
epic: run-the-shop
status: draft
created: 2026-10-23T21:25:20Z
context: backoffice
title: A delivery counts its own attempts
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# A delivery counts its own attempts

## Story

As an operator I want each delivery's attempts counted for that delivery, so that two consumers failing once do not read as three attempts.

## Acceptance criteria

### Rule: Attempts are counted per delivery

#### two-consumers-failing-once-read-two-attempts-each (happy path)
- Given an event delivered to two consumers, each failing once and then succeeding
- When an operator opens the event log
- Then each delivery reads `attempts: 2`

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- Java counts per listener; .NET adds every consumer's failures to one publication.
