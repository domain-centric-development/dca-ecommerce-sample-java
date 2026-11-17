---
story: paying-again-requests-no-second-payment
epic: provider-payment
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (3 rows, 1 without a fitting value)
---

# Findings — paying-again-requests-no-second-payment

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSession.java:324 | minor | introduced |  | Forgetting a cancelled payment still changes a checkout that has already closed. | `releasePayment` is the only mutator without `ensureModifiable()`. It runs in a later transaction (`SubmitPaymentUseCase.java:467` in the diff), and a concurrent confirmation or expiry can happen before then. | Call `ensureModifiable()` first, and add the unit test "a session no longer open releases no payment". | done | code | domain-invariant | missing |
| 2 | plan | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSession.java:324 | minor | introduced |  | The rule that a cancelled payment no longer counts is in the code but not in the plan. | build.md names "a released payment covers nothing" as an unplanned invariant. `aReleasedPaymentCoversNothing` tests it, but the plan's `## Invariants` does not list it. | Add it to the checkout spec's invariants. | done | plan | domain-invariant | missing |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/submitpayment/SubmitPaymentUseCase.java:64 | minor | introduced |  | The payment step's main method has grown too long to follow at a glance. | `execute` mixes the outcome checks of the new request with the higher-level calls around them: keep, cancel, request, change the session. | Extract `requestPayment(...)` so that it returns the new `PaymentSelection`. | done | — | — | — |
