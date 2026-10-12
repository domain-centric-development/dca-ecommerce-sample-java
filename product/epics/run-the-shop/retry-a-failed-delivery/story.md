---
id: retry-a-failed-delivery
epic: run-the-shop
status: adopted
context: backoffice
feature: redelivery
title: Retry a failed delivery
depends_on: [failed-deliveries-are-retried-on-their]
---

# Retry a failed delivery

## Story

As an operator I want to retry a delivery the shop gave up on, once its cause is fixed, so that the event
reaches its listener after all and nobody has to repeat what they did in the shop.

## Acceptance criteria

<!-- happy-path: retry-completes-a-failed-delivery -->

### Rule: A failed entry offers a retry

#### failed-entry-offers-a-retry
- Given the operator is logged in and a delivery has used up its five attempts ("Status: FAILED, attempts: 5")
- When they open `/backoffice/events`
- Then that entry shows the button "Retry failed listener"

### Rule: A retry delivers the event once more to that one listener

#### retry-completes-a-failed-delivery
- Given the operator is logged in, a delivery has used up its five attempts and its listener handles the event again
- When the operator presses "Retry failed listener" on that entry
- Then `/backoffice/events` opens again, and once the delivery has run the entry shows the badge "Completed" and a "Completed" date
- And "Completed" has grown by one and "Incomplete" has shrunk by one

#### retry-delivers-the-original-event
- Given the operator retried a failed delivery and it completed
- When they expand that entry's "Event Payload"
- Then the event's content is the same as before the retry, with the same eventId

#### retry-leaves-other-listeners-alone
- Given an event with two listeners, one delivery "Completed" and the other failed for good, and the operator logged in
- When the operator presses "Retry failed listener" on the failed entry
- Then the other listener's entry keeps its "Completed" date unchanged

#### failing-retry-leaves-the-entry-failed
- Given the operator is logged in and a delivery has used up its five attempts and its listener still fails
- When the operator presses "Retry failed listener" on that entry
- Then `/backoffice/events` opens again and the entry still shows the badge "Incomplete"

## Out of scope

- Automatic retries — `failed-deliveries-are-retried-on-their`.
- Entries whose delivery has not failed: there is nothing to retry.

## Assumptions

- answered: How does a delivery fail, when nothing reachable from outside makes one fail? — The test arranges it,
  with a listener that fails; the shop offers no way of its own (user, 2026-09-25).

## Notes

- Replay story: `OPS-06` (run-the-shop).
- Inventory: backoffice B10.
