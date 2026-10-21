---
story: the-cart-holds-no-more-than-is-in-stock
epic: fill-a-cart
---

# Findings — the-cart-holds-no-more-than-is-in-stock

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/shopping/additemtocart/AddItemToCartUseCase.java:191 | minor | The use case does the cart's arithmetic instead of asking the cart. | It reads `quantityOf` and adds the request itself. The rule now concerns the line's state, which is more than "about the request" (`marker/application/usecaseexception.md`). | Hand the stock snapshot to the model (`cart.wouldExceed(productId, quantity, availableStock)`), or record in an ADR why the rule stays a use-case failure. | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/CartPageNotice.java:143 | minor | A way of sending the stock refusal stays that now shows the customer nothing. | The one-number `redirectTo(path, count, attributes)` has no production caller left. Its parameter order differs from the varargs overload. `CartPageNoticeTest.java:86` pins it with `NOT_ENOUGH_STOCK`, which now needs three numbers. | Delete the overload and rewrite the test on the varargs form with a one-number notice. | open |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/shopping/CartPageController.java:199 | minor | Swapping "held" and "in stock" in the refusal would pass every test. | The three numbers are positional against the sentence's `%d`. Every test crossing the seam uses held = in stock (8/8, `1, 8, 8`), and the integration test asserts only `requested()`. | Use distinct numbers in `CartPageNoticeTest`, and assert `held()` 5 / `inStock()` 8 in `AddItemToCartIntegrationTest`. | open |
