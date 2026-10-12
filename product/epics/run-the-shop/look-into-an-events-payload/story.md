---
id: look-into-an-events-payload
epic: run-the-shop
status: adopted
context: backoffice
feature: eventlog
title: Look into an event's payload
depends_on: [the-event-log-lists-every-delivery]
---

# Look into an event's payload

## Story

As an operator I want to open an entry and read what the event carried, together with its delivery status and
the number of attempts, so that I can tell what a failed delivery was about.

## Acceptance criteria

<!-- happy-path: payload-can-be-expanded -->

### Rule: Every entry has a collapsible payload

#### payload-is-collapsed-at-first
- Given the operator is logged in and the log lists at least one entry
- When they open `/backoffice/events`
- Then every entry offers "Event Payload", collapsed

#### payload-can-be-expanded
- Given the operator is logged in and the log lists at least one entry
- When they expand "Event Payload" of the first entry
- Then the payload is shown and is not empty

### Rule: The payload starts with a line "Status: <status>, attempts: <number>", followed by the event's content

#### payload-starts-with-status-and-attempts
- Given the operator is logged in and the log lists a "Completed" entry for a product created at start-up, delivered at the first attempt
- When they expand that entry's "Event Payload"
- Then its first line reads "Status: COMPLETED, attempts: 1"
- And the lines after it hold the event's content: eventId, occurredOn, productId, amount, currency and initialStock

## Out of scope

- Retrying the delivery shown in a payload — `failed-deliveries-are-retried-on-their`, `retry-a-failed-delivery`.

## Notes

- Replay story: `OPS-03` (run-the-shop).
- Shared scenario: `scenario.backoffice.event-payload`.
- Inventory: backoffice B7.
