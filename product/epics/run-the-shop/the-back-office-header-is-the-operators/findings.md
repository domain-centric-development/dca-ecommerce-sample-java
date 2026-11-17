---
story: the-back-office-header-is-the-operators
epic: run-the-shop
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (3 rows, 1 without a fitting value)
---

# Findings — the-back-office-header-is-the-operators

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/BackofficeHeaderIntegrationTest.java:44 | minor | introduced |  | The test's setup buries what it checks. | One ~45-line method mixes shopper registration, shop login check and operator login with the assertions. | Extract `registerAndLogInShopper` and `logInOperator`. | open | — | — | — |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/BackofficeHeader.java:53 | minor | introduced |  | A page-object method's name promises a button but gives its text. | `logoutButton()` returns a `String`. | Rename to `logoutButtonText()`. | open | test | naming | incorrect |
| 3 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/BackofficeHeaderIntegrationTest.java:69 | minor | introduced |  | The operator's credentials are bare words, unlike in the browser test. | Inline `"admin"`/`"admin"` where the e2e test names them. | Named constants for the demo operator. | open | test | naming | missing |
