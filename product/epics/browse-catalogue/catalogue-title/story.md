---
id: catalogue-title
epic: browse-catalogue
status: delivered
delivered: 2026-10-12T20:45:03Z
delivered_by: gate
created: 2026-10-12T20:16:16Z
context: product
feature: catalogue
title: The catalogue page is titled "Product Catalog"
depends_on: []
---

# The catalogue page is titled "Product Catalog"

## Story

As a visitor I want the catalogue page's title to read "Product Catalog" so that the tab names the page.

## Acceptance criteria

### Rule: The catalogue page carries the catalogue's name as its title

#### catalogue-tab-reads-product-catalog (happy path)
Title: The catalogue page is titled Product Catalog
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then the page title is "Product Catalog"

## Notes

- The .NET shop delivered this criterion as a change story; here the behaviour exists and is adopted, so that both shops hold the same spec (WP-107).
