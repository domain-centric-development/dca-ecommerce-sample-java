---
story: each-payment-names-its-checkout
epic: provider-payment
---

# Findings — each-payment-names-its-checkout

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | ddd | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ConfirmationPage.java:50 | minor | The test helper calls the checkout reference an "order reference", which is a different thing in the model. | `orderReference()` returns the checkout session's id, but the story keeps that apart from the model's order reference. | open |
| 2 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/CheckoutReferenceIntegrationTest.java:168 | minor | The steps that bring a checkout to the payment page are now copied into a fourth test class. | `checkoutAtThePaymentStep` repeats the journey in `CheckoutFlowIntegrationTest`, `PlaceTheOrderIntegrationTest` and `ArticleDataFlowIntegrationTest`. | open |
