---
story: only-your-own-cart-through-the-api
epic: shop-api
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (1 rows, 0 without a fitting value)
---

# Findings — only-your-own-cart-through-the-api

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/api/ApiAuthorizationIntegrationTest.java:101 | minor | introduced |  |  | reading-another-customers-cart-finds-nothing: the mapped test reads an empty cart, the scenario's Given is a cart holding "Domain-Driven Design" × 2; the 404 does not depend on the lines | arrange the Given with the scenario's line (add "Domain-Driven Design" × 2 before Grace's read) | done (a1e227d) | test | traceability | incorrect |
