---
id: only-an-offered-shipping-method-is-taken
epic: guest-checkout
status: approved
context: checkout
feature: delivery
title: A shipping method the delivery step does not offer is refused
depends_on: [the-shop-sets-the-shipping-price]
---

# A shipping method the delivery step does not offer is refused

## Story

As a buyer whose browser sent a shipping method the shop does not offer I want to be told so and asked to choose
again, so that I am not refused for an address that is right.

## Acceptance criteria

### Rule: A shipping method the delivery step does not offer is refused

#### an-unknown-shipping-method-is-refused (happy path)
- Given a guest on the delivery step
- When the delivery form is sent with Street Address "Hauptstrasse 1", City "Berlin", Postal Code "10115", Country
  "Germany" and the shipping method "teleport"
- Then the delivery step opens again with the error "This shipping method is not offered. Please choose another one."

## Out of scope

- The shipping price of an offered method: story `the-shop-sets-the-shipping-price`.

## Changed expectations

- A shipping method the delivery step does not offer is accepted now, or refused with "Please check the delivery
  address you entered.", which names the address; after, it is refused with "This shipping method is not offered.
  Please choose another one."

## Assumptions

- answered: the refusal reads "This shipping method is not offered. Please choose another one."
  (planning/questions-2026-10-15.md Q4.3)

## Notes

- From TODO #91, checked 2026-10-15: red in both shops — the Java shop takes any method the form names, the .NET shop
  answers it with the address message. Built in both (planning/questions-2026-10-15.md Q4.1).
- Split from `the-shop-sets-the-shipping-price`, whose scenario already holds in the .NET shop.
- Depends on `the-shop-sets-the-shipping-price`: both change how the delivery step reads the chosen method.
- Spec (`checkout/delivery`): adds `an-unknown-shipping-method-is-refused`; holds `four-shipping-methods-are-offered`
  and `address-and-shipping-open-the-payment-step`.
