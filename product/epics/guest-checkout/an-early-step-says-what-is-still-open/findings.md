---
story: an-early-step-says-what-is-still-open
epic: guest-checkout
---

# Findings — an-early-step-says-what-is-still-open

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/BuyerInfoPageController.java:108 | minor | The GET's "denied: forward the notice, granted: show it" block is repeated in Delivery, Payment and Review GETs | Let `CheckoutRoutes.redirectFor(StepAccess, Optional<StepNotice>)` absorb the `map/orElseGet` and call it once per GET | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/BuyerInfoPageController.java:185 | minor | `messageFor` spells the sentence `StepNotice.EARLIER_STEPS_FIRST` owns twice more | Return `StepNotice.EARLIER_STEPS_FIRST.sentence()` from one combined case | open |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/DeliveryPageController.java:204 | minor | Comment copied into three catch blocks (also Payment, Confirmation) whose "it" has no antecedent | Shorten to "no flash: the notice travels in the URL (see StepNotice)" or drop it | open |
