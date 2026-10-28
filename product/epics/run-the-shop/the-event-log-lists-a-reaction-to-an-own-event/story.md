---
id: the-event-log-lists-a-reaction-to-an-own-event
epic: run-the-shop
status: adopted
context: backoffice
feature: eventlog
title: The event log lists a reaction to a context's own event
created: 2026-10-28T20:41:46Z
depends_on: [the-buyer-gets-an-order-confirmation]
---

# The event log lists a reaction to a context's own event

## Story

As an operator I want the event log to list every delivery the shop makes after a commit — also the delivery of a
context's own event to a reaction in that context — so that I see a failed order confirmation and can retry it like
any other delivery.

## Acceptance criteria

### Rule: A delivery of a context's own event to a reaction is an entry like any other

#### the-log-lists-the-order-confirmations-delivery (happy path)
- Given a guest has placed an order, and the operator is logged in
- When they open `/backoffice/events`
- Then the log lists an entry whose type label names the confirmed checkout and whose "Listener" names the order
  confirmation
- And the entry shows the badge "Completed"

## Out of scope

- Domain events that no reaction waits for: they are delivered inside the publishing transaction and leave no entry.

## Notes

- WP-158 follow-up: this shop lists these deliveries from its event publication registry already, so the story is
  adopted here; the .NET shop builds it.
