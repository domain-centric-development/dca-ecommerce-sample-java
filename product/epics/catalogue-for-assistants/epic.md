---
id: catalogue-for-assistants
title: The catalogue for AI assistants
intent: Customers ask an AI assistant what to buy, and the assistant knows nothing about this shop
goal: An assistant answers what the shop sells, with price and availability, from the shop's own data
metric: an assistant's answer names a product with its price and stock
domain_contact: shop-owner
---

# The catalogue for AI assistants

## Intent

Customers ask an AI assistant what to buy, and the assistant knows nothing about this shop.

## Goal

An assistant answers what the shop sells, with price and availability, from the shop's own data.

## Outcome

The epic is measured by an observable outcome, not an event: **an assistant's answer names a product with its price and stock**. The shop publishes no event for it today, and the replay reproduces today's shop, so none is added. As long as this does not happen in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- an-assistant-reads-the-catalogue — An assistant reads the catalogue through tools
