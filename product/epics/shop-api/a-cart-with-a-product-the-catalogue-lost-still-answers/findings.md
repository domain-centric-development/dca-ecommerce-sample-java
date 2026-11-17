---
story: a-cart-with-a-product-the-catalogue-lost-still-answers
epic: shop-api
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (8 rows, 1 without a fitting value)
---

# Findings — a-cart-with-a-product-the-catalogue-lost-still-answers

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/shared/CartItemSummary.java:23 | minor | introduced |  | A line's state is a missing price plus a flag, so "unpriced but available" can be written. | domain-concept: `@Nullable unitPrice` + `available` admit (null, true); `from(CartItem)` states `true` unchecked | Name the line's state in the result; flatten it in the converter | open | code | domain-concept | incorrect |
| 2 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/domain/model/EnrichedCartItem.java:113 | minor | introduced |  | Some questions about a line whose product is gone still crash instead of answering. | domain-concept: `currentLineTotal`, `priceIncreased`, `priceDifference`, `hasSufficientStock` throw `NoSuchElementException` | Make them total: `Optional` line total, `false` for stock and rise | open | code | domain-concept | incorrect |
| 3 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/domain/model/EnrichedCart.java:98 | minor | introduced |  | The cart decides which line counts toward the total instead of asking the line. | domain-concept: subtotal filters on `item.article()` | Let the line answer it (`Optional` line total, `flatMap`) | open | code | domain-concept | incorrect |
| 4 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/domain/model/EnrichedCart.java:108 | minor | introduced |  | The price-change sum counts a gone product's whole price as a change. | domain-concept: original subtotal counts lost lines, current does not; `totalPriceDifference` skews (cart page only) | Original subtotal over the priced lines, with a unit test | open | code | domain-concept | incorrect |
| 5 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/shopping/CartPageViewModel.java:82 | minor | introduced |  | deferred by the plan: the cart page and mini basket still break for a cart holding a gone product. | the web adapters call the throwing accessors, no translation | A page story for such a line | open | code | checking | missing |
| 6 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/api/ShoppingCartDtoConverter.java:101 | minor | introduced |  | A comment says every line carries today's price, no longer true. | stale line comment | Delete it | open | code | comment | extraneous |
| 7 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/domain/model/EnrichedCartItem.java:37 | minor | introduced |  | A shortcut constructor passes "nothing" into an optional to trigger a check. | null into an `Optional` slot | Throw there; pass `Optional.of` | open | code | interface | incorrect |
| 8 | plan | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/shopping/MiniBasketControllerAdvice.java:127 | minor | introduced |  | Two planned page changes were left out on purpose, rightly. | planned compile follows unneeded: `currentArticle()` kept (build.md Deviations) | none | open | — | — | — |
