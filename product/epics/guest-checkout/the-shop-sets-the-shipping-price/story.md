---
id: the-shop-sets-the-shipping-price
epic: guest-checkout
status: approved
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

## Out of scope

- A shipping method the delivery step does not offer: story `only-an-offered-shipping-method-is-taken`.

## Changed expectations

- A delivery form that carries a shipping price of its own can set the order's shipping price now, where the shop
  takes the price from the form; after, the order always carries the price the delivery step shows for the chosen
  method.

## Assumptions

- answered: adopted in the shop where the scenario already holds, built in the other (planning/questions-2026-10-15.md
  Q4.1)

## Notes

- From TODO #91 (shipping cost from the client), checked 2026-10-15: still true in the Java shop (the delivery step
  takes name, estimate and price from the form); fixed in the .NET shop (only the method's id is read, the price comes
  from the shop's list; the form still carries the old fields, which nothing reads). Hence `approved` in the Java shop
  and `adopted` in the .NET shop.
- The refusal of a method the step does not offer is red in both shops, so it is a story of its own, built in both:
  `only-an-offered-shipping-method-is-taken`.
- Spec (`checkout/delivery`): adds `a-shipping-price-sent-with-the-form-is-not-taken`; holds
  `four-shipping-methods-are-offered` and `address-and-shipping-open-the-payment-step`.
