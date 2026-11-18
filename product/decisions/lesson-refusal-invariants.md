---
id: lesson-refusal-invariants
kind: lesson
lesson: refusal
key: invariants
stories: 10
asked: 2026-11-18T11:51:29Z
---

# The same gate refusal in 10 stories — invariants

## Question
Counted, never guessed: this refusal repeats in 10 stories. Where does its correction belong? The factory writes no rule and no knowledge itself — the skill that owns the place does, with the evidence below. No story waits for this answer.

## Evidence
- an-open-checkout-keeps-its-price: an-open-checkout-keeps-its-price journal: test gate refused invariants
- logging-in-from-the-checkout-returns-to-it: logging-in-from-the-checkout-returns-to-it journal: test gate refused invariants
- only-an-offered-shipping-method-is-taken: only-an-offered-shipping-method-is-taken journal: test gate refused invariants
- paying-again-requests-no-second-payment: paying-again-requests-no-second-payment journal: test gate refused invariants
- the-buyer-gets-an-order-confirmation: the-buyer-gets-an-order-confirmation journal: test gate refused invariants
- the-cart-holds-no-more-than-is-in-stock: the-cart-holds-no-more-than-is-in-stock journal: test gate refused invariants
- the-cart-oversight-is-a-feature-of-its-own: the-cart-oversight-is-a-feature-of-its-own journal: test gate refused invariants
- the-payment-is-authorized-at-place-order: the-payment-is-authorized-at-place-order journal: test gate refused invariants
- the-profile-refusal-names-today: the-profile-refusal-names-today journal: test gate refused invariants
- two-orders-never-sell-more-than-the-stock: two-orders-never-sell-more-than-the-stock journal: test gate refused invariants

## Options
- a: factory — the factory itself got this wrong — an issue text for its maintainers (`factory.sh lessons --issue lesson-refusal-invariants`, sent by a person)
- b: profile-check — a check the profile names, run at the gate (written by `/factory-setup`)
- c: instructions — a line of the project's agent instructions (written by `/dca-describe`)
- d: not-now — asked again once the evidence has grown
- e: never — the same candidate does not come back

## Recommendation
a — factory: the gate refuses the same check story after story; either the stages are not told what it holds, or the check refuses wrongly — the factory's maintainers look at both.
