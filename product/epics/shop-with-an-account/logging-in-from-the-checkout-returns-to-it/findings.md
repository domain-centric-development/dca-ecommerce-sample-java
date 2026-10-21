---
story: logging-in-from-the-checkout-returns-to-it
epic: shop-with-an-account
---

# Findings — logging-in-from-the-checkout-returns-to-it

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/application/cartrecovery/recovercart/RecoverCartUseCase.java:63 | minor | The cart recovery gives the account's cart two names in one method. | `accountCart` (line 63) sits beside the existing `registeredCart`/`registeredCustomerId`, and line 75 binds `registeredCart = accountCart.get()`: one object, two names. | Use one pair of names throughout, e.g. `registeredCart` as the `Optional` and `anonCart`. | open |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/LoginPage.java:47 | minor | The login page object builds its return address twice. | `navigateTo(page, returnUrl)` (line 47) and the private constructor (line 54) each build `BASE_URL + URL_PATTERN + "?returnUrl=" + returnUrl`. | Extract `private static String urlReturningTo(String returnUrl)` and use it in both places. | open |
