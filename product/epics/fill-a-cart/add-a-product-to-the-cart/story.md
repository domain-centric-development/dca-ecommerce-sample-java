---
id: add-a-product-to-the-cart
epic: fill-a-cart
status: adopted
context: cart
feature: shopping
title: Add a product to the cart
depends_on: [CAT-02, availability-shown]
---

# Add a product to the cart

## Story

As a guest I want to put a product into a cart from its product page and see it there at once, so that I
can collect what I want to buy without keeping it in mind.

## Acceptance criteria

<!-- happy-path: add-to-cart-lands-on-the-cart -->

### Rule: "Add to Cart" puts one unit of the product into the visitor's cart and opens the cart page

#### add-to-cart-lands-on-the-cart
- Given a guest with an empty cart on the product page of "Domain-Driven Design"
- When they press "Add to Cart"
- Then they are on the cart page at `/cart`
- And the page shows the message "Product added to cart!"
- And the cart shows one line "Domain-Driven Design" with quantity 1

#### cart-page-heading
- Given a guest who added "Domain-Driven Design" to the cart
- When they open `/cart`
- Then the page title and the heading read "Shopping Cart"

### Rule: A product appears on one line only; adding it again raises that line's quantity

#### adding-again-raises-the-quantity
- Given a guest whose cart holds one line "Domain-Driven Design" with quantity 1
- When they press "Add to Cart" on the product page of "Domain-Driven Design" again
- Then the cart shows one line "Domain-Driven Design" with quantity 2
- And no second line for "Domain-Driven Design"

#### another-product-gets-its-own-line
- Given a guest whose cart holds one line "Domain-Driven Design" with quantity 1
- When they press "Add to Cart" on the product page of "Clean Architecture"
- Then the cart shows two lines: "Domain-Driven Design" with quantity 1 and "Clean Architecture" with quantity 1

## Out of scope

- Whether "Add to Cart" is offered at all (only for an available product) — `availability-shown`.
- Prices, line totals, the counts, the subtotal and the VAT line on the cart page, and the empty cart — `the-cart-page`.
- The header basket — `the-header-basket`.
- The anti-forgery token on the add form — cross-cutting, `fits-its-surroundings`.
- Adding through the API, with its refusals for an unknown product, a quantity below 1 or too little stock —
  `shop-api`.
- A cart page quantity field or a remove button: the cart page has neither.
- The price a line remembers from the moment it was added: no surface changes a price, so the difference is not
  visible from outside.

## Notes

- Replay story: `CRT-01` (fill-a-cart).
- Inventory: cart C-06, C-10, C-16 (page title; the lines' name and quantity part of C-17).
