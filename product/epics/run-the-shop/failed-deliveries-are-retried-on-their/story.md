---
id: failed-deliveries-are-retried-on-their
epic: run-the-shop
status: adopted
context: backoffice
feature: redelivery
title: Failed deliveries are retried on their own
depends_on: [look-into-an-events-payload]
---

# Failed deliveries are retried on their own

## Story

As an operator I want a delivery that fails to be retried by the shop a few times on its own, and to see in
the log when it gave up, so that passing hiccups heal without me and only real failures wait for me.

## Acceptance criteria

<!-- happy-path: passing-failure-heals-on-its-own -->

### Rule: A failed delivery is retried automatically

#### passing-failure-heals-on-its-own
- Given the operator is logged in and a listener failed once on the delivery of an event and handles it again from then on
- When the operator opens `/backoffice/events` a few seconds later
- Then that event's entry for the listener shows the badge "Completed" and a "Completed" date, without the operator having done anything
- And its payload's first line reads "Status: COMPLETED, attempts: 2"

### Rule: After five attempts the shop gives up and leaves the entry for the operator

#### exhausted-delivery-stays-incomplete
- Given the operator is logged in and a listener fails on every delivery of an event
- When the operator opens `/backoffice/events` after the automatic retries have stopped
- Then that event's entry for the listener shows the badge "Incomplete" and no "Completed" date
- And its payload's first line reads "Status: FAILED, attempts: 5"

#### exhausted-delivery-counts-as-incomplete
- Given the operator is logged in and one delivery has used up its five attempts
- When they open `/backoffice/events`
- Then the entry is counted under "Incomplete" and not under "Completed"

## Out of scope

- Retrying by hand — `retry-a-failed-delivery`.
- The waiting times between the attempts: they are not shown anywhere an operator can see.

## Assumptions

- answered: How does a delivery fail, when nothing reachable from outside makes one fail? — The test arranges it,
  with a listener that fails; the shop offers no way of its own (user, 2026-09-25).

## Notes

- Replay story: `OPS-05` (run-the-shop).
- Inventory: backoffice B13 (attempts, as far as an operator sees them).
