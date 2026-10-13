---
id: not-found-title
epic: browse-catalogue
status: delivered
delivered: 2026-10-12T21:25:50Z
delivered_by: gate
context: portal
title: The not-found page carries the shop's name as its title
depends_on: []
---

# The not-found page carries the shop's name as its title

## Story

As a visitor who lands on a page that does not exist I want the tab to still name the shop, so that I
still know which site I am on.

## Acceptance criteria

### Rule: The not-found page carries the shop's name as its title

#### not-found-tab-reads-the-shop-name (happy path)
Title: The not-found page is titled with the shop's name
- Given the seeded sample catalogue, in which no product has the id "no-such-product"
- When a visitor opens the product page `/products/no-such-product`
- Then the browser tab title is "domaincentric.commerce"

## Notes

- The .NET shop delivered this criterion as a change story; here the behaviour exists and is adopted, so that both shops hold the same spec (WP-107).
