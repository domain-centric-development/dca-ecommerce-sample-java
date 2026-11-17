---
story: an-unknown-product-in-the-body-is-unprocessable
epic: shop-api
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (2 rows, 0 without a fitting value)
---

# Findings — an-unknown-product-in-the-body-is-unprocessable

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/api/CartApiExceptionHandler.java:46 | minor | introduced |  | The comment promises the cart exists, but an unknown product sent to a missing cart gets the same answer. | The Javadoc says "the cart the address names is there". The use case looks up the article before it loads the cart (`AddItemToCartUseCase.java:66-81`), so a missing cart with an unknown product also reaches this handler. | Reword it to say only what holds: the product is a reference in the body, not the addressed resource, so the answer is 422. | open | code | comment | incorrect |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShoppingCartApiE2ETest.java:130 | minor | introduced |  | The API refusal tests copy the same open-cart, add, check-refusal and check-empty steps three times. | The open-cart, post-item, problem-content-type and empty-cart sequence now appears three times (lines 64, 99 and 130). | Extract `openCart`, `addItem`, `assertProblemJson` and `assertCartIsEmpty` beside the existing helpers. | open | test | duplication | extraneous |
