---
story: remove-a-line-from-the-cart
epic: fill-a-cart
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (3 rows, 0 without a fitting value)
---

# Findings — remove-a-line-from-the-cart

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/web/shopping/CartPageController.java:183 | minor | introduced |  |  | the catch drops every `UseCaseException`/`DomainException`/`IllegalArgumentException` without a trace, so an unexpected refusal (e.g. `CartNotModifiableException`) reads as "the line was already gone"; the plan asks for this redirect, not for silence in the log | log the refusal by type at debug level in the catch, keeping the redirect without a notice | open | code | checking | missing |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/CartPage.java:174 | minor | introduced |  |  | `lineNames()` reads the same `cart-item`/`cart-item-name` elements as `lineItems()`, with `cart-item-name` a third literal in the file | reuse one reader and name the selector as a constant | open | test | duplication | extraneous |
| 3 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/cart/RemoveCartItemIntegrationTest.java:472 | minor | introduced |  |  | method `aRemoveNamingNoLineOfTheCartChangesNothing` does not match its scenario "A remove with a malformed line id changes nothing"; `removeFormOf` uses `orElse(null)` + null check where the file elsewhere uses `orElseThrow` | rename to `aRemoveWithAMalformedLineIdChangesNothing` and use `orElseThrow(AssertionError)` | open | test | traceability | incorrect |
