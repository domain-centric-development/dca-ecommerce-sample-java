---
id: only-a-failed-delivery-offers-a-retry
epic: run-the-shop
status: draft
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

- Every incomplete entry offers "Retry failed listener" now, also one whose delivery is still under way, and pressing
  it there changes nothing and says nothing; after, only an entry whose delivery failed offers it.

## Assumptions

- open: an entry that failed once and waits for its automatic retry — offer "Retry failed listener" (it can be
  retried), or not until the automatic retries are used up (`failed-entry-offers-a-retry` names five attempts)?
- open: the scenario already holds in one of the two shops (it offers the retry for failed entries only) — adopt it
  there, and build it only where it does not hold?

## Notes

- From TODO #94 (f), checked 2026-10-15: still true in the Java shop (the button shows on every incomplete entry, the
  retry acts only on a failed one and silently returns to the log otherwise); fixed in the .NET shop.
- Spec (`backoffice/redelivery`): adds `a-delivery-under-way-offers-no-retry`; holds `failed-entry-offers-a-retry` and
  `retry-completes-a-failed-delivery`.
