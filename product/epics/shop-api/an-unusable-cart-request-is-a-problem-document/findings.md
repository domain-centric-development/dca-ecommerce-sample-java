---
story: an-unusable-cart-request-is-a-problem-document
epic: shop-api
---

# Findings — an-unusable-cart-request-is-a-problem-document

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ShoppingCartApiE2ETest.java:132 | minor | The cart API tests repeat the same setup and checks four times over. | The new test is the fourth copy of opening a cart (:38, :68, :103, :135), the problem media-type check (:85, :118, :152, :186) and the still-empty check (:94, :125, :161, :195). | Extract `openCart`, `assertProblemDocument` and `assertCartStillEmpty` in this file and use them in all four tests. | open |
