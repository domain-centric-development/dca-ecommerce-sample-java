---
id: the-cart-holds-no-more-than-is-in-stock
epic: fill-a-cart
status: approved
created: 2026-10-14T23:44:13Z
context: cart
feature: shopping
title: A cart line holds no more than the product's stock
depends_on: [an-unusable-cart-request-is-a-problem-document]
---

# A cart line holds no more than the product's stock

## Story

As a customer I want the cart to refuse a unit more than the shop has, so that I learn it when I add the product and
not only when I try to check out.

## Acceptance criteria

### Rule: Adding is refused when the line would hold more than the product's stock, and the refusal names what the cart already holds

#### adding-past-the-stock-on-the-page-is-refused (happy path)
- Given the seeded "Wooden Hexagon Desk Model" with a stock of 8
- And a guest whose cart holds "Wooden Hexagon Desk Model" with quantity 8
- When the guest presses "Add to Cart" on its product page
- Then the cart page opens with the error
  "There is not enough stock to add 1 of this product — your cart already holds 8, and 8 are in stock."
- And the line "Wooden Hexagon Desk Model" still shows quantity 8

#### adding-past-the-stock-through-the-api-is-refused
- Given Ada's open cart holds "Wooden Hexagon Desk Model" with quantity 5, and 8 are in stock
- When her client adds "Wooden Hexagon Desk Model" with quantity 4
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Not enough stock" and its detail
  "Requested 4 of product <id of Wooden Hexagon Desk Model>, more than is in stock."
- And her cart still holds "Wooden Hexagon Desk Model" with quantity 5

## Out of scope

- Two carts that together hold more than the stock: each cart is checked against the stock on its own; what an order
  does then is story `two-orders-never-sell-more-than-the-stock`.

## Changed expectations

- Adding a product is checked against the quantity added alone now, so a line can grow past the stock one "Add to
  Cart" at a time and is only stopped at the checkout; after, it is checked against what the line would hold.
- The cart page's refusal reads "There is not enough stock to add 1 of this product." now; after, it also names what
  the cart already holds and how many are in stock.

## Assumptions

- answered: adding counts what the cart already holds, and the message names it: "There is not enough stock to add 5
  of this product — your cart already holds 5, and 8 are in stock." (planning/questions-2026-10-15.md Q4.4)
- answered: the API's detail stays "Requested … of product …, more than is in stock.", the detail the API already
  gives for a quantity past the stock; the answer names the page's message only (planning/questions-2026-10-15.md
  Q4.4)

## Notes

- From TODO #92 (overselling, first half), checked 2026-10-15: still true in both shops.
- The limit's acceptance — a line of 5 plus 3 makes 8 with 8 in stock — holds today; it is a guard for the plan, not a
  criterion, because its test would be green before the build.
- Depends on `an-unusable-cart-request-is-a-problem-document`: both change the cart API's refusals, one after the
  other.
- Spec (`cart/shopping`): adds `adding-past-the-stock-on-the-page-is-refused` and
  `adding-past-the-stock-through-the-api-is-refused`; holds `more-than-in-stock-is-refused`,
  `exactly-the-stock-is-accepted` and `adding-the-same-product-again-adds-up`.
