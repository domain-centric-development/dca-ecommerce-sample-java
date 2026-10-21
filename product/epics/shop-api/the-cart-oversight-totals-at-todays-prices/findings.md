---
story: the-cart-oversight-totals-at-todays-prices
epic: shop-api
---

# Findings — the-cart-oversight-totals-at-todays-prices

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/cartoversight/getallcarts/GetAllCartsUseCase.java:111 | minor | One cart holding a product the catalogue no longer knows now makes the whole staff list fail as a bad request. | `CompositeArticleDataAdapter.java:78` omits unknown products, `EnrichedCartFactory.java:55` throws `IllegalArgumentException`, the cart handler answers 400; the plan accepts this under Open assumptions, but the list's exposure is new. | A follow-up story decides what a missing article means for the list (skip the line, or a named failure mapped to a server status). | open |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShopUnderTest.java:177 | minor | The test helper says what a staff token is in two places. | `staffTokenIn` repeats `staffTokenFor`'s body (`:159`–`:165`), including the role set. | Let `staffTokenFor` delegate: `.map(started -> staffTokenIn(started, userId, email))`. | open |
