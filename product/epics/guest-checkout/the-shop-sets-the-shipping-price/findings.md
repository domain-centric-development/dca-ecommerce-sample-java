---
story: the-shop-sets-the-shipping-price
epic: guest-checkout
---

# Findings — the-shop-sets-the-shipping-price

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/submitdelivery/SubmitDeliveryUseCase.java:59 | minor | A shipping method the shop does not offer is refused like a malformed field, not as its own case. | The registry miss raises `IllegalArgumentException`, where the sibling registry's miss is the named `PaymentProviderNotFoundException`; the plan's `## Open assumptions` defers it | In `only-an-offered-shipping-method-is-taken`: a named use-case failure carrying the id, mapped in `CheckoutStepPage` | open |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/PaymentPage.java:70 | minor | The payment page's test helper finds the amounts by position, so a markup change breaks it. | `summaryAmount` picks the second `span` of a summary line instead of a `data-test` selector | Add `data-test` attributes to the summary amounts in both shops' markup and select on them | open |
