---
story: login-leads-only-to-shop-pages
epic: account
---

# Findings — login-leads-only-to-shop-pages

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/LoginPageController.java:156 | minor | The rule for what counts as a page of the shop is now written out in two contexts and can drift apart. | `isShopPage` copies the cart's `CartMergePageController.java:212` check; the copies already differ in their nullability signature. | When a third copy is needed, give the check one home in the shared web edge and call it from each adapter. | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/LoginPageController.java:146 | minor | The new helpers do not say that the return page may be missing. | `addReturnPage` and `isShopPage` take `returnUrl` from `@RequestParam(required = false)` and check it for null, but are not marked `@Nullable` as the cart's twin is. | Annotate both parameters `@Nullable` (JSpecify). | open |
