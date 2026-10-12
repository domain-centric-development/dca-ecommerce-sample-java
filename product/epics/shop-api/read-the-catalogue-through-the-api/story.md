---
id: read-the-catalogue-through-the-api
epic: shop-api
status: adopted
context: product
feature: catalogue
title: Read the catalogue through the API
depends_on: [CAT-01, prices-shown-from-the-price-owner, availability-shown]
---

# Read the catalogue through the API

## Story

As a partner application I want to read the shop's products with their price and stock as JSON, without signing
in, so that I can offer the range in my own application without copying it by hand.

## Acceptance criteria

<!-- happy-path: listing-returns-the-range-in-name-order -->

### Rule: Anyone may read the catalogue, in the same order as the catalogue page

#### listing-returns-the-range-in-name-order
- Given the shop has started and seeded the sample catalogue of `seed-catalogue.json`
- When a client without a token sends `GET /api/products`
- Then the shop answers 200 with a JSON array of 21 products
- And they are in ordinal order of the product name, the first named `"Bounded Context" Enamel Pin`

### Rule: A product carries its master data, its price and its stock

#### a-listed-product-carries-price-and-stock
- Given the seeded product "Domain-Driven Design"
- When a client sends `GET /api/products`
- Then its entry has an `id`, `sku` "BOOK-001", `name` "Domain-Driven Design", its `description`, an `imageUrl`, `price` 54.99, `currency` "EUR", `category` "Books", `stockQuantity` 20 and `isAvailable` true

#### one-product-by-its-id
- Given the `id` of "Hexagonal Architecture Explained" as the list returns it
- When a client without a token sends `GET /api/products/<that id>`
- Then the shop answers 200 with that product: `sku` "BOOK-005", `price` 34.99, `currency` "EUR", `category` "Books", `stockQuantity` 26 and `isAvailable` true

### Rule: An unknown product is not found

#### an-unknown-product-is-not-found
- Given the seeded sample catalogue
- When a client sends `GET /api/products/no-such-product`
- Then the shop answers 404 with an empty body

## Out of scope

- Creating a product — `staff-create-a-product-through-the-api`.
- The same catalogue for AI assistants — `an-assistant-reads-the-catalogue`.

## Notes

- Replay story: `API-02` (shop-api).
- Inventory: product P11, P12; cross-cutting X11 (the public half).
