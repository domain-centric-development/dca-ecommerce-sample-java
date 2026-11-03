---
story: the-product-findings-are-closed
epic: shop-api
---

# Findings — the-product-findings-are-closed

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShopUnderTest.java:216 | minor | Two staff-token test helpers for the same started shop use different little words in their names for no reason. | `registeredStaffTokenOn(started)` sits beside `staffTokenIn(started, userId, email)` (`:200`); both take the started shop and return a staff token, one says *On*, the other *In* — One Word per Concept. | Rename to `registeredStaffTokenIn` so the preposition matches `staffTokenIn`. | open |
| 2 | plan | docs/integrations/pug4j-integration.md:111 | minor | The template guide shows an old version of the product page that catches an error and sets no not-found status. | deferred by the plan: the example's `showProductDetail(id, model)` with a try/catch on `IllegalArgumentException` predates the result-based handler; the code now returns `ModelAndView` with `HttpStatus.NOT_FOUND` (`ProductPageController.java:89-101`). | Replace the example with the current handler shape (`ModelAndView`, `result.found()`, `error/404` with 404). | open |
