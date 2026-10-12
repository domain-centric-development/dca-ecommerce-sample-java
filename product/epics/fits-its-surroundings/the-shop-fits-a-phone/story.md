---
id: the-shop-fits-a-phone
epic: fits-its-surroundings
status: adopted
context: portal
feature: appearance
title: The shop fits a phone
depends_on: [CAT-02, the-cart-page, start-the-checkout-from-the-cart]
---

# The shop fits a phone

## Story

As a shopper on a phone I want the catalogue, a product, the cart and the checkout to fit my screen, so that I can
shop to the end without scrolling sideways.

## Acceptance criteria

<!-- happy-path: the-catalogue-fits-a-phone -->

### Rule: At 393 px wide, no page on the way to the checkout scrolls sideways

#### the-catalogue-fits-a-phone
- Given a 393 px wide viewport and the seeded sample catalogue
- When the visitor opens the catalogue page
- Then the page does not scroll sideways

#### a-product-page-fits-a-phone
- Given a 393 px wide viewport
- When the visitor opens the product page of "Patterns of Enterprise Application Architecture"
- Then the page does not scroll sideways

#### the-cart-fits-a-phone
- Given a 393 px wide viewport and "Domain-Driven Design" added to the cart
- When the visitor opens the cart page
- Then the page does not scroll sideways

#### the-buyer-step-fits-a-phone
- Given a 393 px wide viewport and a cart holding "Domain-Driven Design"
- When the visitor presses "Proceed to Checkout"
- Then the buyer step opens and does not scroll sideways

## Out of scope

- Account pages, login, registration and back-office pages at phone width: not held to it today.
- The home page product slider: not part of the replay.

## Notes

- Replay story: `FIT-03` (fits-its-surroundings).
- Shared scenario: `scenario.layout.phone`.
- Inventory: product P9; cart C-28; checkout 50; cross-cutting X18.
