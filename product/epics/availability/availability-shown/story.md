---
id: availability-shown
epic: availability
status: adopted
context: inventory
feature: availability
title: Availability shown
depends_on: [CAT-01, CAT-02]
---

# Availability shown

## Story

As a visitor I want to see on the catalogue and on the product page whether a product is in stock and how
many units are left, and to be offered "Add to Cart" only for a product that is available, so that I never
try to buy something the shop cannot deliver.

## Acceptance criteria

<!-- happy-path: product-page-shows-units-available -->

### Rule: A product starts with the stock it was created with

#### product-page-shows-units-available
- Given the seeded product "Domain-Driven Design", created with a stock of 20
- When a visitor opens its product page
- Then the page shows the badge "In Stock"
- And it shows "Availability" with the value "20 units available"

#### few-units-are-counted-exactly
- Given the seeded product "Wooden Hexagon Desk Model", created with a stock of 8
- When a visitor opens its product page
- Then it shows "Availability" with the value "8 units available"

### Rule: A product with stock is shown as in stock on its card

#### card-shows-in-stock
- Given the seeded sample catalogue, in which every product has stock
- When a visitor opens the catalogue page
- Then every card shows the badge "In Stock"

### Rule: "Add to Cart" is offered only for an available product

#### available-product-offers-add-to-cart
- Given the seeded product "Clean Architecture", created with a stock of 35
- When a visitor opens its product page
- Then the page offers an "Add to Cart" button next to "Back to Products"

### Rule: A product with no stock left is shown as out of stock and cannot be added

#### card-shows-out-of-stock
- Given the product "Wooden Hexagon Desk Model" has 0 units in stock
- When a visitor opens the catalogue page
- Then its card shows the badge "Out of Stock"

#### out-of-stock-product-page
- Given the product "Wooden Hexagon Desk Model" has 0 units in stock
- When a visitor opens its product page
- Then the page shows the badge "Out of Stock" and "Availability" with the value "Currently out of stock"
- And it offers no "Add to Cart" button, only "Back to Products"

## Out of scope

- What "Add to Cart" does, and the cart refusing a product that is not available — `add-a-product-to-the-cart` and `fill-a-cart`.
- The stock going down when an order is placed — `guest-checkout`.
- Stock in the API and the assistant tools — `shop-api`, `catalogue-for-assistants`.
- Reserved stock: nothing in the shop reserves stock today, so the units available are the units on hand.
- A product whose stock has not arrived yet: not a state anyone can rely on reaching from outside.
- A list of products running low: no page shows it today.

## Notes

- Replay story: `AVL-01` (availability).
- How a test brings "Wooden Hexagon Desk Model" to 0 units is the test's choice; from outside the shop reaches
  it when all 8 units have been ordered.
- Inventory: inventory I1, I2; product P2 (stock part), P4 (badge), P6 (badge, availability line), P7 ("Add to Cart" only when available).
