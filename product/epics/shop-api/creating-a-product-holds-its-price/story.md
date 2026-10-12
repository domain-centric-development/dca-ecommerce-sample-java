---
id: creating-a-product-holds-its-price
epic: shop-api
status: adopted
context: product
feature: assortment
title: Creating a product holds its price and stock to the rules
depends_on: [staff-create-a-product-through-the-api, prices-shown-from-the-price-owner, availability-shown]
---

# Creating a product holds its price and stock to the rules

## Story

As a member of staff I want a new product's price and initial stock to follow the same rules as every other
amount in the shop, so that the catalogue never shows a price of nothing, a fraction of a cent or a negative stock.

## Acceptance criteria

<!-- happy-path: a-price-is-rounded-half-up-to-cents -->

### Rule: A price is rounded to two decimals, half up

#### a-price-is-rounded-half-up-to-cents
- Given a client holding a staff token
- When it creates "Rounding Mug" with SKU "ROUND-001", price 14.995, category "Desk & Office" and stock 5
- Then the shop answers 201 with `price` 15.00
- And the catalogue card "Rounding Mug" shows "15.00 EUR"

### Rule: A price is greater than zero

#### a-price-of-zero-is-refused
- Given a client holding a staff token
- When it creates "Free Mug" with SKU "FREE-001", price 0, category "Desk & Office" and stock 5
- Then the shop answers 400
- And the catalogue still holds 21 products

#### a-price-that-rounds-to-zero-is-refused
- Given a client holding a staff token
- When it creates "Tiny Mug" with SKU "TINY-001", price 0.001, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its detail "Price must be greater than zero"

### Rule: A price is at most 999999999999.99

#### the-highest-price-is-accepted
- Given a client holding a staff token
- When it creates "Priceless Model" with SKU "MAX-001", price 999999999999.99, category "Modeling" and stock 1
- Then the shop answers 201
- And the catalogue card "Priceless Model" shows "999999999999.99 EUR"

#### a-price-above-the-highest-is-refused
- Given a client holding a staff token
- When it creates "Too Precious Model" with SKU "MAX-002", price 1000000000000.00, category "Modeling" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its detail "Amount exceeds maximum monetary value"

### Rule: Initial stock may be zero but not negative

#### an-initial-stock-of-zero-is-accepted
- Given a client holding a staff token
- When it creates "Sold Out Pin" with SKU "PIN-002", price 12.99, category "Stickers & Pins" and stock 0
- Then the shop answers 201
- And the catalogue card "Sold Out Pin" shows "12.99 EUR" and "Out of Stock"

#### a-negative-initial-stock-is-refused
- Given a client holding a staff token
- When it creates "Owed Pin" with SKU "PIN-003", price 12.99, category "Stickers & Pins" and stock -1
- Then the shop answers 400
- And the catalogue still holds 21 products

## Out of scope

- The body of the 400 for a price of zero or a negative stock: its shape is not replayed.
- The currency: always EUR on creation — `staff-create-a-product-through-the-api`.
- Changing a price after creation: no page, API or tool offers it today.
- The moment right after creation before price and stock have arrived: a race, not behaviour.

## Notes

- Replay story: `API-05` (shop-api).
- Inventory: product P13 (price, stock), P17 (status only), P21; product rules price, initial stock; pricing R5,
  R6 (as reached through product creation).
