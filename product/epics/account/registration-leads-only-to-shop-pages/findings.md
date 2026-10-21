---
story: registration-leads-only-to-shop-pages
epic: account
---

# Findings — registration-leads-only-to-shop-pages

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/RegisterPageController.java:57 | minor | The registration page describes its return page in two different ways. | GET handler's `@param returnUrl` still says "URL to redirect to"; the POST Javadoc now says it is followed only when it is a page of the shop, and GET uses it for the login link too. | Use the POST wording for the GET `@param`. | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/LoginPageController.java:146 | minor | The login page does not say its return page may be missing, unlike the registration page. | `addReturnPage(Model, String returnUrl)` lacks `@Nullable`, while its twin in `RegisterPageController` and `ReturnPage.carriedOnTo` carry it. | Mark the parameter `@Nullable`. | open |
