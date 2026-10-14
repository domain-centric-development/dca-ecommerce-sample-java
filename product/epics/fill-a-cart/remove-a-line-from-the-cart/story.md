---
id: remove-a-line-from-the-cart
epic: fill-a-cart
status: approved
context: cart
feature: shopping
title: A line is removed from the cart page
depends_on: []
---

# A line is removed from the cart page

## Story

As a shopper I want to remove a line from my cart on the cart page, so that what I no longer want does not travel
on into the checkout.

## Acceptance criteria

### Rule: "Remove" takes one line out of the cart, whatever its quantity; the other lines keep their order

#### remove-takes-the-line-out (happy path)
- Given a guest who added "Domain-Driven Design", "Clean Architecture" and "Team Topologies" once each, in that order
- When they press "Remove" on the line "Clean Architecture" on the cart page
- Then the cart page shows two lines, "Domain-Driven Design" first and "Team Topologies" second
- And the header's cart entry shows the badge "2"

#### remove-takes-the-whole-quantity
- Given a guest whose cart holds "Domain-Driven Design" with quantity 3 and "Clean Architecture" with quantity 1
- When they press "Remove" on the line "Domain-Driven Design"
- Then the cart page shows one line, "Clean Architecture" with quantity 1
- And it shows "Total Items: 1" and "Total Quantity: 1"

#### removing-the-last-line-empties-the-cart
- Given a guest whose cart holds only "Domain-Driven Design" with quantity 1
- When they press "Remove" on that line
- Then the cart page shows "Your cart is empty"

### Rule: A "Remove" for a line the visitor's cart does not hold changes nothing

#### removing-a-line-already-gone-changes-nothing
- Given a guest who opened the cart page with "Domain-Driven Design" and "Clean Architecture" in two tabs and
  pressed "Remove" on "Clean Architecture" in the first tab
- When they press "Remove" on "Clean Architecture" in the second tab
- Then the cart page opens and shows the one line "Domain-Driven Design"

#### a-remove-with-a-malformed-line-id-changes-nothing
- Given a guest whose cart holds "Domain-Driven Design" with quantity 1
- When a "Remove" naming the line "no-such-line" is sent to the shop
- Then the cart page opens and still shows "Domain-Driven Design" with quantity 1

#### a-remove-for-another-visitors-line-changes-nothing
- Given a guest whose cart holds `"Ubiquitous Language" Mug` and a second guest in another browser whose cart holds
  "Team Topologies"
- When the second guest sends a "Remove" naming the first guest's `"Ubiquitous Language" Mug` line
- Then the first guest's cart still shows `"Ubiquitous Language" Mug` with quantity 1
- And the second guest's cart page still shows "Team Topologies"

## Out of scope

- Removing through the cart API, which keeps naming the product (`DELETE /api/carts/<cartId>/items/<productId>`).
- Changing a line's quantity on the cart page.
- A message after a removal: the cart page shows the cart as it is now, nothing more.

## Notes

- Decided by the user 2026-10-14 (triage C13): the cart already removes a line by the line's own id, and nothing
  reaches it; removing a line by its own id becomes a use case of its own, reached from the cart page — the page
  knows the line, not only the product. The API's removal by product stays as it is.
