---
id: the-checkout-price-holds-for-fifteen-minutes
epic: prices
status: superseded
created: 2026-10-21T06:36:42Z
context: checkout
feature: ordering
title: The price a checkout shows holds for fifteen minutes
depends_on: [an-open-checkout-keeps-its-price]
---

# The price a checkout shows holds for fifteen minutes

## Superseded

- Withdrawn before its release: "Place Order" orders exactly what the review showed, compared through a fingerprint
  (story `an-open-checkout-keeps-its-price`), so a change since the review always refuses and no time window is
  needed — the user, 2026-10-21.

## Story

As a customer at the review I want to get the price I see there, and to be told when it changed after I looked at it
too long ago, so that I never order at a price that was changed long ago without being told.

## Acceptance criteria

### Rule: Place Order more than fifteen minutes after the review showed its prices, with a price changed since, is refused with today's prices

#### place-order-on-an-expired-snapshot-with-a-changed-price-is-refused (happy path)
Title: Place Order on prices older than fifteen minutes that changed since is refused and shows today's prices
- Given a guest at the review with "Domain-Driven Design" once and "Standard Shipping", shown at 54.99 EUR
- And the price of "Domain-Driven Design" has since been set to 49.99 EUR through the staff API
- And 15 minutes and 1 second have passed since the review showed its prices
- When the guest presses "Place Order"
- Then the review stays open at `/checkout/review` with "The price of 'Domain-Driven Design' has changed. Please check
  your order and place it again."
- And it shows "Domain-Driven Design x1 49.99 EUR" and Total "54.98 EUR"
- And no confirmation is shown

#### place-order-after-the-refusal-takes-todays-price
- Given a guest whose "Place Order" was refused because the price of "Domain-Driven Design" changed from 54.99 EUR to
  49.99 EUR more than fifteen minutes after the review showed it
- When the guest presses "Place Order" again
- Then the confirmation page shows under "Items Ordered" "Domain-Driven Design x1 49.99 EUR"

## Out of scope

- Opening the review again and a price change within the fifteen minutes: story `an-open-checkout-keeps-its-price` —
  opening the review takes today's prices, and "Place Order" orders at the prices the review showed.
- A shortage of stock: its refusal and wording stay as they are.
- A session that expires for inactivity (`Expire`): unchanged.

## Changed expectations

- After `an-open-checkout-keeps-its-price`, "Place Order" orders at the prices the review showed however long ago it
  showed them; after this story, only within fifteen minutes. Later, a changed price refuses the order with a message
  that names the product, and the review shows today's prices.

## Assumptions

- answered: the customer gets the price the review shows; opening the review always takes today's prices (the user,
  2026-10-21, in story `an-open-checkout-keeps-its-price`)
- answered: the fifteen minutes count from the last time the review showed its prices — opening it, or a refused
  "Place Order" that shows today's (the user, 2026-10-21)
- answered: after the fifteen minutes "Place Order" is refused only when a price changed; with every price unchanged the
  order is placed (the user, 2026-10-21)
- answered: the refusal names the product: "The price of '<product>' has changed. Please check your order and place it
  again." (the user, 2026-10-21)

## Notes

- Follow-up to `an-open-checkout-keeps-its-price`, written for both shops with the same text.
- Guarantees, not criteria (green once the predecessor is delivered): "Place Order" exactly fifteen minutes after the
  review showed its prices, with a changed price, orders at the shown price; "Place Order" after fifteen minutes with
  every price unchanged places the order.
- A test needs to let fifteen minutes pass without waiting them out; how is the plan's.
- Spec (`checkout/ordering`): adds the two scenarios above.
