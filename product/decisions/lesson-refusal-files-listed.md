---
id: lesson-refusal-files-listed
kind: lesson
lesson: refusal
key: files-listed
stories: 21
asked: 2026-11-18T11:51:29Z
---

# The same gate refusal in 21 stories — files-listed

## Question
Counted, never guessed: this refusal repeats in 21 stories. Where does its correction belong? The factory writes no rule and no knowledge itself — the skill that owns the place does, with the evidence below. No story waits for this answer.

## Evidence
- a-malformed-product-id-is-a-bad-request: a-malformed-product-id-is-a-bad-request journal: build gate refused files-listed
- a-place-order-with-the-review-open-asks-for-it: a-place-order-with-the-review-open-asks-for-it journal: build gate refused files-listed
- an-early-step-says-what-is-still-open: an-early-step-says-what-is-still-open journal: test gate refused files-listed
- an-open-checkout-keeps-its-price: an-open-checkout-keeps-its-price journal: test gate refused files-listed
- availability-shown: availability-shown journal: test gate refused files-listed
- creating-a-product-holds-its-price: creating-a-product-holds-its-price journal: test gate refused files-listed
- each-listener-counts-its-own-attempts: each-listener-counts-its-own-attempts journal: test gate refused files-listed
- every-cart-answer-shows-todays-price: every-cart-answer-shows-todays-price journal: build gate refused files-listed
- moving-between-pages-is-animated: moving-between-pages-is-animated journal: test gate refused files-listed
- one-account-per-address-and-per-browser: one-account-per-address-and-per-browser journal: test gate refused files-listed
- paying-again-requests-no-second-payment: paying-again-requests-no-second-payment journal: test gate refused files-listed
- reading-the-active-cart-opens-none: reading-the-active-cart-opens-none journal: test gate refused files-listed
- register-an-account: register-an-account journal: test gate refused files-listed
- registration-refuses-an-unusable-form: registration-refuses-an-unusable-form journal: test gate refused files-listed
- registration-says-the-account-was-created: registration-says-the-account-was-created journal: build gate refused files-listed
- remove-a-line-from-the-cart: remove-a-line-from-the-cart journal: build gate refused files-listed
- taking-over-the-guest-cart-is-a-step-of-its-own: taking-over-the-guest-cart-is-a-step-of-its-own journal: build gate refused files-listed
- the-buyer-gets-an-order-confirmation: the-buyer-gets-an-order-confirmation journal: test gate refused files-listed
- the-cart-oversight-is-a-feature-of-its-own: the-cart-oversight-is-a-feature-of-its-own journal: test gate refused files-listed
- the-payment-is-authorized-at-place-order: the-payment-is-authorized-at-place-order journal: build gate refused files-listed
- the-profile-refuses-an-unusable-change: the-profile-refuses-an-unusable-change journal: test gate refused files-listed

## Options
- a: factory — the factory itself got this wrong — an issue text for its maintainers (`factory.sh lessons --issue lesson-refusal-files-listed`, sent by a person)
- b: profile-check — a check the profile names, run at the gate (written by `/factory-setup`)
- c: instructions — a line of the project's agent instructions (written by `/dca-describe`)
- d: not-now — asked again once the evidence has grown
- e: never — the same candidate does not come back

## Recommendation
a — factory: the gate refuses the same check story after story; either the stages are not told what it holds, or the check refuses wrongly — the factory's maintainers look at both.
