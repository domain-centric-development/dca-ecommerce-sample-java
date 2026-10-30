---
story: the-mail-provider-is-on-the-context-map
epic: guest-checkout
---

# Findings — the-mail-provider-is-on-the-context-map

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | `src/main/java/dev/domaincentric/sample/ecommerce/checkout/package-info.java:9` | minor | The checkout context's one-line description does not mention the order confirmation it sends. | domain-term: the `@BoundedContext` description (and the Javadoc at line 4) reads "Checkout process, order placement, and payment orchestration", while the designed map at `product/domain.md:25` names "order confirmation" and the context now declares a Mail Provider upstream for it; the generated map's Bounded Contexts row (`docs/architecture/context-map.md:23`) carries the narrower sentence. Pre-dates this story; the clean-code nit names the same lines. | Add "order confirmation" to the description and the Javadoc in the designed map's words and let `ContextMapDocumentationTest` regenerate the map — a code change, so a later story's. | open |
