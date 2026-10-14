---
story: the-cart-stays-with-the-person
epic: shop-with-an-account
---

# Findings — the-cart-stays-with-the-person

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/account/CartAcrossTheSessionIntegrationTest.java:163 | minor | The `When` says the session expires "after its 7 days"; the test only asserts that the session's max-age is shorter than the identity's, so a 1-day session would also pass | Also assert `getMaxAge()` of `shop-session` equals 7 days (604800 s) | done (00d4654) |
