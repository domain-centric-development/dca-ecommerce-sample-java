---
id: product-refusals-name-the-field-in-the-shops-words
epic: shop-api
status: approved
created: 2026-10-23T21:25:20Z
context: product
title: Product refusals name the field in the shop's words
depends_on: [the-take-over-leads-only-to-pages-of-the-shop, the-product-findings-are-closed]
---

# Product refusals name the field in the shop's words

## Story

As a partner creating a product through the API I want every refusal of a value to name the field, with a code I
can act on and a message in the shop's words, so that I can correct the request without reading framework
messages — and one rule answers one way, whether the value is missing, blank or too long.

## Acceptance criteria

`errors` holds one entry under the field as the request spells it: `{ "code": <code>, "message": <message> }`.

### Rule: A SKU is required

#### a-missing-sku-is-refused (happy path)
- Given a client holding a staff token
- When it creates a product "Aggregate Mug" without a SKU, price 14.99, category "Desk & Office" and stock 12
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `sku` with code `sku-missing` and the message "Please enter a SKU."
- And the catalogue still holds 21 products

### Rule: A SKU holds only upper-case letters, digits and hyphens

#### a-lower-case-sku-is-refused
- Given a client holding a staff token
- When it creates a product "Aggregate Mug" with SKU "mug-002", price 14.99, category "Desk & Office" and stock 12
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `sku` with code `sku-malformed` and the message "A SKU holds only capital letters, digits and hyphens."
- And the catalogue still holds 21 products

### Rule: A name is required and at most 255 characters long

#### a-missing-name-is-refused
- Given a client holding a staff token
- When it creates a product without a name, SKU "NONAME-001", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `name` with code `product-name-missing` and the message "Please enter a name."
- And the catalogue still holds 21 products

#### a-blank-name-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "BLANK-001", name "   ", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `name` with code `product-name-missing` and the message "Please enter a name."
- And the catalogue still holds 21 products

#### an-overlong-name-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "LONG-001" and a name of 256 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `name` with code `product-name-too-long` and the message "A name holds at most 255 characters."
- And the catalogue still holds 21 products

### Rule: A description and an image URL are at most 2000 characters long

#### an-overlong-description-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "DESC-001", name "Long Story", a description of 2001 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `description` with code `description-too-long` and the message "A description holds at most 2000 characters."
- And the catalogue still holds 21 products

#### an-overlong-image-url-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "IMG-001", name "Big Picture", an `imageUrl` of 2001 characters, price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `imageUrl` with code `image-url-too-long` and the message "An image address holds at most 2000 characters."
- And the catalogue still holds 21 products

### Rule: A price is required

#### a-missing-price-is-refused
- Given a client holding a staff token
- When it creates "Free Mug" with SKU "FREE-002", no price, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `price` with code `price-missing` and the message "Please enter a price."
- And the catalogue still holds 21 products

### Rule: A price is greater than zero

#### a-price-of-zero-is-refused
- Given a client holding a staff token
- When it creates "Free Mug" with SKU "FREE-001", price 0, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `price` with code `price-not-positive` and the message "A price is greater than zero."
- And the catalogue still holds 21 products

#### a-price-that-rounds-to-zero-is-refused
- Given a client holding a staff token
- When it creates "Tiny Mug" with SKU "TINY-001", price 0.001, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `price` with code `price-not-positive` and the message "A price is greater than zero."
- And the catalogue still holds 21 products

### Rule: A price is at most 999999999999.99

#### a-price-above-the-highest-is-refused
- Given a client holding a staff token
- When it creates "Too Precious Model" with SKU "MAX-002", price 1000000000000.00, category "Modeling" and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `price` with code `price-too-high` and the message "A price is at most 999999999999.99."
- And the catalogue still holds 21 products

### Rule: A category is required

#### a-missing-category-is-refused
- Given a client holding a staff token
- When it creates "Lost Mug" with SKU "CAT-001", price 9.99, no category and stock 1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `category` with code `category-missing` and the message "Please choose a category."
- And the catalogue still holds 21 products

### Rule: Initial stock may be zero but not negative

#### a-missing-initial-stock-is-refused
- Given a client holding a staff token
- When it creates "Owed Pin" with SKU "PIN-004", price 12.99, category "Stickers & Pins" and no stock
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `stock` with code `initial-stock-missing` and the message "Please enter the initial stock."
- And the catalogue still holds 21 products

#### a-negative-initial-stock-is-refused
- Given a client holding a staff token
- When it creates "Owed Pin" with SKU "PIN-003", price 12.99, category "Stickers & Pins" and stock -1
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `stock` with code `initial-stock-negative` and the message "The initial stock is zero or more."
- And the catalogue still holds 21 products

## Changed expectations

- A refused product value answers today either without any detail or with `errors` naming the field and a reason
  the framework words; after, every refused value names its field with a code and a message in the shop's words.
- A product request without a SKU, a price, a category or a stock is refused today with the framework's reason;
  after, with the code and message of its rule.

## Assumptions

- answered: every refusal of a value from input names its field, with a code in the domain's words and a message
  in the shop's words (the person, 2026-11-01, WP-170 decision 4)
- answered: one named failure per rule of the value — missing (null or blank alike), too long, malformed, out of
  range — each carrying the figures it compared (the person, 2026-11-01, WP-170 decision 5)
- answered: the rule is checked once, in the value object; the request carries no annotation repeating it and no
  default turning a missing value into one; the message is the adapter's, never the exception's (WP-170
  decisions 1 and 2)

## Notes

- From the findings triage of 2026-10-23 (WP-151), rewritten 2026-11-01 for WP-170 (a rule is checked once, in the
  value object): `CreateProductRequest` checks blank and positive with request validation and the value objects
  check them again, so one rule answers twice — the blank name names `name`, the overlong name carries no detail.
- .NET answers a missing SKU or category with the framework's sentence, a long name without the field, an unreadable value with the serializer's message.
- A request with several refused values names the first refusal the creation meets; which one is not part of the
  contract.
- Spec (`product/assortment`): changes the refusal scenarios under their rules; adds `a-missing-sku-is-refused`,
  `a-missing-name-is-refused`, `a-missing-price-is-refused`, `a-missing-category-is-refused`,
  `a-missing-initial-stock-is-refused` and the rules "A SKU is required", "A price is required", "A category is
  required".
