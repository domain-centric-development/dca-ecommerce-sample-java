---
id: prices
title: Prices from their owner
intent: The same product is quoted at different prices depending on where a customer looks, and nobody can say which one is right
goal: A customer sees the same price for a product on every page and in every answer
metric: PriceCreated
domain_contact: shop-owner
---

# Prices from their owner

## Intent

The same product is quoted at different prices depending on where a customer looks, and nobody can say which one is right.

## Goal

A customer sees the same price for a product on every page and in every answer.

## Outcome

The epic is measured by the outcome event `PriceCreated`: as long as it is not published in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- prices-shown-from-the-price-owner — Prices shown from the price owner
