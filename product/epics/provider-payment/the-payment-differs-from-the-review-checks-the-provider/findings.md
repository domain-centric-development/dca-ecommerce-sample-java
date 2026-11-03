---
story: the-payment-differs-from-the-review-checks-the-provider
epic: provider-payment
---

# Findings — the-payment-differs-from-the-review-checks-the-provider

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/PaymentDiffersFromReviewException.java:18 | minor | The failure does not say which of its two cases happened or what was compared. | domain-concept: one type for another provider and another amount keeps only `sessionId`; the compared values live in the message | typed fields or a named case, in a later story | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSession.java:359 | minor | The checkout keeps a test-only second way to record a payment that can never fail the provider check. | the `(String, Money)` overload has only test callers and repeats the guards; kept by the plan's `## Open assumptions` | move those tests to the one-argument form and drop the overload | open |
| 3 | ddd | product/spec/checkout/glossary.md:145 | minor | The glossary says an order needs the chosen provider's payment, but an order with no recorded payment is still confirmed. | domain-term: `confirm` skips the check when no amount is authorized (`CheckoutSession.java:457`), as before the story; the reworded entry now promises more | word it "when a payment was authorized", or let `confirm` refuse an unpaid session in its own story | open |
| 4 | clean-code | src/test/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/confirmcheckout/ConfirmCheckoutUseCaseTest.java:80 | minor | The test that the stock is given back would also pass if nothing had been reserved. | compares two lists that may both be empty | assert the reserved list is not empty first | open |
