---
id: the-confirmation-says-only-what-happened
epic: guest-checkout
status: approved
context: checkout
feature: ordering
title: The confirmation says only what the shop has done
depends_on: []
---

# The confirmation says only what the shop has done

## Story

As a customer who has just placed an order I want the confirmation to tell me only what really happened, so that I
do not wait for an email that never comes or believe I have paid when the payment is only authorized.

## Acceptance criteria

### Rule: The confirmation promises no email and no order tracking

#### the-confirmation-promises-no-email (happy path)
- Given the guest "jane@example.com" is on the review step
- When the guest presses "Place Order"
- Then the confirmation page does not say "We've sent a confirmation email"
- And it does not say "You can track your order status using your order reference."

### Rule: The confirmation calls the amount the order's total, not an amount paid

#### the-confirmation-shows-the-order-total
- Given the guest is on the review step with "Domain-Driven Design" once and "Standard Shipping"
- When the guest presses "Place Order"
- Then the confirmation page shows "Order Total" "59.98 EUR"
- And it does not say "Total Paid"

## Changed expectations

- The confirmation says "We've sent a confirmation email to jane@example.com." and "You can track your order status
  using your order reference." now; the shop sends no email and tracks no order. After, neither sentence is there.
- The confirmation labels the amount "Total Paid" now, while the payment is only authorized at the provider; after,
  it reads "Order Total".

## Assumptions

- answered: the email and the tracking sentences go without replacement, and the amount reads "Order Total"
  (planning/questions-2026-10-15.md Q4.5)

## Notes

- From TODO #94 (a) and (b), checked 2026-10-15: still true in both shops — no mail is sent anywhere, and the
  payment is initiated at the payment step and never captured.
- Spec (`checkout/ordering`): adds `the-confirmation-promises-no-email` and `the-confirmation-shows-the-order-total`;
  holds `guest-places-the-order` and `confirmation-shows-what-was-ordered`.
