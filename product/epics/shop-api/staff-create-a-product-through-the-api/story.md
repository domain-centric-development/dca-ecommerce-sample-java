---
id: staff-create-a-product-through-the-api
epic: shop-api
status: adopted
context: product
feature: assortment
title: Staff create a product through the API
depends_on: [register-through-the-api, read-the-catalogue-through-the-api, CAT-02]
---

# Staff create a product through the API

## Story

As a member of staff I want to add a product to the catalogue through the API, so that a new product is on sale
without anyone touching the shop's data by hand; and as the shop owner I want nobody else to be able to do it.

## Acceptance criteria

<!-- happy-path: staff-create-a-product -->

### Rule: Staff create a product; its price is always in euros

#### staff-create-a-product
- Given a client holding a token whose roles include staff
- When it sends `POST /api/products` with `{"sku":"MUG-002","name":"Aggregate Mug","description":"A mug with a consistency boundary.","imageUrl":"","price":14.99,"category":"Desk & Office","stock":12}`
- Then the shop answers 201
- And the body has an `id`, `sku` "MUG-002", `name` "Aggregate Mug", `price` 14.99, `currency` "EUR" and `category` "Desk & Office"

#### a-created-product-joins-the-catalogue
- Given staff created "Aggregate Mug" with price 14.99 and stock 12
- When a visitor opens the catalogue page
- Then it shows 22 product cards
- And the card "Aggregate Mug" shows "14.99 EUR" and "In Stock"

#### description-and-image-may-be-left-out
- Given a client holding a staff token
- When it creates the product "Bounded Context Tote" with SKU "TOTE-001", price 19.99, category "Apparel" and stock 10, sending no `description` and no `imageUrl`
- Then the shop answers 201
- And the body has `description` "" and `imageUrl` ""

### Rule: A product without an image shows the first letter of its name instead

#### a-card-without-image-shows-the-initial
- Given staff created "Aggregate Mug" with `imageUrl` ""
- When a visitor opens the catalogue page
- Then the card "Aggregate Mug" shows the letter "A" where the image would be

### Rule: A product without a description says so on its page

#### a-product-page-without-description
- Given staff created "Bounded Context Tote" without a description
- When a visitor opens its product page
- Then the description reads "No description available."

### Rule: Only staff may create; a stranger is challenged, a customer is forbidden

#### a-stranger-is-challenged
- Given a client without a token
- When it sends `POST /api/products` with a valid product
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`
- And the body is an `application/problem+json` document with title "Unauthorized"
- And the catalogue still holds 21 products

#### a-customer-is-forbidden
- Given a client holding the token of the customer account "ada@example.com"
- When it sends `POST /api/products` with a valid product
- Then the shop answers 403
- And the body is an `application/problem+json` document with title "Forbidden"

## Out of scope

- How a staff token comes into being: no registration and no login hands out the staff role; staff tokens are
  issued outside the shop.
- Values the product rules refuse — `creating-a-product-refuses-a-bad-sku` (SKU, name) and `creating-a-product-holds-its-price` (price, stock).
- There is no page and no assistant tool that creates a product.
- The moment right after creation before price and stock have arrived: a race, not behaviour.

## Notes

- Replay story: `API-03` (shop-api).
- Inventory: product P4 (placeholder), P6 (empty description), P13, P14; account A38; cross-cutting X11 (the
  staff half), X12.
