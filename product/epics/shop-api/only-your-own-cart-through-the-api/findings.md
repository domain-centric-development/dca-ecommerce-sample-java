---
story: only-your-own-cart-through-the-api
epic: shop-api
---

# Findings — only-your-own-cart-through-the-api

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/api/ApiAuthorizationIntegrationTest.java:101 | minor | reading-another-customers-cart-finds-nothing: the mapped test reads an empty cart, the scenario's Given is a cart holding "Domain-Driven Design" × 2; the 404 does not depend on the lines | arrange the Given with the scenario's line (add "Domain-Driven Design" × 2 before Grace's read) | done (a1e227d) |
