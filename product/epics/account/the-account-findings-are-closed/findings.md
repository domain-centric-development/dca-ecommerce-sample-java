---
story: the-account-findings-are-closed
epic: account
---

# Findings — the-account-findings-are-closed

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/test/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/RegistrationNoticeTest.java:20 | minor | The tests for how the page shows the notice sit beside the notice's own tests and repeat its absent and unknown cases. | The advice's three `successMessage` tests live in the enum's test class (as the plan put them), next to `in(model)` tests for the same two cases. | Move the advice field and its three tests into `RegistrationNoticeControllerAdviceTest`. | open |
| 2 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/RegistrationE2ETest.java:111 | minor | The browser test still spells "Todays", because its name has to be its scenario's, and the scenario's key spells it so. | Row a-date-of-birth-lies-in-the-past#5 is settled for the integration test only; the browser title is bound to the spec key `todays-date-of-birth-is-refused` (`SharedScenariosTest.java:118`), so the spelling lives in the spec. | Give the scenario a `Title:` "Today's date of birth is refused" in a story, in both shops, and rename the test with it. | open |
| 3 | plan | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/LoginApiE2ETest.java:91 | minor | The login tests share their account setup in a slightly different shape than planned. | build.md deviation: `freshAddress()` + `register(email, password)` instead of `registeredAccount(password)`; the 201 check stays per test. The copy and the silent password (row #1) are gone. | None; recorded as the plan's deviation. | open |
| 4 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/ProfilePageController.java:54 | minor | deferred by the plan: the profile page still tells the person a date of birth must not lie in the future, though today is refused too. | deferred by the plan: the shown sentence is behaviour, outside a task. | A story of its own rewords it to "before today". | open |
