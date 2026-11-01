---
id: cart-refusals-name-the-field-in-the-shops-words
epic: shop-api
status: draft
created: 2026-11-01T12:00:08Z
context: cart
feature: shopping
title: Cart refusals name the field in the shop's words
depends_on: [product-refusals-name-the-field-in-the-shops-words]
---

# Cart refusals name the field in the shop's words

## Story

As a client filling a cart — through the API or the page — I want a refused quantity to name the field, with a code and a message
in the shop's words, so that I can correct the request without reading framework messages.

## Acceptance criteria

`errors` holds one entry under the field as the request spells it: `{ "code": <code>, "message": <message> }`,
the shape the product refusals answer with.

### Rule: A quantity is at least 1

#### a-quantity-of-zero-is-refused (happy path)
- Given Ada's open, empty cart
- When her client adds "Domain-Driven Design" with quantity 0
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `quantity` with code `quantity-not-positive` and the message "A quantity is at least 1."
- And her cart is still empty

#### a-missing-quantity-is-refused
- Given Ada's open, empty cart
- When her client sends `POST /api/carts/<cartId>/items` with `{"productId":"<id of Domain-Driven Design>"}`
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `quantity` with code `quantity-missing` and the message "Please enter a quantity."
- And her cart is still empty

### Rule: An "Add to Cart" without a quantity adds nothing

#### a-missing-quantity-on-the-page-is-refused
- Given a guest with an empty cart
- When their browser sends the "Add to Cart" form of "Domain-Driven Design" without a quantity
- Then they are on the cart page at `/cart`
- And the page shows the message "Please enter a valid quantity."
- And the cart is empty

## Changed expectations

- A quantity of zero is refused today with `errors` naming the quantity and a reason the framework words; after,
  with the code and message of its rule.
- An item without a quantity is refused today as a quantity of zero; after, as a missing quantity.
- An "Add to Cart" form without a quantity puts one unit into the cart today in one shop and is refused in the
  other; after, both refuse it with "Please enter a valid quantity." and the cart stays as it was.

## Assumptions

- answered: every refusal of a value from input names its field, with a code in the domain's words and a message
  in the shop's words (the person, 2026-11-01, WP-170 decision 4)
- answered: one named failure per rule of the value — missing (null or blank alike), too long, malformed, out of
  range — each carrying the figures it compared (the person, 2026-11-01, WP-170 decision 5)
- answered: the rule is checked once, in the value object; the request carries no annotation repeating it and no
  default turning a missing value into one; the message is the adapter's, never the exception's (WP-170
  decisions 1 and 2)

## Notes

- WP-170 (a rule is checked once, in the value object): `AddToCartRequest` checks the quantity with request
  validation and the quantity value checks it again.
- A missing or malformed product id stays a malformed call the adapter answers itself ("Unacceptable value", no
  detail): it is the shape of the request, not a rule of the cart.
- The product page's "Add to Cart" form carries `quantity` 1 itself; a form without it is a hand-made request. The
  page adapter passes the missing quantity on as missing, the quantity value refuses it (the person, 2026-11-01).
- Spec (`cart/shopping`): changes `a-quantity-of-zero-is-refused`, adds `a-missing-quantity-is-refused` and, under
  a new rule, `a-missing-quantity-on-the-page-is-refused`.
