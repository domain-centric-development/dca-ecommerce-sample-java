---
story: registration-says-the-account-was-created
epic: account
---

# Findings — registration-says-the-account-was-created

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/RegistrationNotice.java:122 | minor | `show(model)` reads the notice and writes the layout's `successMessage`, where the sibling `AccountPageNotice.in(model)` (AccountPageNotice.java:47) only reads | give the enum `in(model)` and `sentence()` and let `RegistrationNoticeControllerAdvice` put `successMessage` | open |
| 2 | clean-code | src/test/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/RegistrationNoticeTest.java:428 | minor | one test checks the absent and the unknown code on one model with one assertion, so a failure does not say which broke | split into one test per case | open |
