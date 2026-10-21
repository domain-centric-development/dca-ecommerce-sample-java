---
id: login-leads-only-to-shop-pages-accept-1
story: login-leads-only-to-shop-pages
stage: document
kind: acceptance
asked: 2026-10-21T16:25:10Z
digest: 62a0d1d9b4b19efc40def2f73ff46580735e9076b044b6cf59768cd89c087589
---

# Accept login-leads-only-to-shop-pages?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- the-register-link-drops-an-outside-return-page: Given a visitor with no cookies; When they open `/login?returnUrl=https://elsewhere.example/`; Then the "Register here" link leads to `/register` — `dev.domaincentric.sample.ecommerce.e2e.LoginE2ETest#theRegisterLinkLeadsToTheRegistrationWithoutAReturnPageOfAnotherSite`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.
