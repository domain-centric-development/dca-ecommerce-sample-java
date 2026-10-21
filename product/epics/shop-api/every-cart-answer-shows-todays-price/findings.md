---
story: every-cart-answer-shows-todays-price
epic: shop-api
---

# Findings — every-cart-answer-shows-todays-price

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/shopping/removeitemfromcart/RemoveItemFromCartUseCase.java:350 | minor | A saved add or remove can answer "bad request" when a product in the cart has lost its catalogue data. | Post-commit `EnrichedCartFactory.create` throws `IllegalArgumentException` (`EnrichedCartFactory.java:55`), mapped to 400 at `CartApiExceptionHandler.java:129`; same at `AddItemToCartUseCase.java:200`. | Let the post-commit enrichment tolerate a missing article, or raise a named failure whose status does not deny the committed change. | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/shopping/additemtocart/AddItemToCartUseCase.java:207 | minor | How a cart is priced at today's prices is now written out in several places. | `atTodaysPrices` is word for word in add and remove (`RemoveItemFromCartUseCase.java:358`); `GetCartByIdUseCase` holds the same steps inline. | Extract one application collaborator in `cart/application/shared` and call it from the use cases. | open |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/shopping/additemtocart/AddItemToCartResult.java:24 | minor | Which price a cart line shows depends on which of two same-named methods gets picked. | `CartItemSummary::from` is overloaded for `CartItem` (price at addition) and `EnrichedCartItem` (today's price), `CartItemSummary.java:22,31`. | Name the two factories after the price they use. | open |
