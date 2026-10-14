---
id: the-shop-sets-the-shipping-price
epic: guest-checkout
status: draft
context: checkout
feature: delivery
title: The shipping price is the price the shop offers
depends_on: []
---

# The shipping price is the price the shop offers

## Story

As the shop owner I want every order to carry the shipping price the shop offers for the chosen method, so that no
buyer sets their own shipping price by changing what their browser sends.

## Acceptance criteria

### Rule: The shipping price is the chosen method's price as the delivery step offers it, whatever the form carries

#### a-shipping-price-sent-with-the-form-is-not-taken (happy path)
- Given a guest who started the checkout with "Domain-Driven Design" once and is on the delivery step
- When the delivery form is sent with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and "Overnight Shipping", carrying the shipping price "0.00 EUR"
- Then the payment step opens and shows Subtotal "54.99 EUR", Shipping "19.99 EUR" and Total "74.98 EUR"

### Rule: A shipping method the delivery step does not offer is refused

#### an-unknown-shipping-method-is-refused
- Given a guest on the delivery step
- When the delivery form is sent with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and the shipping method "teleport"
- Then the delivery step opens again with the error "This shipping method is not offered. Please choose another one."

## Changed expectations

- A delivery form that carries a shipping price of its own can set the order's shipping price now; after, the order
  always carries the price the delivery step shows for the chosen method.
- A shipping method the delivery step does not offer is accepted now, or refused with "Please check the delivery
  address you entered.", which names the address; after, it is refused with "This shipping method is not offered.
  Please choose another one."

## Assumptions

- open: the wording of the refusal — "This shipping method is not offered. Please choose another one." (as written,
  after the payment step's "This way to pay is not offered. Please choose another one.")?
- open: the happy path already holds in one of the two shops (it reads the price from the shipping method there and
  ignores the form's price) — adopt it there, and build it only where it does not hold?

## Notes

- From TODO #91 (shipping cost from the client), checked 2026-10-15: still true in the Java shop (the delivery step
  takes name, estimate and price from the form); fixed in the .NET shop (only the method's id is read, the price comes
  from the shop's list; the form still carries the old fields, which nothing reads). The unknown-method refusal is
  red in both: the .NET shop answers it with the address message.
- Spec (`checkout/delivery`): adds `a-shipping-price-sent-with-the-form-is-not-taken` and
  `an-unknown-shipping-method-is-refused`; holds `four-shipping-methods-are-offered` and
  `address-and-shipping-open-the-payment-step`.
