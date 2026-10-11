---
id: homepage-discovery
title: Discovering products from the homepage
intent: Visitors to the homepage see products right away, not only categories
goal: More visitors get from the homepage to a product
metric: CartContentsChangedEvent
domain_contact: shop-owner
---

# Discovering products from the homepage

## Intent

The homepage shows a hero, the shop's promises and a list of categories — but no product. A visitor
has to go to the catalogue before they see anything they could buy.

## Outcome

The epic is measured by the outcome event `CartContentsChangedEvent`: a product lands in a cart. As
long as it is not published in production, the epic has not delivered, however much code exists.
The event does not yet tell apart whether the product was reached from the homepage; the domain
contact accepts that for now.

## Stories

- product-slider — Product slider on the homepage
