---
story: the-confirmation-says-only-what-happened
epic: guest-checkout
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (2 rows, 0 without a fitting value)
---

# Findings — the-confirmation-says-only-what-happened

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/PlaceTheOrderIntegrationTest.java:122 | minor | introduced |  | The new test reads the total line by hand instead of using the helper the class already has for it. | The "label value" join over `.order-summary__total > span` repeats what `totalsOf(...)` (line 151) does for the same container; the plan named `totalsOf(...)` for reuse. | `assertThat(totalsOf(confirmation.selectFirst(".confirmation__details"))).contains("Order Total 59.98 EUR");` and drop the `details` local. | open | test | duplication | extraneous |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/PlaceTheOrderE2ETest.java:36 | minor | introduced |  | The browser test bypasses the confirmation page object and spells out the page's selectors itself. | It calls `ReviewPage.pressPlaceOrder()` (documented as not assuming where the guest lands), then waits on a raw `confirmation-title` locator and reads a raw `.confirmation` locator, while `ReviewPage.placeOrder()` returns a `ConfirmationPage` that owns the page and its load wait. | Use `aGuestOnTheReviewStep().placeOrder()`, give `ConfirmationPage` an intent-named locator over `.confirmation`, and assert the two absences on it. | open | test | interface | incorrect |
