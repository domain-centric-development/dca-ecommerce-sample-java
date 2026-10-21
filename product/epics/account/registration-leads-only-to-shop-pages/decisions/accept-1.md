---
id: registration-leads-only-to-shop-pages-accept-1
story: registration-leads-only-to-shop-pages
stage: document
kind: acceptance
asked: 2026-10-21T19:29:16Z
digest: 76eb3171bfac1c0d2c9ef7abf74f4daf9dfec512e8fe93f826bedb1e767b794a
---

# Accept registration-leads-only-to-shop-pages?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- an-outside-return-page-leads-home-after-registering: Given a visitor with no cookies who opened `/register?returnUrl=https://elsewhere.example/`; When they submit a valid registration for "anna.schmidt@example.com"; Then the home page `/` opens and shows "Account created successfully! Welcome!" — `dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#aRegistrationLeadsToTheHomePageInsteadOfAnotherSitesReturnPage`
- a-scheme-relative-return-page-leads-home-after-registering: Given a visitor with no cookies who opened `/register?returnUrl=//elsewhere.example/`; When they submit a valid registration for "anna.schmidt@example.com"; Then the home page `/` opens and shows "Account created successfully! Welcome!" — `dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#aRegistrationLeadsToTheHomePageInsteadOfASchemeRelativeReturnPage`
- the-login-link-drops-an-outside-return-page: Given a visitor with no cookies; When they open `/register?returnUrl=https://elsewhere.example/`; Then the "Login here" link leads to `/login` — `dev.domaincentric.sample.ecommerce.account.RegistrationPageIntegrationTest#loginLinkLeadsToTheLoginWithoutAReturnPageOfAnotherSite`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T19:36:18Z
