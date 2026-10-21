---
story: the-back-office-header-is-the-operators
epic: run-the-shop
---

# Findings — the-back-office-header-is-the-operators

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/BackofficeHeaderIntegrationTest.java:44 | minor | The test's setup buries what it checks. | One ~45-line method mixes shopper registration, shop login check and operator login with the assertions. | Extract `registerAndLogInShopper` and `logInOperator`. | open |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/BackofficeHeader.java:53 | minor | A page-object method's name promises a button but gives its text. | `logoutButton()` returns a `String`. | Rename to `logoutButtonText()`. | open |
| 3 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/BackofficeHeaderIntegrationTest.java:69 | minor | The operator's credentials are bare words, unlike in the browser test. | Inline `"admin"`/`"admin"` where the e2e test names them. | Named constants for the demo operator. | open |
