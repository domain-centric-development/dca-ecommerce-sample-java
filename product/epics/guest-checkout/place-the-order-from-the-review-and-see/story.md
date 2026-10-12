---
id: place-the-order-from-the-review-and-see
epic: guest-checkout
status: adopted
context: checkout
feature: ordering
title: Place the order from the review and see the confirmation
depends_on: [choose-how-to-pay]
---

# Place the order from the review and see the confirmation

## Story

As a guest at the end of the checkout I want to see everything I entered and what the order costs before I
place it, and a confirmation afterwards, so that I know the order is right and that it went through.

## Acceptance criteria

<!-- happy-path: guest-places-the-order -->

In every scenario the guest started the checkout with "Domain-Driven Design" once, submitted the buyer step
with "jane@example.com", "Jane", "Doe", "+49 30 1234567", the delivery step with "Hauptstrasse 1", "Berlin",
"10115", "Germany" and "Standard Shipping", and chose "Mock Payment (Test)".

### Rule: The review shows what the guest entered

#### review-shows-buyer-delivery-and-payment
- Given the guest has chosen the payment provider
- When the guest opens the review step
- Then the section "Buyer Information" shows "Jane Doe", "jane@example.com" and "+49 30 1234567"
- And the section "Delivery" shows under "Shipping Address" "Hauptstrasse 1", "Berlin, 10115" and "Germany",
  and under "Shipping Method" "Standard Shipping", "Estimated Delivery: 5-7 business days" and
  "Cost: 4.99 EUR"
- And the section "Payment Method" names the chosen payment provider

### Rule: The review shows the items and the totals, with the 19 % VAT contained in the total

#### review-shows-items-and-totals
- Given the guest has chosen the payment provider
- When the guest opens the review step
- Then "Order Items" shows "Domain-Driven Design x1 54.99 EUR"
- And the totals read Subtotal "54.99 EUR", Shipping "4.99 EUR", Tax "9.58 EUR" and Total "59.98 EUR"
- And a "Place Order" button

### Rule: Placing the order confirms it and opens the confirmation

#### guest-places-the-order
- Given the guest is on the review step
- When the guest presses "Place Order"
- Then the confirmation page opens at `/checkout/confirmation`
- And it reads "Your order has been placed successfully"

#### confirmation-shows-what-was-ordered
- Given the guest is on the review step
- When the guest presses "Place Order"
- Then the confirmation page shows under "Contact Information" "Jane Doe", "jane@example.com" and
  "+49 30 1234567"
- And under "Delivery Address" "Hauptstrasse 1", "Berlin, 10115" and "Germany"
- And under "Shipping Method" "Standard Shipping" and "Estimated Delivery: 5-7 business days"
- And under "Payment Method" the chosen payment provider
- And under "Items Ordered" "Domain-Driven Design x1 54.99 EUR"
- And the amounts Subtotal "54.99 EUR", Shipping "4.99 EUR" and Tax "9.58 EUR"

#### continue-shopping-leads-home
- Given the guest has placed the order and sees the confirmation page
- When the guest follows "Continue Shopping"
- Then the home page `/` opens

## Out of scope

- The progress bar on the review and the confirmation — `the-steps-show-the-progress`.
- The "Edit" links and the "Back" link on the review — `go-back-to-an-earlier-step-and-change-it`.
- Reopening the confirmation later, and the step pages after the order — `without-an-open-checkout-the-steps-lead`.
- What the order does to the cart and to the stock — `after-the-order-the-cart-keeps-only`, `stock-goes-down-by-what-was-bought`.
- A "Place Order" refused because stock ran short — `starting-and-placing-check-the-cart`.
- The same checkout for a logged-in customer — `shop-with-an-account`.

## Notes

- Replay story: `CHK-05` (guest-checkout).
- Shared scenario: `scenario.checkout.guest-completes`.
- Inventory: checkout 26 (guest), 31 (tax), 37, 39, 42.
