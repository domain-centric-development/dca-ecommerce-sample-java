---
story: an-open-checkout-keeps-its-price
epic: prices
---

# Findings — an-open-checkout-keeps-its-price

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/CheckoutSessionAuthorizePaymentTest.java:69 | minor | A test's name still says the payment is for the session's total, but the payment records whatever amount was authorized. | `theAuthorizedPaymentIsForTheSessionsTotal` contradicts `theAuthorizedPaymentIsForTheAmountAuthorized` (:80). | Rename it after its display name, e.g. `theAuthorizedPaymentCarriesItsReferenceProviderAndAmount`. | open |
| 2 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/OrderAtTodaysPriceAfterReopeningTheReviewIntegrationTest.java:67 | minor | A test class is named for the price scenario but also holds the other-tab scenario. | The class name covers only criterion 4. It also holds `placeOrderAfterAChangeInAnotherTabIsRefused` (:131), which has the same name as the adapter test in `OrderChangedSinceTheReviewIntegrationTest.java:64`. | Rename the class after its display name, e.g. `PlaceOrderAgainstTheShownReviewIntegrationTest`. | open |
