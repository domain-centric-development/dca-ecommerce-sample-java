---
id: run-the-shop
title: Run the shop
intent: When something goes wrong behind the scenes, operators learn about it from complaints and cannot put it right
goal: An operator sees what the shop did and when, and gets a failed delivery through by retrying it
metric: a failed delivery succeeds on retry
domain_contact: operator
---

# Run the shop

## Intent

When something goes wrong behind the scenes, operators learn about it from complaints and cannot put it right.

## Goal

An operator sees what the shop did and when, and gets a failed delivery through by retrying it.

## Outcome

The epic is measured by an observable outcome, not an event: **a failed delivery succeeds on retry**. The shop publishes no event for it today, and the replay reproduces today's shop, so none is added. As long as this does not happen in production, the epic has not delivered, however much code exists.

## Journey

An operator logs in to the back office, finds an event whose delivery failed, retries it and sees it completed. A break here leaves a failed delivery failed for good.

## Stories

Delivery order is `depends_on`, ties by id.

- operator-login-to-the-back-office — Operator login to the back office
- the-event-log-lists-every-delivery — The event log lists every delivery
- look-into-an-events-payload — Look into an event's payload
- the-back-office-stays-closed-to-others — The back office stays closed to others
- failed-deliveries-are-retried-on-their — Failed deliveries are retried on their own
- retry-a-failed-delivery — Retry a failed delivery
