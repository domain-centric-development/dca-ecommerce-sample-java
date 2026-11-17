---
story: a-place-order-with-the-review-open-asks-for-it
epic: guest-checkout
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (3 rows, 0 without a fitting value)
---

# Findings — a-place-order-with-the-review-open-asks-for-it

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/ConfirmationPageController.java:120 | minor | introduced |  | The helper's name promises the review, but it can also lead to an earlier step. | `redirectToTheReview` also redirects to the step still open when `StepAccess` for REVIEW is denied (line 126); the name covers only one of its two outcomes | rename it after what it decides, e.g. `askForTheReview` | done | code | naming | incorrect |
| 2 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/ConfirmationPageController.java:121 | minor | introduced |  | Which message a refused order shows is now decided in two places of the checkout's web side. | the controller picks `REVIEW_FIRST` / `EARLIER_STEPS_FIRST` and the granted/denied route itself, while `CheckoutStepPage.java:27-29` says a refusal is translated there once and `redirectFor` (`:100-106`) already has the same branch | move the branch into `CheckoutStepPage` as a static beside `submitting` and call it from the controller, or name the second site in `CheckoutStepPage`'s class comment | done | code | duplication | extraneous |
| 3 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/ConfirmationPageController.java:49 | minor | introduced |  | The word that marks the review's form is written twice, so renaming it on one side would refuse every order. | `REVIEW_PAGE = "review"` and the hidden field's `value="review"` at `src/main/resources/templates/checkout/review.pug:109` hold one request contract in two places, tied only by tests | hand the value to the template from the controller's model so one constant names it | done | code | duplication | extraneous |
