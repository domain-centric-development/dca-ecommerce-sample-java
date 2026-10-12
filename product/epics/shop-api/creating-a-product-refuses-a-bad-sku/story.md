---
id: creating-a-product-refuses-a-bad-sku
epic: shop-api
status: adopted
context: product
feature: assortment
title: Creating a product refuses a bad SKU, name or text
depends_on: [staff-create-a-product-through-the-api]
---

# Creating a product refuses a bad SKU, name or text

## Story

As a member of staff I want the API to tell me why it refuses a product's SKU, name, description or image URL, so that the catalogue only
ever holds products it can tell apart and show.

## Acceptance criteria

<!-- happy-path: a-duplicate-sku-is-a-conflict -->

### Rule: A SKU is unique across the catalogue

#### a-duplicate-sku-is-a-conflict
- Given the seeded product "Domain-Driven Design" with SKU "BOOK-001" and a client holding a staff token
- When it creates a product "Another Book" with SKU "BOOK-001", price 19.99, category "Books" and stock 5
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Stock keeping unit already in use" and its detail "A product with SKU BOOK-001 already exists."
- And the catalogue still holds 21 products

### Rule: A SKU holds only upper-case letters, digits and hyphens

#### a-lower-case-sku-is-refused
- Given a client holding a staff token
- When it creates a product "Aggregate Mug" with SKU "mug-002", price 14.99, category "Desk & Office" and stock 12
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail

### Rule: A name is required and at most 255 characters long

#### an-overlong-name-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "LONG-001" and a name of 256 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail

#### a-name-of-255-characters-is-accepted
- Given a client holding a staff token
- When it creates a product with SKU "LONG-002" and a name of 255 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 201

#### a-blank-name-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "BLANK-001", name "   ", price 9.99, category "Books" and stock 1
- Then the shop answers 400
- And the catalogue still holds 21 products

### Rule: A description and an image URL are at most 2000 characters long

#### an-overlong-description-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "DESC-001", name "Long Story", a description of 2001 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail

#### an-overlong-image-url-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "IMG-001", name "Big Picture", an `imageUrl` of 2001 characters, price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail

## Out of scope

- The body of the 400 for a missing or blank field (SKU, name, category, price, stock): its shape is not replayed.
- Price and stock rules — `creating-a-product-holds-its-price`.

## Notes

- Replay story: `API-04` (shop-api).
- Inventory: product P15, P16, P17 (status only); product rules SKU, name, description, image URL.
