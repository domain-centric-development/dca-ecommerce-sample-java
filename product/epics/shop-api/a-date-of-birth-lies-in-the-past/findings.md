---
story: a-date-of-birth-lies-in-the-past
epic: shop-api
---

# Findings — a-date-of-birth-lies-in-the-past

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/domain/model/Owner.java:35 | minor | The owner's documentation still says only future dates are refused, though today is refused now. | `@param`/`@throws` of `of` and `withDateOfBirth` (:35, :37-38, :48, :50) say "not in the future"; the rule now refuses today. Plan left the file out of `## Files`, so no plan deviation. | Reword to "before today" / "missing or does not lie before today". | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/domain/model/Account.java:298 | minor | The account's documentation of a date-of-birth change names only future dates. | `@throws` says "if the date lies in the future"; it also throws for today and a missing date. | "if the date is missing or does not lie before today". | open |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/application/changeprofile/ChangeProfileResult.java:41 | minor | The profile result describes the old date rule. | `DATE_OF_BIRTH_INVALID` documented as "missing or lies in the future". | "missing or does not lie before today". | open |
| 4 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/api/AccountApiExceptionHandler.java:105 | minor | The new API refusal reads as if it were only about dates of birth. | Javadoc names one example as the scope; the handler answers every field refusal in the package, login included. | Describe it as any request body that fails its own validation, the date as an example. | open |
| 5 | clean-code | src/test-integration/java/dev/domaincentric/sample/ecommerce/account/RegistrationApiIntegrationTest.java:76 | minor | Two new test names misspell "today's". | Display names "Todays date of birth…" here and in `RegistrationE2ETest.java:111`. | "Today's date of birth is refused…". | open |
