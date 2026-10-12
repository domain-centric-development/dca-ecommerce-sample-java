---
id: shop-with-an-account
title: Shop with an account
intent: Customers who log in halfway through lose the cart they already filled, and give up
goal: A customer logs in or registers at any point without losing the cart they filled, keeps an open checkout when registering, and decides how two carts come together
metric: CartCompleted
domain_contact: shop-owner
---

# Shop with an account

## Intent

Customers who log in halfway through lose the cart they already filled, and give up.

## Goal

A customer logs in or registers at any point without losing the cart they filled, keeps an open checkout when registering, and decides how two carts come together.

## Outcome

The epic is measured by the outcome event `CartCompleted`: as long as it is not published in production, the epic has not delivered, however much code exists.

## Journey

A guest fills a cart, starts the checkout, registers from the buyer step, returns to the buyer step with the open checkout, completes the order and sees the confirmation, logged in. A break here loses exactly the customers who decided to stay.

## Stories

Delivery order is `depends_on`, ties by id.

- logging-in-brings-the-guest-cart-along — Logging in brings the guest cart along
- the-cart-stays-with-the-person — The cart stays with the person through registration, logout and a lapsed login
- choose-how-two-carts-come-together — Choose how two carts come together
- the-buyer-step-knows-who-is-buying — The buyer step knows who is buying
