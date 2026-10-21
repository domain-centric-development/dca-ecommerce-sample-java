---
id: the-payment-is-authorized-at-place-order
epic: provider-payment
status: approved
created: 2026-10-21T14:31:50Z
context: checkout
feature: payment
title: The payment is authorized at Place Order, for the total the review showed
depends_on: [paying-again-requests-no-second-payment, two-orders-never-sell-more-than-the-stock]
---

# The payment is authorized at Place Order, for the total the review showed

## Story

As a customer I want my payment authorized only when I press "Place Order", for exactly the total the review showed
me, so that the provider never holds an amount I did not order, nor one for an order I never placed.

## Acceptance criteria

### Rule: The payment step chooses how to pay; it requests no payment

#### authorized-payment-moves-to-review
- Given a checkout session at the payment step
- And the payment provider authorizes payments
- When the customer chooses "Mock Payment (Test)" and presses "Continue to Review"
- Then the checkout shows the review step
- And the payment provider received no payment request

### Rule: Place Order has the provider authorize the total the review showed, then confirms the order

#### place-order-authorizes-the-reviewed-total (happy path)
Title: Place Order has the payment provider authorize the total the review showed and opens the confirmation
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", Total "59.98 EUR"
- And the payment provider authorizes payments
- When the guest presses "Place Order"
- Then the confirmation page opens at `/checkout/confirmation` with "Order Total" "59.98 EUR"
- And the payment provider received one payment request, of "59.98"

#### the-payment-request-names-its-checkout
- Given a checkout session at the review
- And the payment provider authorizes payments
- When the customer presses "Place Order"
- Then the payment provider received one payment request for the checkout session's total
- And that request carries the checkout reference, the checkout session's id as text, as `merchantReference`

#### two-checkouts-send-two-references
- Given two customers, each with a checkout session at the review
- When both press "Place Order"
- Then the payment provider received two payment requests with two different `merchantReference` values

### Rule: A refused payment keeps the customer at the review and places no order

#### refused-payment-stays-at-payment
Title: A payment the provider refuses at Place Order keeps the customer at the review
- Given a checkout session at the review
- And the payment provider refuses payments
- When the customer presses "Place Order"
- Then the review stays open at `/checkout/review` with "The payment was refused. Please choose another way to pay."
- And no confirmation is shown

#### a-refused-payment-holds-no-stock
- Given a guest at the review with "Wooden Hexagon Desk Model" twice, whose product page reads "8 units available"
- And the payment provider refuses payments
- When the guest presses "Place Order"
- Then the review stays open at `/checkout/review` with "The payment was refused. Please choose another way to pay."
- And the product page of "Wooden Hexagon Desk Model" still reads "8 units available"

#### the-provider-answers-per-checkout
- Given two customers, each with a checkout session at the review
- And the payment provider refuses the payment for the first customer's checkout reference only
- When both press "Place Order"
- Then the first customer stays at the review with "The payment was refused. Please choose another way to pay."
- And the second customer sees the confirmation

#### a-second-attempt-names-the-same-checkout
- Given a checkout session at the review
- And the customer's first "Place Order" was refused by the payment provider
- When the customer presses "Place Order" again
- Then the payment provider received two payment requests for that checkout session
- And both carry the same `merchantReference`

### Rule: A provider that does not answer in time counts as unavailable

#### slow-provider-counts-as-unavailable
Title: A provider that answers Place Order only after 5 seconds counts as unavailable
- Given a checkout session at the review
- And the payment provider answers only after 5 seconds
- When the customer presses "Place Order"
- Then the review stays open at `/checkout/review` with "The payment provider is not available right now. Please try
  again later."
- And no confirmation is shown

### Rule: An order Place Order refuses before the payment requests no payment

#### place-order-refused-for-stock-requests-no-payment
- Given a guest at the review with "Wooden Hexagon Desk Model" five times
- And another visitor has since bought it five times, leaving 3
- When the guest presses "Place Order"
- Then the review stays open at `/checkout/review` with "Not enough stock left for 'Wooden Hexagon Desk Model'.
  Please adjust your order."
- And the payment provider received no payment request

## Out of scope

- A change since the review (price, another tab): story `an-open-checkout-keeps-its-price`, which depends on this
  one — its refusal requests no payment, and its order after the refusal is authorized at the total then shown.
- A cancellation for an authorization that a later failure leaves without an order: Place Order checks the review and
  the stock before it requests the payment, so no step after it refuses.
- Capture of the payment: unchanged, outside the shop.

## Changed expectations

- Today "Continue to Review" on the payment step sends the payment request and the review opens on an authorized
  payment; after, the payment step only records the chosen provider, and "Place Order" sends the one payment request,
  for the total the review showed, after the order's checks and before it is confirmed.
- Today a refused payment or a provider that does not answer keeps the customer at the payment step with their
  message; after, the review stays open with the same message and no order is placed. The stock the order reserved
  is free again.
- The rule "A payment the provider authorizes moves the checkout on" reads "The payment step chooses how to pay; it
  requests no payment"; "A refused payment keeps the customer at the payment step" reads "A refused payment keeps the
  customer at the review and places no order". The scenarios `the-payment-request-names-its-checkout`,
  `two-checkouts-send-two-references`, `the-provider-answers-per-checkout` and
  `a-second-attempt-names-the-same-checkout` send their requests at "Place Order".
- Paying again from the payment step no longer meets an authorized payment, so the rules "A checkout whose authorized
  payment covers its total keeps that payment" and "A checkout whose total changed after its payment was authorized
  cancels that payment and requests a new one" go with their scenarios
  `paying-again-for-the-same-amount-keeps-the-payment` and `paying-again-for-a-changed-amount-replaces-the-payment`,
  and the message "Your payment is already authorized." is no longer shown.
- `PaymentSubmitted`, the epic's outcome event, is published when Place Order's payment is authorized, not when the
  payment step is sent.

## Assumptions

- answered: the payment is authorized at "Place Order", after the review's fingerprint and the stock are checked, for
  exactly the total the review showed; the payment step only chooses how to pay; the confirmation shows the prices
  of the moment the order was placed, and nothing changes them afterwards (the user, 2026-10-21, on decision
  an-open-checkout-keeps-its-price-03 in the Java shop)
- answered: a refused payment and an unavailable provider keep their wording and are shown on the review (the user,
  2026-10-21)
- answered: a, in `SharedScenariosTest`, not in the gate: a `Test:` line need not name an existing browser test while a story that is not delivered yet changes or removes that scenario — the exemption the test already grants undelivered stories for titles; the plan lists the test under `## Changed tests`. Same answer as the .NET shop's decision the-payment-is-authorized-at-place-order-01. (the-payment-is-authorized-at-place-order-01)

## Notes

- From the Java shop's decision `an-open-checkout-keeps-its-price-03`, 2026-10-21: an authorization taken on the payment
  step is for the total of that moment; once the review shows today's prices, Place Order would confirm an order the
  authorization does not cover. Authorizing at Place Order removes the gap. Written for both shops with the same text.
- Guarantees, not criteria: `payment-step-offers-the-provider`, `choosing-the-provider-opens-the-review`,
  `unknown-provider-is-refused` and the payment step's totals hold; the confirmation's scenarios in
  `checkout/ordering` hold.
- Order of Place Order's steps: the review's checks, the stock reservation, the payment, the confirmation. A refused
  or unavailable payment releases the reservation; how is the plan's.
- Spec (`checkout/payment`): changes `authorized-payment-moves-to-review`, `refused-payment-stays-at-payment`,
  `slow-provider-counts-as-unavailable`, `the-payment-request-names-its-checkout`, `two-checkouts-send-two-references`,
  `the-provider-answers-per-checkout`, `a-second-attempt-names-the-same-checkout`; removes
  `paying-again-for-the-same-amount-keeps-the-payment` and `paying-again-for-a-changed-amount-replaces-the-payment`;
  adds `place-order-authorizes-the-reviewed-total`, `a-refused-payment-holds-no-stock` and
  `place-order-refused-for-stock-requests-no-payment`.
