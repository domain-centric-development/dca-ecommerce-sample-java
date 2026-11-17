---
story: an-early-step-says-what-is-still-open
epic: guest-checkout
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (3 rows, 0 without a fitting value)
---

# Findings — an-early-step-says-what-is-still-open

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/BuyerInfoPageController.java:108 | minor | introduced |  |  | The GET's "denied: forward the notice, granted: show it" block is repeated in Delivery, Payment and Review GETs | Let `CheckoutRoutes.redirectFor(StepAccess, Optional<StepNotice>)` absorb the `map/orElseGet` and call it once per GET | done (5a688bb) | code | duplication | extraneous |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/BuyerInfoPageController.java:185 | minor | introduced |  |  | `messageFor` spells the sentence `StepNotice.EARLIER_STEPS_FIRST` owns twice more | Return `StepNotice.EARLIER_STEPS_FIRST.sentence()` from one combined case | done (5a688bb) | code | duplication | extraneous |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/DeliveryPageController.java:204 | minor | introduced |  |  | Comment copied into three catch blocks (also Payment, Confirmation) whose "it" has no antecedent | Shorten to "no flash: the notice travels in the URL (see StepNotice)" or drop it | done (5a688bb) | code | comment | incorrect |
