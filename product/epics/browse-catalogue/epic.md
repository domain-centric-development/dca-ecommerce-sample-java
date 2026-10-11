---
id: browse-catalogue
title: Browse the catalogue
intent: People who want to buy from the shop have no way to see what it offers without contacting it
goal: A visitor browses the whole range in a stable order and opens any product to see what it is
metric: CartItemAddedToCart
domain_contact: shop-owner
---

# Browse the catalogue

## Intent

People who want to buy from the shop have no way to see what it offers without contacting it.

## Goal

A visitor browses the whole range in a stable order and opens any product to see what it is.

## Outcome

The epic is measured by the outcome event `CartItemAddedToCart` (browsing worked when a product went into a cart): as long as it is not published in production, the epic has not delivered, however much code exists.

## Stories

Story ids start with `CAT-`; delivery order is `depends_on`, ties by id.

- CAT-01 — The catalogue lists the range
- CAT-02 — The product page
- CAT-03 — A product that does not exist
- CAT-04 — The home page
- CAT-05 — Every page shares the shop's header and footer
