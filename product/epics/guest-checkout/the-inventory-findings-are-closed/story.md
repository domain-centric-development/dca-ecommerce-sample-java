---
id: the-inventory-findings-are-closed
epic: guest-checkout
kind: task
status: draft
created: 2026-10-23T21:25:20Z
context: inventory
title: The inventory's open findings are closed
closes: [two-orders-never-sell-more-than-the-stock#1, two-orders-never-sell-more-than-the-stock#2, two-orders-never-sell-more-than-the-stock#3, two-orders-never-sell-more-than-the-stock#5, two-orders-never-sell-more-than-the-stock#6]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The inventory's open findings are closed

Closes the open findings of the inventory context that change nothing an actor sees: test support copied instead of reused, test names and leftovers, weak test data, raw locators, doc comments a rule change made wrong, code the change left without a caller, a rule or literal in two places, names that drifted from the glossary, a twin divergence nobody recorded. Each row's fix stands in its findings row; the delivering gate sets them done.

A change no actor sees: no acceptance criterion, no test stage. A row that turns out to change behaviour is a `## needs-human` of the plan, and goes into a story of its own.

## Notes

- WP-151 decision 4 (the user, 2026-10-23): the open rows as clean-up items, one per bounded context, cart and checkout split into tests and code, after the pipeline closes new findings at their source (dca-factory 0.91.0); every item depends on `the-take-over-leads-only-to-pages-of-the-shop`.
