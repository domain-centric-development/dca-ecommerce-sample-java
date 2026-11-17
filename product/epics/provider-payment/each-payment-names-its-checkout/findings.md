---
story: each-payment-names-its-checkout
epic: provider-payment
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (2 rows, 0 without a fitting value)
---

# Findings — each-payment-names-its-checkout

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ddd | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ConfirmationPage.java:50 | minor | introduced |  |  | The test helper calls the checkout reference an "order reference", which is a different thing in the model. | `orderReference()` returns the checkout session's id, but the story keeps that apart from the model's order reference. | open | test | domain-term | incorrect |
| 2 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/CheckoutReferenceIntegrationTest.java:168 | minor | introduced |  |  | The steps that bring a checkout to the payment page are now copied into a fourth test class. | `checkoutAtThePaymentStep` repeats the journey in `CheckoutFlowIntegrationTest`, `PlaceTheOrderIntegrationTest` and `ArticleDataFlowIntegrationTest`. | open | test | duplication | extraneous |
