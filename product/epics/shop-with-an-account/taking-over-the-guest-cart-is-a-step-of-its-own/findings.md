---
story: taking-over-the-guest-cart-is-a-step-of-its-own
epic: shop-with-an-account
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (5 rows, 0 without a fitting value)
---

# Findings — taking-over-the-guest-cart-is-a-step-of-its-own

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/cartrecovery/CartMergePageController.java:125 | minor | introduced |  |  | "Continue" merges both carts without asking if the account's cart filled up after the page was shown. | The POST runs `RecoverCart` without re-checking that the account's cart is still empty; `RecoverCartUseCase` adds the guest lines to a non-empty cart, bypassing the merge choice. | open | code | domain-invariant | missing |
| 2 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/cartrecovery/CartMergePageController.java:89 | minor | introduced |  |  | The page after the login works out "take over" from a missing value instead of being told. | `GetCartMergeOptionsResult` encodes three outcomes as `mergeRequired` plus a nullable `anonymousCart`; the controller decodes them, and the guest summary (an `ArticleDataPort` call) is built only to be checked for null. | open | code | interface | incorrect |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/cartrecovery/getcartmergeoptions/GetCartMergeOptionsUseCase.java:68 | minor | introduced |  |  | A filter on the account's cart ignores the cart it filters. | `accountCart.filter(cart -> accountHasItems)` ignores its parameter. | open | code | algorithm | incorrect |
| 4 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/cartrecovery/getcartmergeoptions/GetCartMergeOptionsResult.java:30 | minor | introduced |  |  | One result type calls the same cart both "guest" and "anonymous". | `onlyGuestCartHoldsItems(CartSummary anonymousCart)` beside the `anonymousCart` component. | open | code | domain-term | incorrect |
| 5 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/CartTakeOverPage.java:39 | minor | introduced |  |  | The take-over page object has a method nobody calls. | `continueButton()` is unused; `continueToCart()` clicks via the constant. | done | test | dead-code | extraneous |
