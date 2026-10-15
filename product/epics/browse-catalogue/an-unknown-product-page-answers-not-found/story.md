---
id: an-unknown-product-page-answers-not-found
epic: browse-catalogue
status: delivered
delivered: 2026-10-15T15:28:14Z
delivered_by: gate
context: product
feature: productdetails
title: The page of an unknown product answers "not found"
depends_on: []
---

# The page of an unknown product answers "not found"

## Story

As a visitor, a search engine or a link checker I want the address of a product the shop does not have to answer
"not found", so that a dead link is recognised as one and not kept as a page of the shop.

## Acceptance criteria

### Rule: A product address the catalogue has no product for answers with status 404 and the not-found page

#### an-unknown-product-answers-404 (happy path)
- Given the catalogue has no product with the id "00000000-0000-4000-8000-000000000001"
- When a visitor opens `/products/00000000-0000-4000-8000-000000000001`
- Then the shop answers with status 404
- And the page shows "404", "Page Not Found" and "The page you're looking for doesn't exist or has been removed.
  Perhaps you were looking for one of our products?"

#### a-malformed-product-address-answers-404
- Given a visitor with no cookies
- When they open `/products/no-such-product`
- Then the shop answers with status 404
- And the page shows "Page Not Found"

## Changed expectations

- The not-found page of an unknown product answers with status 200 now, where the shop answers it so; after, always
  with 404. What the page shows stays the same.

## Assumptions

- answered: adopted in the shop where both scenarios already hold, built in the other (planning/questions-2026-10-15.md
  Q4.1)

## Notes

- From TODO #94 (d), checked 2026-10-15: still true in the Java shop (status 200 for an unknown and for a malformed
  id); fixed in the .NET shop (404 for both). Hence `approved` in the Java shop and `adopted` in the .NET shop.
- Spec (`product/productdetails`): adds `an-unknown-product-answers-404` and `a-malformed-product-address-answers-404`;
  holds `unknown-product-shows-the-not-found-page` and `not-found-page-has-the-shop-title`.
