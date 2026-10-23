---
id: continue-takes-over-only-what-the-page-showed
epic: shop-with-an-account
status: draft
created: 2026-10-23T21:25:20Z
context: cart
feature: cartrecovery
title: Continue takes over only what the merge page showed
closes: [taking-over-the-guest-cart-is-a-step-of-its-own#1]
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# Continue takes over only what the merge page showed

## Story

As a customer who logged in with a guest cart I want "Continue" to do what the page showed me, so that a cart that filled meanwhile is not merged without my choice.

## Acceptance criteria

### Rule: "Continue" carries out the choice only on the carts the page showed

#### a-cart-filled-after-the-page-asks-again (happy path)
- Given an account whose cart is empty, and a guest cart holding "Clean Architecture" once
- And the guest logged in and is on the page that takes the guest cart over
- And meanwhile, in another browser, "Domain-Driven Design" was added to the account's cart
- When they press "Continue"
- Then the merge page shows both carts and asks for a choice
- And the account's cart still holds only "Domain-Driven Design"

## Assumptions

- open: the account cart filled after the page was shown — the merge page shows the choice again, or the take-over is refused with a notice?

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- Finding group 8 (a precondition the command does not check again after an earlier read), the same miss in both shops (J82 = N77); catalog pitfall `a-command-decided-on-an-earlier-read`.
