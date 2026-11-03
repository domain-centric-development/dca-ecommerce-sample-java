---
story: a-second-login-takes-over-no-account-as-guest
epic: shop-with-an-account
---

# Findings — a-second-login-takes-over-no-account-as-guest

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/LoginPageController.java:165 | minor | The logged-in notice has a made-up fallback that would read "You are logged in as your account." | `email().orElse("your account")` covers a registered identity without an email, which the token service never builds; the guard (:155) and the notice (:165) read the identity in two calls | Read the identity once per handler, pass it to `askToLogOutFirst(model, identity)`, take its email without an invented phrase | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/account/adapter/incoming/web/LoginPageController.java:125-130 | minor | The login still chooses between two kinds of session token, but one of them can no longer happen. | With the guard at :102 the captured id is always a guest's, so the `equals` branch is dead | Drop the equal branch and name the captured value `guestUserId` | open |
| 3 | clean-code | src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/LoginPage.java:158-171 | minor | The login page's test helper repeats the same "read an element's text" code a third time. | `loggedInMessage()` and `logoutButton()` repeat the shape of `errorMessage()`; `logoutButton()` returns the label, not the button | Extract a private `textOf(dataTest)` for all three; rename `logoutButton()` to `logoutButtonText()` | open |
