---
story: the-payment-differs-from-the-review-checks-the-provider
epic: provider-payment
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (4 rows, 0 without a fitting value)
---

# Findings — the-payment-differs-from-the-review-checks-the-provider

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/PaymentDiffersFromReviewException.java:18 | minor | introduced |  | The failure does not say which of its two cases happened or what was compared. | domain-concept: one type for another provider and another amount keeps only `sessionId`; the compared values live in the message | typed fields or a named case, in a later story | open | code | domain-concept | missing |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSession.java:359 | minor | introduced |  | The checkout keeps a test-only second way to record a payment that can never fail the provider check. | the `(String, Money)` overload has only test callers and repeats the guards; kept by the plan's `## Open assumptions` | move those tests to the one-argument form and drop the overload | open | code | dead-code | extraneous |
| 3 | ddd | product/spec/checkout/glossary.md:145 | minor | introduced |  | The glossary says an order needs the chosen provider's payment, but an order with no recorded payment is still confirmed. | domain-term: `confirm` skips the check when no amount is authorized (`CheckoutSession.java:457`), as before the story; the reworded entry now promises more | word it "when a payment was authorized", or let `confirm` refuse an unpaid session in its own story | open | glossary | domain-term | incorrect |
| 4 | clean-code | src/test/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/confirmcheckout/ConfirmCheckoutUseCaseTest.java:80 | minor | introduced |  | The test that the stock is given back would also pass if nothing had been reserved. | compares two lists that may both be empty | assert the reserved list is not empty first | open | test | checking | missing |
