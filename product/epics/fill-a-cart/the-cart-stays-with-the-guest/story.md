---
id: the-cart-stays-with-the-guest
epic: fill-a-cart
status: adopted
context: cart
feature: shopping
title: The cart stays with the guest
depends_on: [add-a-product-to-the-cart]
---

# The cart stays with the guest

## Story

As a guest I want my cart to be there again when I come back in the same browser, and to be mine alone, so
that I can collect products over several visits without an account and without seeing anyone else's.

## Acceptance criteria

<!-- happy-path: cart-kept-across-visits -->

### Rule: A guest's cart belongs to their browser and is kept across visits for 30 days

#### cart-kept-across-visits
- Given a guest who added `"Ubiquitous Language" Mug` to the cart and then closed the browser
- When they open the cart page in the same browser the next day
- Then the cart shows the line `"Ubiquitous Language" Mug` with quantity 1

### Rule: Every browser has its own cart

#### another-browser-has-its-own-cart
- Given a guest whose cart holds `"Ubiquitous Language" Mug`
- When a second visitor opens the cart page in another browser
- Then the second visitor's cart is empty

#### carts-of-two-guests-stay-apart
- Given a guest whose cart holds `"Ubiquitous Language" Mug` and a second guest in another browser
- When the second guest adds "Team Topologies" to their cart
- Then the second guest's cart shows only "Team Topologies"
- And the first guest's cart still shows only `"Ubiquitous Language" Mug`

### Rule: A guest has one cart, whichever page or tab they add from

#### one-cart-across-tabs
- Given a guest who added "Team Topologies" in one tab of their browser
- When they add `"Ubiquitous Language" Mug` from the product page in a second tab of the same browser
- Then the cart page in either tab shows both lines, "Team Topologies" and `"Ubiquitous Language" Mug`

## Out of scope

- What happens to the guest cart on registration, login and logout — `shop-with-an-account` (`logging-in-brings-the-guest-cart-along`, `the-cart-stays-with-the-person`).
- Creating a second cart through the API, and the API's answer for someone else's cart — `shop-api`.
- A shop restart: the shop keeps nothing across a restart.

## Notes

- Replay story: `CRT-04` (fill-a-cart).
- The guest's browser identity lives 30 days; the cart is kept with it.
- Inventory: cart C-01, C-52 and C-53 as seen from the pages (one cart per visitor), C-54 as seen from the pages
  (each visitor sees only their own cart).
