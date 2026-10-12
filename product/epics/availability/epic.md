---
id: availability
title: Availability before adding
intent: Customers order products that are no longer in stock and have to be told afterwards
goal: A customer sees whether a product is available before adding it, and cannot put an unavailable one in the cart
metric: StockChanged
domain_contact: warehouse-lead
---

# Availability before adding

## Intent

Customers order products that are no longer in stock and have to be told afterwards.

## Goal

A customer sees whether a product is available before adding it, and cannot put an unavailable one in the cart.

## Outcome

The epic is measured by the outcome event `StockChanged`: as long as it is not published in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- availability-shown — Availability shown
