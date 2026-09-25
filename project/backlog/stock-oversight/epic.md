---
id: stock-oversight
title: Stock oversight for operators
intent: Operators notice that a product ran out only when a customer complains about it
goal: An operator sees which products are running low before they run out
metric: StockChanged
domain_contact: warehouse-lead
---

# Stock oversight for operators

## Intent

Nothing in the shop tells an operator that a product is running low. Stock is visible one product at
a time, so the only way to notice a gap is to look for it — and nobody looks until a customer
complains.

## Outcome

The epic is measured by the outcome event `StockChanged`: as long as an operator does not act on a
low stock level in production, the epic has not delivered, however much code exists.

## Stories

- STORY-P1 — Low stock overview
