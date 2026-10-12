---
id: moving-between-pages-is-animated
epic: fits-its-surroundings
status: adopted
context: portal
feature: appearance
title: Moving between pages is animated
depends_on: [CAT-02, start-the-checkout-from-the-cart]
---

# Moving between pages is animated

## Story

As a visitor I want moving from page to page to feel like one continuous shop, and to be spared the motion when
I have asked my device for less of it, so that the shop is pleasant to use and does not make me dizzy.

## Acceptance criteria

<!-- happy-path: the-frame-stays-while-the-page-changes -->

### Rule: Page changes are animated where the browser supports transitions between pages

#### the-frame-stays-while-the-page-changes
- Given a browser that supports animated transitions between pages
- When the visitor follows "View Details" from the catalogue to "Domain-Driven Design"
- Then the page content changes with a fade, while the header, the logo and the footer stay in place

#### the-product-image-carries-over
- Given a browser that supports animated transitions between pages
- When the visitor follows "View Details" on the card "Domain-Driven Design"
- Then the product's image moves from its place on the card to its place on the product page

#### the-checkout-progress-stays-anchored
- Given a browser that supports animated transitions between pages and a checkout on the buyer step
- When the visitor continues to the delivery step
- Then the checkout progress bar stays in place while the step's form changes

### Rule: Motion is switched off for a visitor who prefers reduced motion

#### reduced-motion-means-no-animation
- Given a browser that supports animated transitions between pages and whose user prefers reduced motion
- When the visitor follows "View Details" from the catalogue to "Domain-Driven Design"
- Then the product page appears without any animation

## Out of scope

- Themes — `the-visitor-chooses-the-shops-look`.
- Browsers without transitions between pages: they change pages as they always do.

## Notes

- Replay story: `FIT-02` (fits-its-surroundings).
- Inventory: cross-cutting X17.
