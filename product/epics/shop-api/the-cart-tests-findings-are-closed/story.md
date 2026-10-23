---
id: the-cart-tests-findings-are-closed
epic: shop-api
kind: task
status: draft
created: 2026-10-23T21:25:20Z
context: cart
title: The cart's open findings in its tests and test support are closed
closes: [remove-a-line-from-the-cart#2, remove-a-line-from-the-cart#3, a-malformed-product-id-is-a-bad-request#1, an-unknown-product-in-the-body-is-unprocessable#2, an-unusable-cart-request-is-a-problem-document#1, the-cart-oversight-totals-at-todays-prices#2, logging-in-from-the-checkout-returns-to-it#2, only-the-browsers-own-guest-cart-is-taken-over#5]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The cart's open findings in its tests and test support are closed

Closes the open findings of the cart context in its tests and test support that change nothing an actor sees: test support copied instead of reused, test names and leftovers, weak test data, raw locators, doc comments a rule change made wrong, code the change left without a caller, a rule or literal in two places, names that drifted from the glossary, a twin divergence nobody recorded. Each row's fix stands in its findings row; the delivering gate sets them done.

A change no actor sees: no acceptance criterion, no test stage. A row that turns out to change behaviour is a `## needs-human` of the plan, and goes into a story of its own.

## Notes

- WP-151 decision 4 (the user, 2026-10-23): the open rows as clean-up items, one per bounded context, cart and checkout split into tests and code, after the pipeline closes new findings at their source (dca-factory 0.91.0); every item depends on `the-take-over-leads-only-to-pages-of-the-shop`.
