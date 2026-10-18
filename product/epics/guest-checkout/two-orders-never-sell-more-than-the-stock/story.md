---
id: two-orders-never-sell-more-than-the-stock
epic: guest-checkout
status: approved
created: 2026-10-14T23:44:13Z
context: checkout
feature: ordering
title: Two orders placed at once never sell more than the stock
depends_on: [the-confirmation-says-only-what-happened]
---

# Two orders placed at once never sell more than the stock

## Story

As the shop owner I want an order placed only while the stock still covers it, so that the shop never confirms an
order it cannot deliver.

## Acceptance criteria

### Rule: Of orders placed at the same time, only those the stock still covers are placed

#### the-order-the-stock-no-longer-covers-is-refused (happy path)
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And two guests, each at the review with "Wooden Hexagon Desk Model" five times
- When both press "Place Order" at the same moment
- Then exactly one of them sees the confirmation
- And the other is back at the review `/checkout/review` with
  "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."
- And shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "3 units available"

### Rule: Place Order checks every line again and changes nothing when one fails

#### place-order-is-refused-when-stock-ran-short
- Given a guest at the review with "Wooden Hexagon Desk Model" five times
- And another visitor has since bought it five times, leaving 3
- When the guest presses "Place Order"
- Then the review step stays open at `/checkout/review` and shows
  "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."
- And no confirmation is shown
- And the product page still reads "3 units available" and the guest's cart still holds the product five
  times

#### place-order-is-refused-when-sold-out
- Given a guest at the review with "Wooden Hexagon Desk Model" twice
- And another visitor has since bought all 8
- When the guest presses "Place Order"
- Then the review step stays open at `/checkout/review` and shows
  "Not enough stock left for 'Wooden Hexagon Desk Model'. Please adjust your order."
- And no confirmation is shown

## Changed expectations

- Two orders placed at the same moment that together need more than the stock are both confirmed now; the stock
  reduction of the later one finds too little stock, leaves the stock as it is and only writes a log line. After, the
  later order is refused at the review and the stock goes down by the confirmed order only.
- An order that someone else's order left without enough stock is refused at the review with "Cannot place the
  order, 1 item(s) unavailable or out of stock." now; after, with "Not enough stock left for '<product>'. Please
  adjust your order.", which names the product.

## Assumptions

- answered: the later of two orders is refused at Place Order with "Not enough stock left for '…'. Please adjust your
  order." and leads back to the review (planning/questions-2026-10-15.md Q4.4)

## Notes

- From TODO #92 (overselling, second half), checked 2026-10-15: still true in both shops — Place Order checks the
  stock again, but nothing holds it between that check and the reduction, and a reduction that finds too little stock
  is skipped with a log line.
- The two held scenarios of a stock that ran short before Place Order are the later of two orders as well; they carry
  the same message, so the customer reads one sentence whichever check refuses the order.
- Depends on `the-confirmation-says-only-what-happened`: both change `checkout/ordering`, one after the other.
- Spec (`checkout/ordering`): adds `the-order-the-stock-no-longer-covers-is-refused`; changes
  `place-order-is-refused-when-stock-ran-short` and `place-order-is-refused-when-sold-out` (the message). `inventory/stock`
  holds.
