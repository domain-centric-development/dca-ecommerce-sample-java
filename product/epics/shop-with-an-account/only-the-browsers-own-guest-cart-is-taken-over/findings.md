---
story: only-the-browsers-own-guest-cart-is-taken-over
epic: shop-with-an-account
---

# Findings — only-the-browsers-own-guest-cart-is-taken-over

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/LoginPageController.java:115 | minor | Logging in as one account while logged in as another records the first account as the guest, so its cart can be pulled into the second. | The session names the earlier identity as `guestBeforeLogin` whatever its type; the port promises a guest. This was already possible before the story, through the URL. | Pass the guest only when the identity captured at :96 `isAnonymous()`, otherwise use the three-argument token. | open |
| 2 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/cartrecovery/mergecarts/MergeCartsUseCase.java:57 | minor | A merge with nothing to bring can still create an empty account cart, though the class says no cart changes. | The get-or-create at :57-63 saves a new cart before the guard at :68. | Return before the get-or-create, or narrow the doc at :25 to "moves no line, deletes no cart". | open |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/cartrecovery/mergecarts/MergeCartsUseCase.java:89 | minor | The three merge helpers still handle a missing guest cart that can no longer occur. | After the guard at :68, the `isPresent()` checks at :89, :118 and :132 are always true. | Unwrap once and pass `ShoppingCart`; delete the dead branches. | open |
| 4 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/cartrecovery/CartMergePageController.java:336 | minor | The take-over and the merge use different signals for "nothing moved". | The take-over checks the nullable `cartId`, while the merge checks `anonymousCartDeleted`. | Add a named query to both results, e.g. `nothingMoved()`. | open |
| 5 | test | src/test/java/dev/domaincentric/sample/ecommerce/cart/application/cartrecovery/mergecarts/MergeCartsUseCaseTest.java:869 | minor | A guest cart that exists but is empty is never tested. | The heading says "without a line", but only the absent cart is tested; the second half of the guard at `MergeCartsUseCase.java:68` has no test. | Add the empty-cart case under that heading. | open |
