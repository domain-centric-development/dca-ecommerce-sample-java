---
id: each-listener-counts-its-own-attempts
epic: run-the-shop
status: adopted
context: backoffice
feature: eventlog
title: Each listener's entry counts its own delivery attempts
depends_on: []
---

# Each listener's entry counts its own delivery attempts

## Story

As the operator I want each listener's entry in the event log to count the attempts of that listener's delivery
alone, so that the number tells me how often that one listener failed and not how busy the event was.

## Acceptance criteria

### Rule: An entry's attempts are the tries of its own listener, the successful one included

#### each-listener-counts-its-own-attempts (happy path)
- Given the operator is logged in and an event with two listeners, the first of which failed twice and the second
  once before each handled it
- When they expand the "Event Payload" of the event's two entries
- Then the first listener's entry reads "Status: COMPLETED, attempts: 3" on its first line
- And the second listener's entry reads "Status: COMPLETED, attempts: 2"

#### a-failing-listener-leaves-the-others-count-alone
- Given the operator is logged in and an event with two listeners, the first of which fails on every delivery and
  the second handled it at the first try
- When they expand the "Event Payload" of the event's two entries after the automatic retries have stopped
- Then the first listener's entry reads "Status: FAILED, attempts: 5" on its first line
- And the second listener's entry reads "Status: COMPLETED, attempts: 1"

## Notes

- Decided by the user 2026-10-14 (triage Gruppe 5): `attempts` counts per listener; the glossary's *Attempt* says
  so. Both shops already count so: this story is adopted, not built — the scenarios come into the spec with the
  tests that prove them.
