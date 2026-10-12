---
id: a-site-of-the-shops-own-can-embed-it
epic: fits-its-surroundings
status: adopted
context: account
title: A site of the shop's own can embed it, a foreign site cannot
depends_on: [add-a-product-to-the-cart]
---

# A site of the shop's own can embed it, a foreign site cannot

## Story

As the shop owner I want a page of my own — a slide deck on another port of the shop's host — to frame the shop
and let visitors use it there, while a foreign site cannot use the shop inside its page unless I start the shop
for that, so that the shop appears where I put it and nowhere else.

## Acceptance criteria

<!-- happy-path: a-page-on-another-port-embeds-the-shop -->

### Rule: Out of the box, a page on the shop's own host may frame the shop and use it

#### a-page-on-another-port-embeds-the-shop
- Given the shop reached as `localhost` and a page served from another port of `localhost` that frames the catalogue
- When the visitor opens the first product inside the frame and presses "Add to Cart"
- Then the framed shop's cart page shows the product

### Rule: Out of the box, a page on another site cannot use the framed shop

#### a-foreign-site-cannot-add-to-the-cart
- Given the shop reached as `localhost` and a page on `127.0.0.1` that frames the catalogue
- When the visitor opens the first product inside the frame and presses "Add to Cart"
- Then the product does not reach the framed shop's cart

### Rule: A shop started for cross-site embedding works in a foreign site's frame

#### a-shop-started-for-embedding-works-cross-site
- Given a shop started for cross-site embedding, so that its cookies are sent as `SameSite=None; Secure`, reached as `localhost`, and a page on `127.0.0.1` that frames the catalogue
- When the visitor opens the first product inside the frame and presses "Add to Cart"
- Then the framed shop's cart page shows the product

### Rule: A shop started with framing switched off cannot be framed by any other page

#### a-shop-started-without-framing-refuses-the-frame
- Given a shop started with framing switched off and a page on another port of the shop's host that frames the catalogue
- When the visitor opens that page
- Then the shop's pages answer with `X-Frame-Options: SAMEORIGIN` and the browser shows nothing of the shop inside the frame

## Out of scope

- Which settings start the shop for cross-site embedding or without framing: that is the technical description's
  business, not the story's.
- Refusals a shop start makes for unsafe settings: a deployment concern with no shopper-visible behaviour.
- The form token itself — `the-shops-forms-refuse-posts-that-did`.

## Notes

- Replay story: `FIT-04` (fits-its-surroundings).
- Shared scenario: `scenario.embedding.same-site`, `scenario.embedding.cross-site`.
- Inventory: product P10; cart C-09; cross-cutting X4 (as it shows in a frame), X5 (framing part), X6, X7, X8.
