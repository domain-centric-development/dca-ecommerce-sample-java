---
story: a-malformed-product-id-is-a-bad-request
epic: shop-api
---

# Findings — a-malformed-product-id-is-a-bad-request

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShoppingCartApiE2ETest.java:100 | minor | Third copy of "open a cart, read its id" and "POST an item as JSON" (lines 36, 66, 100). | Extract `openCart(token)` and `addItem(token, cartId, json)` helpers, as the integration test already has. | open |
