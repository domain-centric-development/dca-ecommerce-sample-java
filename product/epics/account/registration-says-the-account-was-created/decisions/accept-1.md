---
id: registration-says-the-account-was-created-accept-1
story: registration-says-the-account-was-created
stage: document
kind: acceptance
asked: 2026-10-14T17:31:08Z
digest: e5444b061442b47ee56895d7fc457ccf845fc34209d04900df262e9fd10d2d12
---

# Accept registration-says-the-account-was-created?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- registration-says-the-account-was-created: Given a visitor with no cookies on `/register`; When they submit First Name "Anna", Last Name "Schmidt", Date of Birth 1990-05-17, Email "anna.schmidt@example.com",; Then the home page `/` opens and shows "Account created successfully! Welcome!" — `dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#theHomePageARegistrationLeadsToSaysTheAccountWasCreated`
- the-page-returned-to-says-the-account-was-created: Given a visitor with no cookies who opened `/register?returnUrl=/products`; When they submit a valid registration for "anna.schmidt@example.com"; Then the catalogue page `/products` opens and shows "Account created successfully! Welcome!" — `dev.domaincentric.sample.ecommerce.account.RegistrationNoticeIntegrationTest#thePageTheVisitorCameFromSaysTheAccountWasCreated`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-14T17:32:15Z
