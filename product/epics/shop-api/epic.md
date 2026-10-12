---
id: shop-api
title: Shop through the API
intent: Partners who want to offer the range in their own applications have to copy it by hand
goal: A partner application signs in, reads and creates products and runs its own carts, without the shop's pages
metric: CartItemAddedToCart
domain_contact: integration-partner
---

# Shop through the API

## Intent

Partners who want to offer the range in their own applications have to copy it by hand.

## Goal

A partner application signs in, reads and creates products and runs its own carts, without the shop's pages.

## Outcome

The epic is measured by the outcome event `CartItemAddedToCart` (published when the item was added through the API): as long as it is not published in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- register-through-the-api — Register through the API
- log-in-and-out-through-the-api — Log in and out through the API
- read-the-catalogue-through-the-api — Read the catalogue through the API
- staff-create-a-product-through-the-api — Staff create a product through the API
- creating-a-product-refuses-a-bad-sku — Creating a product refuses a bad SKU, name or text
- creating-a-product-holds-its-price — Creating a product holds its price and stock to the rules
- open-and-fill-a-cart-through-the-api — Open and fill a cart through the API
- only-your-own-cart-through-the-api — Only your own cart through the API
- the-cart-api-refuses-what-the-cart — The cart API refuses what the cart cannot take
- the-api-trusts-only-the-token — The API trusts only the token
