---
id: fits-its-surroundings
title: A shop that fits its surroundings
intent: Customers shop on their phones and from partner sites, and a shop that only works on a desktop in its own tab loses them
goal: A customer uses the shop on a phone and in the look they choose; a site of the shop's own can embed it, a foreign site cannot
metric: an order placed from a phone
domain_contact: shop-owner
---

# A shop that fits its surroundings

## Intent

Customers shop on their phones and from partner sites, and a shop that only works on a desktop in its own tab loses them.

## Goal

A customer uses the shop on a phone and in the look they choose; a site of the shop's own can embed it, a foreign site cannot.

## Outcome

The epic is measured by an observable outcome, not an event: **an order placed from a phone**. The shop publishes no event for it today, and the replay reproduces today's shop, so none is added. As long as this does not happen in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- the-visitor-chooses-the-shops-look — The visitor chooses the shop's look
- moving-between-pages-is-animated — Moving between pages is animated
- the-shop-fits-a-phone — The shop fits a phone
- a-site-of-the-shops-own-can-embed-it — A site of the shop's own can embed it, a foreign site cannot
- the-shops-forms-refuse-posts-that-did — The shop's forms refuse posts that did not come from its pages
