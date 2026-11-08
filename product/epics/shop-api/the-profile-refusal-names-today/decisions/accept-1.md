---
id: the-profile-refusal-names-today-accept-1
story: the-profile-refusal-names-today
stage: document
kind: acceptance
asked: 2026-11-03T15:10:49Z
digest: aeed104c3a51e92e10a730eb0a32a9488360f9af5783be68c550a134a8a97b06
---

# Accept the-profile-refusal-names-today?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- todays-date-of-birth-is-refused-in-words-that-name-it: Given a customer logged in on the profile page; When they save today's date as the date of birth; Then the profile shows "Please enter a date of birth in the past." — `dev.domaincentric.sample.ecommerce.e2e.ProfileE2ETest#todaysDateOfBirthIsRefusedWithTheSentenceThatAsksForThePast`
- future-date-is-refused-in-the-profile: Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`; When they save Date of Birth 2999-01-01; Then the profile shows "Please enter a date of birth in the past." — `dev.domaincentric.sample.ecommerce.account.ProfileIntegrationTest#aDateOfBirthInTheFutureIsRefused`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.
