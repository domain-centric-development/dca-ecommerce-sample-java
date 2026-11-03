---
story: the-checkout-code-findings-are-closed
epic: provider-payment
---

# Findings — the-checkout-code-findings-are-closed

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/NothingToPayException.java:7` | minor | The "nothing to pay" refusal still explains itself with a provider request the payment step no longer makes. | `domain-term:` the Javadoc says "a payment would be arranged" and "a payment intent … would ask a provider to charge nothing". The rule now lives in `submitPayment`, which only records the choice and asks no provider anything (glossary, `submitPayment` entry). | Reword the Javadoc to the rule as it stands: a way to pay is chosen only for a session that has something to pay. | open |
| 2 | hexagonal | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/checkoutcompletion/ReviewPageController.java:109` | minor | The review page borrows the name of its form's marker from the Place Order controller. | `ConfirmationPageController.REVIEW_PAGE` was made package-visible so a sibling controller can read it. The value is a contract between the form and the confirm endpoint, and neither controller owns it alone (hexagonal and clean-code nit, one defect). | Move the constant into `CheckoutStepPage` (or `CheckoutRoutes`) and have both controllers read it from there. | open |
| 3 | clean-code | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/CheckoutStepNotice.java:19` | minor | The checkout's step messages are all refusals now, but the type is still called a "notice". | With `Kind` gone, every constant is shown as the page's `error` (`PAGE_ATTRIBUTE`, `:66`). "Notice" was the name of the removed informational kind. | Rename the type to something like `CheckoutStepRefusal`. The flash key can stay. | open |
| 4 | clean-code | `src/test/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSessionPaymentPreconditionsTest.java:50` | minor | A test helper looks adjustable, but it would build an inconsistent checkout with any other values. | `sessionAtThePaymentStep(unitPrice, shipping)` has one caller, which passes zeros. It passes `unitPrice` as the subtotal, which is only right for a single line of quantity 1. | Make it `sessionWithNothingToPayAtThePaymentStep()` with the zero amounts inside. | open |
| 5 | plan | `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ReviewPage.java:82` | minor | The browser test's review page still offers to read a message box the page no longer has. | deferred by the plan: `ReviewPage.notice()` has no caller, and its `review-notice-message` slot left `review.pug` in this change. The plan's `## Open assumptions` leaves it to `the-checkout-tests-findings-are-closed`. | Remove `notice()` in `the-checkout-tests-findings-are-closed`. | open |
