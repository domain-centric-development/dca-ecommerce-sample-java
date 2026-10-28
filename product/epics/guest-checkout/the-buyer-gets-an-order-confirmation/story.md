---
id: the-buyer-gets-an-order-confirmation
epic: guest-checkout
status: approved
context: checkout
feature: orderconfirmation
title: The buyer gets an order confirmation
created: 2026-10-28T16:17:58Z
depends_on: [place-the-order-from-the-review-and-see]
---

# The buyer gets an order confirmation

## Story

As a guest who placed an order I want a confirmation mail at the address I gave, so that I have a record that the
order went through — and a mail provider that is down for a moment must neither lose that mail nor undo my order.

## Acceptance criteria

<!-- happy-path: the-buyer-gets-the-order-confirmation -->

The mail reaches the mail provider shortly after the order, not at the same moment: within 15 seconds. The mail
provider is the one the technical description names under its integrations; the mail's template is
`order-confirmation`.

### Rule: A placed order sends one order confirmation to the buyer's address

#### the-buyer-gets-the-order-confirmation (happy path)
- Given a guest at the review whose buyer e-mail is "jane@example.com"
- When the guest presses "Place Order"
- Then shortly afterwards the mail provider has received one mail with the template "order-confirmation" to
  "jane@example.com"
- And the mail carries the order total the confirmation page shows

#### one-order-is-confirmed-once
- Given a guest who placed an order with the buyer e-mail "jane@example.com"
- When the delivery of that order's confirmation is repeated
- Then the mail provider has received the order-confirmation mail for that order once, under the same key

### Rule: An unavailable mail provider delays the confirmation, it does not lose it

#### the-confirmation-follows-once-the-mail-provider-is-back
- Given the mail provider is unavailable
- And a guest with the buyer e-mail "jane@example.com" has placed an order
- When the mail provider answers again
- Then shortly afterwards exactly one order-confirmation mail to "jane@example.com" reaches it

## Out of scope

- The mail's wording and layout beyond its template, recipient and total — the mail provider renders it.
- Mails about a registered customer's account (registration, password).
- A page that lists the mails the shop sent.

## Assumptions

- answered: the operation is "send the order confirmation" (`sendorderconfirmation`); the feature is "order
  confirmation" (`orderconfirmation`) — informing the buyer that the order went through. Decided 2026-10-28.
- answered: the mail is sent for every confirmed order, guest or registered, to the buyer e-mail of the checkout.
  Decided 2026-10-28.

## Notes

- Planning: WP-158 proof case.
- Guarantee, not a criterion (it holds today, with no mail at all): an unavailable mail provider does not undo the
  order — the confirmation page shows it as placed and the cart keeps only what was not bought. The plan keeps it
  as a guard.
