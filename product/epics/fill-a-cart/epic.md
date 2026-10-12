---
id: fill-a-cart
title: Fill a cart
intent: Customers who want several products have to keep them in mind until they decide to buy
goal: A customer collects products in a cart that stays with them across pages and visits, and sees what it will cost
metric: CartItemAddedToCart
domain_contact: shop-owner
---

# Fill a cart

## Intent

Customers who want several products have to keep them in mind until they decide to buy.

## Goal

A customer collects products in a cart that stays with them across pages and visits, and sees what it will cost.

## Outcome

The epic is measured by the outcome event `CartItemAddedToCart`: as long as it is not published in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- add-a-product-to-the-cart — Add a product to the cart
- the-cart-page — The cart page
- the-header-basket — The header basket
- the-cart-stays-with-the-guest — The cart stays with the guest
- stock-warnings-in-the-cart — Stock warnings in the cart
