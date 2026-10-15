---
id: an-unusable-product-request-is-a-problem-document
epic: shop-api
status: approved
context: product
feature: assortment
title: Every refused product creation is a problem document
depends_on: []
---

# Every refused product creation is a problem document

## Story

As a partner application I want every refused product creation to answer in the same shape, so that one error
handler reads a blank name the way it reads a lower-case SKU.

## Acceptance criteria

### Rule: A name is required and at most 255 characters long

#### a-blank-name-is-refused (happy path)
- Given a client holding a staff token
- When it creates a product with SKU "BLANK-001", name "   ", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `name` with the reason
- And the catalogue still holds 21 products

### Rule: A price is greater than zero

#### a-price-of-zero-is-refused
- Given a client holding a staff token
- When it creates "Free Mug" with SKU "FREE-001", price 0, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `price` with the reason
- And the catalogue still holds 21 products

### Rule: Initial stock may be zero but not negative

#### a-negative-initial-stock-is-refused
- Given a client holding a staff token
- When it creates "Owed Pin" with SKU "PIN-003", price 12.99, category "Stickers & Pins" and stock -1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `stock` with the reason
- And the catalogue still holds 21 products

## Changed expectations

- A blank name, a price of zero and a negative initial stock are answered 400 now, with a body that is not a problem
  document, or one titled "One or more validation errors occurred." with a list of fields; after, each is a problem
  document titled "Unacceptable value" whose `errors` names each refused field and why.

## Assumptions

- answered: a refused value is a problem document titled "Unacceptable value" with an `errors` map that names each
  field and why (planning/questions-2026-10-15.md Q4.6)

## Notes

- From TODO #95 (a), validation half, checked 2026-10-15: still true in the Java shop (no problem document);
  partly in the .NET shop (a problem document, but with the framework's title and a field list).
- `errors` names the fields as the creation request spells them (`name`, `price`, `stock`).
- Spec (`product/assortment`): changes `a-blank-name-is-refused`, `a-price-of-zero-is-refused` and
  `a-negative-initial-stock-is-refused`; holds `a-lower-case-sku-is-refused`.
