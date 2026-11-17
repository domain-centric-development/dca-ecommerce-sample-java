---
story: a-refused-api-login-is-a-problem-document
epic: shop-api
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (2 rows, 0 without a fitting value)
---

# Findings — a-refused-api-login-is-a-problem-document

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/LoginApiE2ETest.java:58 | minor | introduced |  | Both login tests set up the account with the same copied block. | The arrange block (fresh address, register with `Secret123`, assert 201) is copied verbatim from lines 29-39; the wrong-password test relies on that copy's password silently. | Extract `registeredAccount(password)` returning the address and call it from both tests. | done | test | duplication | extraneous |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/LoginApiE2ETest.java:18 | minor | introduced |  | The test class's description still says it only covers a successful login. | The class Javadoc names only the token handed out; the class now also covers the refused login. | Add "or a problem document when the login is refused" to the class Javadoc. | done | test | comment | missing |
