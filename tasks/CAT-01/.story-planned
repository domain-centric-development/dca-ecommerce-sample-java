---
id: CAT-01
epic: browse-catalogue
status: adopted
context: product
title: The catalogue lists the range
depends_on: []
---

# The catalogue lists the range

## Story

As a visitor I want to see every product the shop sells on one catalogue page, in an order that does not
change between visits, so that I can find out what the shop offers without asking.

## Acceptance criteria

<!-- happy-path: catalogue-lists-the-seeded-range -->

### Rule: The shop starts with its sample catalogue

#### catalogue-lists-the-seeded-range
- Given the shop has started and seeded the sample catalogue of `seed-catalogue.json`
- When a visitor opens the catalogue page
- Then it shows 21 product cards

### Rule: Products are listed by name, compared character by character

#### catalogue-is-in-name-order
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then the card titles are in ordinal order of the product name
- And the first card is `"Bounded Context" Enamel Pin`

### Rule: A card shows what the product is

#### card-shows-name-description-and-image
- Given the seeded product "Domain-Driven Design"
- When a visitor opens the catalogue page
- Then its card shows the name "Domain-Driven Design", its description and its image

#### card-leads-to-the-product
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then every card offers a "View Details" link to that product's page

### Rule: The page is recognisable as the catalogue

#### catalogue-page-heading
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then the page title is "Product Catalog", the heading reads "Our Products" and the breadcrumb reads "Home / Products"

## Out of scope

- The price on a card — `PRC-01`, from the price owner.
- The "In Stock" / "Out of Stock" badge — `AVL-01`.
- The product page behind "View Details" — `CAT-02`.
- The initial shown for a product without an image: every seeded product has one; a product without an image
  can only be created through the API — `shop-api`.
- A catalogue with no products: the shop always starts with its sample catalogue, so the empty page is not
  reachable from outside.

## Notes

- Shared scenario: `scenario.catalog.seeded-in-name-order`.
- Inventory: product P1, P3, P4 (name, description, image and link part).
