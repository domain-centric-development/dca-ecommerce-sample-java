---
story: the-cart-oversight-has-its-own-place-in-the-code
epic: shop-api
---

# Findings — the-cart-oversight-has-its-own-place-in-the-code

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/api/ShoppingCartResource.java:36 | minor | The note on where the staff list went reads awkwardly. | "it is the cart oversight's, {@link CartOversightResource}" — a possessive with no noun after it. | Reword to "…by design and lives in {@link CartOversightResource}, which demands the staff role." | open |
