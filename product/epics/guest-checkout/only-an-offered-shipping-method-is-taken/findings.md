---
story: only-an-offered-shipping-method-is-taken
epic: guest-checkout
---

# Findings — only-an-offered-shipping-method-is-taken

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ChooseTheDeliveryAddressE2ETest.java:68 | minor | The new browser test looks up the delivery error itself instead of asking the delivery page for it. | The assertion spells the raw `[data-test='delivery-error-message']` locator; `DeliveryPage` has no accessor for it, unlike `CartPage`, `RegisterPage` and others. | Add an `errorMessage()` locator to `DeliveryPage` and assert through it. | open |
| 2 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/ShippingOptionNotFoundException.java:16 | minor | The new refusal carries the shipping method as plain text, where the payment one carries a typed id. | `String shippingOptionId`, where `PaymentProviderNotFoundException` carries `PaymentProviderId`; inherited from `ShippingOption.id()` and `ShippingOptionRegistry.findById(String)`. | When a `ShippingOptionId` value object is introduced, carry it into this failure in the same change. | open |
