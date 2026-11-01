---
id: price-refusals-name-the-field-in-the-shops-words
epic: shop-api
status: draft
created: 2026-11-01T12:00:08Z
context: pricing
title: Price refusals name the field in the shop's words
depends_on: [product-refusals-name-the-field-in-the-shops-words]
---

# Price refusals name the field in the shop's words

## Story

As a member of staff changing a price through the API I want a refused price to name the field, with a code and a
message in the shop's words, so that I can correct the request without reading framework messages.

## Acceptance criteria

`errors` holds one entry under the field as the request spells it: `{ "code": <code>, "message": <message> }`,
the shape the product refusals answer with.

### Rule: A new price is greater than zero

#### a-new-price-of-zero-is-refused (happy path)
- Given a client holding a staff token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":0}`
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `price` with code `price-not-positive` and the message "A price is greater than zero."
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"

#### a-missing-new-price-is-refused
- Given a client holding a staff token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{}`
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `price` with code `price-missing` and the message "Please enter a price."
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"

## Changed expectations

- A new price of zero is refused today with `errors` naming the price and a reason the framework words; after,
  with the code and message of its rule.
- A price change without a price is refused today with the framework's reason or not at all; after, as a missing
  price.

## Assumptions

- answered: every refusal of a value from input names its field, with a code in the domain's words and a message
  in the shop's words (the person, 2026-11-01, WP-170 decision 4)
- answered: one named failure per rule of the value — missing (null or blank alike), too long, malformed, out of
  range — each carrying the figures it compared (the person, 2026-11-01, WP-170 decision 5)
- answered: the rule is checked once, in the value object; the request carries no annotation repeating it and no
  default turning a missing value into one; the message is the adapter's, never the exception's (WP-170
  decisions 1 and 2)

## Notes

- WP-170 (a rule is checked once, in the value object): the Java shop's `ChangePriceRequest` checks the price with
  request validation and the price value checks it again; the .NET shop's request takes a `decimal`, so a missing
  price arrives as zero.
- Spec (`pricing`): changes `a-new-price-of-zero-is-refused`, adds `a-missing-new-price-is-refused`.
