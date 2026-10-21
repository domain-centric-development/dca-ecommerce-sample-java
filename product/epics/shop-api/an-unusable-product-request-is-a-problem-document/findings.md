---
story: an-unusable-product-request-is-a-problem-document
epic: shop-api
---

# Findings — an-unusable-product-request-is-a-problem-document

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShopUnderTest.java:184 | minor | Two test helpers for a staff token have almost the same name but do different things. | `staffTokenOf(started)` registers a new account on a shop of its own and re-mints its token, while `staffTokenFor(userId, email)` (`:163`) mints a token on the suite's shared shop for an existing account; the names differ by one preposition. | Rename `staffTokenOf` to say it registers a new account, e.g. `registeredStaffTokenOn(started)`. | open |
