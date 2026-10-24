---
id: the-checkout-tests-findings-are-closed
epic: guest-checkout
kind: task
status: approved
created: 2026-10-23T21:25:20Z
context: checkout
title: The checkout's open findings in its tests and test support are closed
closes: [only-an-offered-shipping-method-is-taken#1, the-confirmation-says-only-what-happened#1, the-confirmation-says-only-what-happened#2, the-shop-sets-the-shipping-price#2, the-steps-show-the-progress#1, an-open-checkout-keeps-its-price#1, an-open-checkout-keeps-its-price#2, each-payment-names-its-checkout#1, each-payment-names-its-checkout#2, the-payment-is-authorized-at-place-order#6, the-payment-is-authorized-at-place-order#7]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The checkout's open findings in its tests and test support are closed

Closes the open findings of the checkout context in its tests and test support that change nothing an actor sees: test support copied instead of reused, test names and leftovers, weak test data, raw locators, doc comments a rule change made wrong, code the change left without a caller, a rule or literal in two places, names that drifted from the glossary, a twin divergence nobody recorded. Each row's fix stands in its findings row; the delivering gate sets them done.

A change no actor sees: no acceptance criterion, no test stage. A row that turns out to change behaviour is a `## needs-human` of the plan, and goes into a story of its own.

## Notes

- WP-151 decision 4 (the user, 2026-10-23): the open rows as clean-up items, one per bounded context, cart and checkout split into tests and code, after the pipeline closes new findings at their source (dca-factory 0.91.0); every item depends on `the-take-over-leads-only-to-pages-of-the-shop`.
