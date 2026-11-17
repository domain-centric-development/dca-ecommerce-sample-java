---
story: registration-says-the-account-was-created
epic: account
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (2 rows, 0 without a fitting value)
---

# Findings — registration-says-the-account-was-created

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/RegistrationNotice.java:122 | minor | introduced |  |  | `show(model)` reads the notice and writes the layout's `successMessage`, where the sibling `AccountPageNotice.in(model)` (AccountPageNotice.java:47) only reads | give the enum `in(model)` and `sentence()` and let `RegistrationNoticeControllerAdvice` put `successMessage` | done | code | interface | incorrect |
| 2 | clean-code | src/test/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/RegistrationNoticeTest.java:428 | minor | introduced |  |  | one test checks the absent and the unknown code on one model with one assertion, so a failure does not say which broke | split into one test per case | done | test | checking | incorrect |
