---
story: paying-again-requests-no-second-payment
epic: provider-payment
---

# Findings — paying-again-requests-no-second-payment

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSession.java:324 | minor | Forgetting a cancelled payment still changes a checkout that has already closed. | `releasePayment` is the only mutator without `ensureModifiable()`. It runs in a later transaction (`SubmitPaymentUseCase.java:467` in the diff), and a concurrent confirmation or expiry can happen before then. | Call `ensureModifiable()` first, and add the unit test "a session no longer open releases no payment". | open |
| 2 | plan | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSession.java:324 | minor | The rule that a cancelled payment no longer counts is in the code but not in the plan. | build.md names "a released payment covers nothing" as an unplanned invariant. `aReleasedPaymentCoversNothing` tests it, but the plan's `## Invariants` does not list it. | Add it to the checkout spec's invariants. | open |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/submitpayment/SubmitPaymentUseCase.java:64 | minor | The payment step's main method has grown too long to follow at a glance. | `execute` mixes the outcome checks of the new request with the higher-level calls around them: keep, cancel, request, change the session. | Extract `requestPayment(...)` so that it returns the new `PaymentSelection`. | open |
