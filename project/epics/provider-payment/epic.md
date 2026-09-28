---
id: provider-payment
title: Payment through the payment provider
intent: The shop takes every payment through a stand-in inside the application, so no order is ever paid through the provider, and a refusal or an outage cannot happen
goal: A customer pays through the payment provider, and learns at once when the provider refuses the payment or cannot be reached
metric: PaymentSubmitted
domain_contact: checkout-lead
---

# Payment through the payment provider

## Intent

Checkout hands every payment to a stand-in inside the application that approves everything. No
payment ever reaches the provider, so a refused card or a provider that does not answer is never
seen — not by the customer, and not by the code.

## Outcome

The epic is measured by the outcome event `PaymentSubmitted`: as long as it is not published for a
payment the provider authorized, the epic has not delivered.

## Journey

- open: the flow from the cart through payment to the confirmation, once the provider is the one that authorizes it

## Stories

- pay-through-the-provider — Pay through the payment provider
