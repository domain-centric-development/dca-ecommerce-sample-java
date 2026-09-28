---
id: CAT-04
epic: browse-catalogue
status: adopted
context: portal
title: The home page
depends_on: [CAT-01]
---

# The home page

## Story

As a visitor arriving at the shop's address I want a home page that says what the shop sells and leads me to
the products, so that I know within seconds whether I am in the right place.

## Acceptance criteria

<!-- happy-path: home-page-welcomes-the-visitor -->

### Rule: The home page says what the shop is

#### home-page-welcomes-the-visitor
- Given the shop has started
- When a visitor opens the home page `/`
- Then the heading reads "Welcome to domaincentric.commerce"
- And the subtitle reads "Books, modelling supplies and hexagon merchandise for people who draw boundaries"
- And below it reads "Everything this architecture is made of: the books it grew out of, the supplies a design workshop runs on, and hexagons for your desk."

#### home-page-title
- Given the shop has started
- When a visitor opens the home page
- Then the browser tab title is "domaincentric.commerce"

### Rule: The home page leads to the products and the cart

#### browse-products-opens-the-catalogue
- Given a visitor on the home page
- When they follow "Browse Products"
- Then the catalogue page "Our Products" opens

#### view-cart-links-to-the-cart
- Given the shop has started
- When a visitor opens the home page
- Then the section below the heading offers a "View Cart" link to the cart page `/cart`

### Rule: The home page explains why to shop here

#### why-shop-with-us
- Given the shop has started
- When a visitor opens the home page
- Then a section "Why Shop With Us" shows four features:
  "Free Shipping" — "Books ship cushioned, posters rolled in a tube, hexagons in a fitted box. Free over €50.";
  "Secure Payments" — "Card or invoice, encrypted end to end. Your payment details never touch our order history.";
  "Built to Last" — "Beech and oiled oak, hard enamel, heavyweight cotton. Objects that survive a decade of workshops.";
  "Easy Returns" — "Not the hexagon you pictured? Send any item back within 30 days for a full refund."

#### popular-categories
- Given the shop has started
- When a visitor opens the home page
- Then a section "Popular Categories" shows, as text and not as links:
  "Books" — "The works this architecture was synthesized from — Evans, Vernon, Cockburn, Fowler, Martin.";
  "Modeling" — "Sticky notes, hexagon magnets, posters and card decks for your next design workshop.";
  "Apparel" — "Shirts, hoodies and caps that explain your architecture before you open your laptop.";
  "Desk & Office" — "Mugs, coasters, a hex-grid notebook, and the wooden hexagon for your desk.";
  "Stickers & Pins" — "Small enough for a laptop lid, loud enough for a conference hallway."

### Rule: The page closes with a call to shop

#### shop-now-opens-the-catalogue
- Given a visitor on the home page, below "Ready to Draw Some Boundaries?" and "Browse the full catalog: the books, the modelling supplies, and the hexagons."
- When they follow "Shop Now"
- Then the catalogue page "Our Products" opens

## Out of scope

- The cart page behind "View Cart" — `CRT-02`.
- The header and footer every page shares — `CAT-05`.
- A product slider on the home page: not part of the shop today.
- The theme switcher and the phone layout — `fits-its-surroundings`.

## Notes

- Inventory: portal H1, H2, H3, H4, H5.
