---
id: lesson-finding-clean-code-test-duplication-extraneous
kind: lesson
lesson: finding
key: clean-code/test/duplication/extraneous
stories: 14
asked: 2026-11-18T11:51:29Z
---

# The same finding in 14 stories — clean-code/test/duplication/extraneous

## Question
Counted, never guessed: this finding repeats in 14 stories. Where does its correction belong? The factory writes no rule and no knowledge itself — the skill that owns the place does, with the evidence below. No story waits for this answer.

## Evidence
- a-guest-cart-not-owned-is-a-named-failure: a-guest-cart-not-owned-is-a-named-failure#2
- the-account-findings-are-closed: the-account-findings-are-closed#1
- remove-a-line-from-the-cart: remove-a-line-from-the-cart#2
- the-confirmation-says-only-what-happened: the-confirmation-says-only-what-happened#1
- an-operator-changes-a-price: an-operator-changes-a-price#3
- each-payment-names-its-checkout: each-payment-names-its-checkout#2
- a-malformed-product-id-is-a-bad-request: a-malformed-product-id-is-a-bad-request#1
- a-refused-api-login-is-a-problem-document: a-refused-api-login-is-a-problem-document#1
- an-unknown-product-in-the-body-is-unprocessable: an-unknown-product-in-the-body-is-unprocessable#2
- an-unusable-cart-request-is-a-problem-document: an-unusable-cart-request-is-a-problem-document#1
- created-product-answers-with-its-rounded-price: created-product-answers-with-its-rounded-price#1
- the-cart-oversight-totals-at-todays-prices: the-cart-oversight-totals-at-todays-prices#2
- a-second-login-takes-over-no-account-as-guest: a-second-login-takes-over-no-account-as-guest#3
- logging-in-from-the-checkout-returns-to-it: logging-in-from-the-checkout-returns-to-it#2
- contrast: the-cart-stays-with-the-person — the same target without this finding

## Options
- a: instructions — a line of the project's agent instructions (written by `/dca-describe`)
- b: factory — the factory itself got this wrong — an issue text for its maintainers (`factory.sh lessons --issue lesson-finding-clean-code-test-duplication-extraneous`, sent by a person)
- c: not-now — asked again once the evidence has grown
- d: never — the same candidate does not come back

## Recommendation
a — instructions: no rule can see it; a line of the agent instructions tells the stages before they write it.
