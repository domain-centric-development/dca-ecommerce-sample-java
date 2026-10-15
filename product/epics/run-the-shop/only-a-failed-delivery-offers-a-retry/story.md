---
id: only-a-failed-delivery-offers-a-retry
epic: run-the-shop
status: delivered
delivered: 2026-10-15T17:13:34Z
delivered_by: gate
context: backoffice
feature: redelivery
title: Only a failed delivery offers a retry
depends_on: []
---

# Only a failed delivery offers a retry

## Story

As an operator I want the event log to offer a retry only where a retry does something, so that pressing it never
leaves me guessing whether anything happened.

## Acceptance criteria

### Rule: An entry whose delivery has not failed offers no retry

#### a-delivery-under-way-offers-no-retry (happy path)
- Given the operator is logged in and an event's delivery to a listener is still under way and has not failed
- When they open `/backoffice/events`
- Then that entry shows the badge "Incomplete"
- And it shows no button "Retry failed listener"

## Changed expectations

- Every incomplete entry offers "Retry failed listener" now, where the shop shows it so, also one whose delivery is
  still under way, and pressing it there changes nothing and says nothing; after, only an entry whose delivery failed
  offers it.

## Assumptions

- answered: "Retry failed listener" shows only on a failed delivery, as one shop already has it; an entry that waits
  for its automatic retry has not failed and offers none (planning/questions-2026-10-15.md Q4.5)
- answered: adopted in the shop where the scenario already holds, built in the other (planning/questions-2026-10-15.md
  Q4.1)

## Notes

- From TODO #94 (f), checked 2026-10-15: still true in the Java shop (the button shows on every incomplete entry, the
  retry acts only on a failed one and silently returns to the log otherwise); fixed in the .NET shop. Hence `approved`
  in the Java shop and `adopted` in the .NET shop.
- Spec (`backoffice/redelivery`): adds `a-delivery-under-way-offers-no-retry`; holds `failed-entry-offers-a-retry` and
  `retry-completes-a-failed-delivery`.
