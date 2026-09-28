---
id: CAT-03
epic: browse-catalogue
status: adopted
context: product
title: A product that does not exist
depends_on: [CAT-02, CAT-04]
---

# A product that does not exist

## Story

As a visitor who follows an outdated or mistyped product address I want the shop to tell me that the page
does not exist and offer me a way on, so that I am not left on a broken page.

## Acceptance criteria

<!-- happy-path: unknown-product-shows-the-not-found-page -->

### Rule: A product address no product has shows the not-found page

#### unknown-product-shows-the-not-found-page
- Given the seeded sample catalogue, in which no product has the id "no-such-product"
- When a visitor opens the product page `/products/no-such-product`
- Then the page shows "404" and the heading "Page Not Found"
- And the message reads "The page you're looking for doesn't exist or has been removed. Perhaps you were looking for one of our products?"

#### not-found-page-has-the-shop-title
- Given the seeded sample catalogue
- When a visitor opens the product page `/products/no-such-product`
- Then the browser tab title is "domaincentric.commerce"

### Rule: The not-found page offers a way on

#### browse-all-products-leads-to-the-catalogue
- Given a visitor on the not-found page of `/products/no-such-product`
- When they follow "Browse All Products"
- Then the catalogue page "Our Products" opens

#### go-to-homepage-leads-to-the-home-page
- Given a visitor on the not-found page of `/products/no-such-product`
- When they follow "Go to Homepage"
- Then the home page "Welcome to domaincentric.commerce" opens

## Out of scope

- An unknown product through the API or the assistant tools — `shop-api`, `catalogue-for-assistants`.

## Notes

- Inventory: product P8.
