---
id: guest-checkout
title: Check out as a guest
intent: Customers who have made up their mind still cannot place an order, and first-time buyers will not create an account just to try the shop
goal: A customer places an order in one sitting, without an account, and knows it went through; what they did not buy stays in the cart
metric: CheckoutConfirmed
domain_contact: shop-owner
---

# Check out as a guest

## Intent

Customers who have made up their mind still cannot place an order, and first-time buyers will not create an account just to try the shop.

## Goal

A customer places an order in one sitting, without an account, and knows it went through; what they did not buy stays in the cart.

## Outcome

The epic is measured by the outcome event `CheckoutConfirmed`: as long as it is not published in production, the epic has not delivered, however much code exists.

## Journey

A guest with a product in the cart starts the checkout, enters buyer details, a delivery address and a shipping method, chooses a payment provider, places the order from the review and sees the confirmation; afterwards the cart no longer holds what was bought and the product's stock is lower by the quantity bought. A break here means the shop takes no orders at all.

## Stories

Delivery order is `depends_on`, ties by id.

- start-the-checkout-from-the-cart — Start the checkout from the cart
- enter-the-buyer-details — Enter the buyer details
- choose-the-delivery-address — Choose the delivery address and the shipping method
- choose-how-to-pay — Choose how to pay
- place-the-order-from-the-review-and-see — Place the order from the review and see the confirmation
- the-steps-show-the-progress — The steps show the progress and are taken in order
- go-back-to-an-earlier-step-and-change-it — Go back to an earlier step and change it
- without-an-open-checkout-the-steps-lead — Without an open checkout the steps lead to the cart
- the-open-checkout-keeps-its-order-while — The open checkout keeps its order while the cart changes
- after-the-order-the-cart-keeps-only — After the order the cart keeps only what was not bought
- stock-goes-down-by-what-was-bought — Stock goes down by what was bought
- starting-and-placing-check-the-cart — Starting and placing check the cart against what can still be sold
- a-visitor-never-reaches-another-visitors — A visitor never reaches another visitor's checkout
