---
story: an-operator-changes-a-price
epic: prices
---

# Findings — an-operator-changes-a-price

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/pricing/application/changeprice/ChangePriceUseCase.java:39 | minor | The rule that a new price keeps its currency is enforced by the use case, so the price model itself would accept a change of currency. | The use case builds the new `Money` from the current currency; `ProductPrice.updatePrice` accepts any currency. The code follows the plan. | Give `ProductPrice` an operation that takes the new amount and keeps its own currency, and pass only the amount. | open |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/PriceChangeApiE2ETest.java:45 | minor | The browser test ignores the answer to the price change, so a refusal only shows up later as the wrong price on the page. | The `PUT` response is not kept or checked. | Assert `204` on the response, with its body in the message. | open |
| 3 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/PriceChangeApiE2ETest.java:85 | minor | The test mints a staff token with a fourth copy of the same code. | `staffToken(shop)` duplicates `ProductCreationApiE2ETest`, `PriceRoundingApiE2ETest` and `DuplicateSkuApiE2ETest`. | Extract it into `ShopUnderTest` beside `staffTokenFor` and call that. | open |
| 4 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/pricing/UpdatePriceIntegrationTest.java:30 | minor | The port's test still says "update" while everything else says "price change". | The class name drifts from the `ChangePrice*` port it exercises. | Rename it to `ChangePriceIntegrationTest`. | open |
