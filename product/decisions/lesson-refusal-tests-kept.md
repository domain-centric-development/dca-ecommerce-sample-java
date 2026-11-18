---
id: lesson-refusal-tests-kept
kind: lesson
lesson: refusal
key: tests-kept
stories: 11
asked: 2026-11-18T11:51:29Z
---

# The same gate refusal in 11 stories — tests-kept

## Question
Counted, never guessed: this refusal repeats in 11 stories. Where does its correction belong? The factory writes no rule and no knowledge itself — the skill that owns the place does, with the evidence below. No story waits for this answer.

## Evidence
- a-missing-review-fingerprint-is-the-models-answer: a-missing-review-fingerprint-is-the-models-answer journal: build gate refused tests-kept
- an-early-step-says-what-is-still-open: an-early-step-says-what-is-still-open journal: test gate refused tests-kept
- an-open-checkout-keeps-its-price: an-open-checkout-keeps-its-price journal: test gate refused tests-kept
- failed-deliveries-are-retried-on-their: failed-deliveries-are-retried-on-their journal: test gate refused tests-kept
- taking-over-the-guest-cart-is-a-step-of-its-own: taking-over-the-guest-cart-is-a-step-of-its-own journal: build gate refused tests-kept
- the-account-findings-are-closed: the-account-findings-are-closed journal: build gate refused tests-kept
- the-backoffice-findings-are-closed: the-backoffice-findings-are-closed journal: build gate refused tests-kept
- the-cart-code-findings-are-closed: the-cart-code-findings-are-closed journal: build gate refused tests-kept
- the-cart-tests-findings-are-closed: the-cart-tests-findings-are-closed journal: build gate refused tests-kept
- the-inventory-findings-are-closed: the-inventory-findings-are-closed journal: build gate refused tests-kept
- the-order-changed-since-the-review-has-the-shared-name: the-order-changed-since-the-review-has-the-shared-name journal: build gate refused tests-kept

## Options
- a: factory — the factory itself got this wrong — an issue text for its maintainers (`factory.sh lessons --issue lesson-refusal-tests-kept`, sent by a person)
- b: profile-check — a check the profile names, run at the gate (written by `/factory-setup`)
- c: instructions — a line of the project's agent instructions (written by `/dca-describe`)
- d: not-now — asked again once the evidence has grown
- e: never — the same candidate does not come back

## Recommendation
a — factory: the gate refuses the same check story after story; either the stages are not told what it holds, or the check refuses wrongly — the factory's maintainers look at both.
