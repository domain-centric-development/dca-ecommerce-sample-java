---
id: the-header-basket
epic: fill-a-cart
status: adopted
context: cart
feature: shopping
title: The header basket
depends_on: [the-cart-page, CAT-05]
---

# The header basket

## Story

As a guest I want the page header to tell me how much is in my cart and what it comes to, on every page, so
that I can keep shopping without opening the cart to check.

## Acceptance criteria

<!-- happy-path: basket-dropdown-lists-the-cart -->

### Rule: Every shop page's header has a "Cart" entry that leads to the cart

#### header-cart-link
- Given a guest on the catalogue page
- When they follow "Cart" in the header
- Then they are on the cart page at `/cart`

### Rule: The header's cart entry carries a badge with the number of units in the cart

#### badge-counts-units
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the catalogue page
- Then the header's cart entry shows the badge "3"

#### badge-follows-each-add
- Given a guest whose cart holds "Domain-Driven Design" once
- When they press "Add to Cart" on the product page of "Domain-Driven Design"
- Then the header's cart entry on the cart page shows the badge "2"

### Rule: Hovering the header basket shows the cart's lines, their total at today's prices and a way to the cart

#### basket-dropdown-lists-the-cart
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they hover the header's cart entry on the catalogue page
- Then a dropdown lists "Domain-Driven Design" with "x2" and "Clean Architecture" with "x1"
- And it shows "Total" with "149.97 EUR"
- And it offers a "View Cart" button to `/cart`

## Out of scope

- The other header links and the home page's "View Cart" link — `browse-catalogue` (`CAT-04`, `CAT-05`).

## Notes

- Replay story: `CRT-03` (fill-a-cart).
- An empty cart shows no badge and no dropdown. Before this story there is no badge at all, so a test of that
  absence would be green before the build: it is a guarantee for the plan stage, not a criterion.
- The badge counts units (the sum of all quantities), while "Total Items" on the cart page counts lines.
- Inventory: cart C-03, C-04.
