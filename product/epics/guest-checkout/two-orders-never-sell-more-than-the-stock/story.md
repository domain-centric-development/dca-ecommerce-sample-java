---
id: two-orders-never-sell-more-than-the-stock
epic: guest-checkout
status: draft
context: checkout
feature: ordering
title: Two orders placed at once never sell more than the stock
depends_on: []
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
- And the other stays at the review `/checkout/review` with "Cannot place the order, 1 item(s) unavailable or out of
  stock."
- And shortly afterwards the product page of "Wooden Hexagon Desk Model" reads "3 units available"

## Changed expectations

- Two orders placed at the same moment that together need more than the stock are both confirmed now; the stock
  reduction of the later one finds too little stock, leaves the stock as it is and only writes a log line. After, the
  later order is refused at the review and the stock goes down by the confirmed order only.

## Assumptions

- open: which behaviour is wanted — (a) the later order is refused at the review (as written), or (b) both orders are
  confirmed and the shortfall is shown to the operator (then this is a story of `stock-oversight`, with a surface for
  it), or (c) both, (a) as the rule and (b) for whatever still slips through?
- open: the refused guest's message — the existing "Cannot place the order, 1 item(s) unavailable or out of stock."
  (as written), or a sentence of its own for an order someone else was faster with?

## Notes

- From TODO #92 (overselling, second half), checked 2026-10-15: still true in both shops — Place Order checks the
  stock again, but nothing holds it between that check and the reduction, and a reduction that finds too little stock
  is skipped with a log line.
- Spec (`checkout/ordering`): adds `the-order-the-stock-no-longer-covers-is-refused`; holds
  `place-order-is-refused-when-stock-ran-short` and `place-order-is-refused-when-sold-out`. `inventory/stock` holds.
