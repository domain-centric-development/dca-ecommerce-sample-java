---
story: created-product-answers-with-its-rounded-price
epic: shop-api
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (1 rows, 0 without a fitting value)
---

# Findings — created-product-answers-with-its-rounded-price

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProductCreationApiE2ETest.java:71 | minor | introduced |  |  | the `POST /api/products` request block repeats lines 37-51 line for line, apart from the body | extract `createProduct(shop, staffToken, body)` and call it from both tests | done (60772ca) | test | duplication | extraneous |
