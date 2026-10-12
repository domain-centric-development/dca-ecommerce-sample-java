---
id: the-cart-page
epic: fill-a-cart
status: adopted
context: cart
feature: shopping
title: The cart page
depends_on: [add-a-product-to-the-cart, prices-shown-from-the-price-owner]
---

# The cart page

## Story

As a guest I want the cart page to show what each line costs, how much I have collected and what it comes to
including VAT, so that I know what the order will cost before I decide.

## Acceptance criteria

<!-- happy-path: cart-shows-lines-with-prices -->

### Rule: Each line shows its image, name, quantity, today's unit price and the line total

#### cart-shows-lines-with-prices
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then the line "Domain-Driven Design" shows its product image, "Quantity" 2, "Unit Price" "54.99 EUR" and "Line Total" "109.98 EUR"
- And the line "Clean Architecture" shows its product image, "Quantity" 1, "Unit Price" "39.99 EUR" and "Line Total" "39.99 EUR"

### Rule: Above the lines the cart counts its lines and its units

#### cart-counts-lines-and-units
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then it shows "Total Items: 2" and "Total Quantity: 3"

### Rule: Below the lines the cart shows the subtotal and the VAT it contains, at 19 %

#### cart-shows-the-subtotal
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then the "Subtotal" row shows "149.97 EUR"

#### cart-shows-the-contained-vat
- Given a guest who added "Domain-Driven Design" twice and "Clean Architecture" once
- When they open the cart page
- Then the "incl. VAT" row shows "23.94 EUR"

#### vat-is-rounded-half-up
- Given a guest who added "Domain-Driven Design" once
- When they open the cart page
- Then the "Subtotal" row shows "54.99 EUR" and the "incl. VAT" row shows "8.78 EUR"

### Rule: An empty cart invites the visitor to browse

#### empty-cart-invites-browsing
- Given a guest who has added nothing
- When they open the cart page
- Then it shows "Your cart is empty" and "Browse our products to find something you'll love."
- And a "Browse Products" button to `/products`
- And no lines, counts, subtotal or VAT row

## Out of scope

- "Proceed to Checkout", the "Continue Shopping" link beside it, and what it starts — `start-the-checkout-from-the-cart`.
- The warnings for a line that is out of stock or short of stock — `stock-warnings-in-the-cart`; the disabled "Cannot Checkout" button — `starting-and-placing-check-the-cart`.
- The "Price increased by …" / "Price decreased by …" alerts and the price-change banner: no surface changes a
  price today, so they cannot be reached from outside.
- The "Order Completed" and "Abandoned" badges: a cart never leaves the active state through the shop.
- Removing a line or changing a quantity: the cart page offers neither.

## Notes

- Replay story: `CRT-02` (fill-a-cart).
- VAT is contained in the gross subtotal: VAT = subtotal − subtotal ÷ 1.19, rounded to two decimals half-up.
- Inventory: cart C-17, C-18, C-19, C-26.
