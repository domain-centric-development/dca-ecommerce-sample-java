---
id: prices-shown-from-the-price-owner
epic: prices
status: adopted
context: pricing
title: Prices shown from the price owner
depends_on: [CAT-01, CAT-02]
---

# Prices shown from the price owner

## Story

As a visitor I want to see a product's price on its catalogue card and on its product page, and the same
price in both places, so that I know what it costs before I decide.

## Acceptance criteria

<!-- happy-path: card-shows-the-price -->

### Rule: A product starts with the price it was created with

#### card-shows-the-price
- Given the seeded product "Domain-Driven Design", created with the price 54.99 EUR
- When a visitor opens the catalogue page
- Then its card shows the price "54.99 EUR"

#### product-page-shows-the-retail-price
- Given the seeded product "Domain-Driven Design", created with the price 54.99 EUR
- When a visitor opens its product page
- Then the page shows the price "54.99 EUR" labelled "Retail Price"

### Rule: A price is shown as its amount with two decimals, followed by the currency code

#### price-keeps-two-decimals
- Given the seeded product "Hexagon Sticker Sheet", created with the price 9.99 EUR
- When a visitor opens the catalogue page
- Then its card shows the price "9.99 EUR"

### Rule: Every product shows its own price, the same on card and page

#### every-card-shows-its-seeded-price
- Given the seeded sample catalogue of `seed-catalogue.json`
- When a visitor opens the catalogue page
- Then each of the 21 cards shows the price its product was seeded with, for example "Wooden Hexagon Desk Model" "39.99 EUR" and `"Bounded Context" Enamel Pin` "12.99 EUR"

#### page-and-card-agree
- Given the seeded product "Patterns of Enterprise Application Architecture", whose card shows "59.99 EUR"
- When a visitor opens its product page
- Then the product page shows the same price "59.99 EUR"

## Out of scope

- The price in the cart and in the checkout — `fill-a-cart`, `guest-checkout`.
- The price in the API and the assistant tools — `shop-api`, `catalogue-for-assistants`.
- The rounding and the limits of a price given when a product is created: a price other than the seeded ones can
  only be given through the API — `shop-api`.
- Changing a price: no page, API or tool offers it today.
- A product whose price has not arrived yet: the shop does not show such a product in a state anyone can rely on
  reaching from outside.

## Notes

- Replay story: `PRC-01` (prices).
- Inventory: pricing R1, R2; product P2 (price part), P4 (price), P6 (price and "Retail Price").
