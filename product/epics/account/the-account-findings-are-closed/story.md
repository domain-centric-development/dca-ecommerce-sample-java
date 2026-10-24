---
id: the-account-findings-are-closed
epic: account
kind: task
status: approved
created: 2026-10-23T21:25:20Z
context: account
title: The account's open findings are closed
closes: [login-leads-only-to-shop-pages#1, login-leads-only-to-shop-pages#2, registration-leads-only-to-shop-pages#1, registration-leads-only-to-shop-pages#2, registration-says-the-account-was-created#1, registration-says-the-account-was-created#2, a-date-of-birth-lies-in-the-past#1, a-date-of-birth-lies-in-the-past#2, a-date-of-birth-lies-in-the-past#3, a-date-of-birth-lies-in-the-past#4, a-date-of-birth-lies-in-the-past#5, a-refused-api-login-is-a-problem-document#1, a-refused-api-login-is-a-problem-document#2]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The account's open findings are closed

Closes the open findings of the account context that change nothing an actor sees: test support copied instead of reused, test names and leftovers, weak test data, raw locators, doc comments a rule change made wrong, code the change left without a caller, a rule or literal in two places, names that drifted from the glossary, a twin divergence nobody recorded. Each row's fix stands in its findings row; the delivering gate sets them done.

A change no actor sees: no acceptance criterion, no test stage. A row that turns out to change behaviour is a `## needs-human` of the plan, and goes into a story of its own.

## Notes

- WP-151 decision 4 (the user, 2026-10-23): the open rows as clean-up items, one per bounded context, cart and checkout split into tests and code, after the pipeline closes new findings at their source (dca-factory 0.91.0); every item depends on `the-take-over-leads-only-to-pages-of-the-shop`.
