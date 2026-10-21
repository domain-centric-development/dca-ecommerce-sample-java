---
id: an-open-checkout-keeps-its-price
epic: prices
status: approved
created: 2026-10-15T07:50:52Z
context: checkout
feature: ordering
title: Place Order orders exactly what the review showed
depends_on: [an-operator-changes-a-price, two-orders-never-sell-more-than-the-stock, the-payment-is-authorized-at-place-order]
---

# Place Order orders exactly what the review showed

## Story

As a customer in the middle of a checkout I want to order exactly what the review showed me, and to be told when
anything changed since, so that a price change or a change in another tab never changes my order behind my back.

## Acceptance criteria

### Rule: The review shows today's prices whenever it is opened

#### the-review-opened-again-shows-todays-price
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- When the guest opens the review again
- Then the review shows "Domain-Driven Design x1 49.99 EUR" and Total "54.98 EUR"

### Rule: Place Order orders only what the review showed; anything changed since refuses it and shows today's order

#### place-order-after-a-price-change-since-the-review-is-refused (happy path)
Title: Place Order after a price change since the review is refused and the review shows today's price
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- When the guest presses "Place Order"
- Then the review stays open at `/checkout/review` with "The price of 'Domain-Driven Design' has changed. Please check
  your order and place it again."
- And it shows "Domain-Driven Design x1 49.99 EUR" and Total "54.98 EUR"
- And no confirmation is shown
- And the payment provider received no payment request

#### place-order-after-a-change-in-another-tab-is-refused
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR
- And in a second tab of the same browser the guest has since chosen "Express Shipping" on the delivery step
- When the guest presses "Place Order" on the first tab's review
- Then the review stays open at `/checkout/review` with "Your order has changed since you reviewed it. Please check it
  and place it again."
- And it shows "Express Shipping"
- And no confirmation is shown

#### place-order-after-the-refusal-orders-what-the-review-now-shows
- Given a guest whose "Place Order" was refused because the price of "Domain-Driven Design" changed from 54.99 EUR to
  49.99 EUR, and the review now shows 49.99 EUR
- When the guest presses "Place Order" again
- Then the confirmation page shows under "Items Ordered" "Domain-Driven Design x1 49.99 EUR"
- And the payment provider received one payment request, of "54.98"

## Out of scope

- The change itself: story `an-operator-changes-a-price`.
- When and for which total the payment is authorized: story `the-payment-is-authorized-at-place-order` — at
  "Place Order", after the review's checks, for the total the review showed.
- A cart: story `the-cart-shows-a-changed-price`.
- The steps before the review: their order summary shows the checkout's prices as they are.
- A shortage of stock: its refusal and wording stay as they are.

## Changed expectations

- Today the review shows the prices the checkout started with, however often it is opened; after, it shows today's.
- Today a changed price refuses "Place Order" with "Cannot place the order, 1 item(s) unavailable or out of stock."
  and the guest starts a fresh checkout; after, the refusal names the product and the price, the review shows today's
  prices, and pressing "Place Order" again orders them. The shared vector `checkout.confirm.price-changed` compares
  with what the review showed, not with the prices the checkout started with.
- Today "Place Order" orders whatever the checkout holds when it is pressed, also a change made in another tab after
  the review was shown; after, such a change refuses it.

## Assumptions

- answered: an open checkout keeps its snapshot of the prices (planning/questions-2026-10-15.md Q4.7)
- answered: the customer gets exactly what the review shows. The review shows today's prices whenever it is opened and
  changes nothing; its "Place Order" carries a fingerprint of what it showed — every line with product, quantity and
  unit price, the buyer's details, the delivery address, the shipping method and its price, the payment method and the
  total. "Place Order" works out the same at that moment; a different result refuses the order and the review shows
  today's. A change in another tab counts like a price change (the user, 2026-10-21; replaces keeping the captured
  price and the answer of decision -01 that the order is placed at it)
- answered: a change other than a price is refused with "Your order has changed since you reviewed it. Please check it
  and place it again." (the user, 2026-10-21)
- answered: correction: none of a/b/c — the review's GET stays a read and shows today's prices; "Place Order" carries a fingerprint of everything the review showed (lines with product, quantity and unit price, buyer details, delivery address, shipping method and price, payment method, total) and works out the same at that moment; a different result refuses the order and the review shows today's. No DCA-HEX-014 exception; what was built to store prices on the GET goes. The story is rewritten accordingly (2026-10-21). (an-open-checkout-keeps-its-price-02)
- answered: correction: the payment is authorized at "Place Order", after the fingerprint and the stock are checked, for exactly the total the review showed — not on the payment step; the confirmation shows the prices of the moment the order was placed and nothing changes them afterwards. Written as its own story, `the-payment-is-authorized-at-place-order`, which this story depends on; a refused "Place Order" requests no payment, and the order after the refusal is authorized at today's total (the user, 2026-10-21)
- answered: correction: none of a/b/c — the payment is authorized at "Place Order", after the fingerprint and the stock are checked, for exactly the total the review showed; the payment step only chooses how to pay. The confirmation shows the prices of the moment the order was placed and nothing changes them afterwards. Written as its own story, `the-payment-is-authorized-at-place-order` (epic provider-payment), which this story now depends on; this story gains: a refused "Place Order" requests no payment, and the order after the refusal is authorized at the total then shown. (an-open-checkout-keeps-its-price-03)
- answered: a — the two Test: lines are corrected on the main line (fa6873b8) to PaymentAtPlaceOrderIntegrationTest#choosingHowToPayRequestsNoPayment and #placeOrderRequestsThePaymentWithTheCheckoutReference; nothing in this story changes — merge the main line and run the build again. The cause, a Moved tests row naming the old test, is fixed in the pipeline (dca-factory 0.87.6). (an-open-checkout-keeps-its-price-04)
- answered: a — the story's lines are the resolution of the four files; from dca-factory 0.87.7 `main-tests` leaves out a test file an answered integrate decision names, and from 0.87.10 the integrate step applies this answer; nothing in the files changes, run the integrate step again. (an-open-checkout-keeps-its-price-05)

## Notes

- From TODO #96 and Q4.7, 2026-10-15; rewritten 2026-10-21 by the user's answers — the customer gets exactly what the
  review shows (a fingerprint sent with "Place Order", as an HTTP `ETag` with `If-Match`); opening the review changes
  nothing, so DCA-HEX-014 holds without an exception. Replaces the review GET that stored today's prices (decisions
  -02 Java / -03 .NET).
- Guarantee, not a criterion: "Place Order" with nothing changed since the review orders what it showed — today's
  behaviour.
- Depends on `two-orders-never-sell-more-than-the-stock`: both change `checkout/ordering`, one after the other.
- Spec (`checkout/ordering`): adds the four scenarios above; holds `confirmation-shows-what-was-ordered`.
