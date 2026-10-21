---
id: a-date-of-birth-lies-in-the-past-accept-1
story: a-date-of-birth-lies-in-the-past
stage: document
kind: acceptance
asked: 2026-10-21T21:50:24Z
digest: 08ae16fbb46cfbd8d2c1eeffbd0bf0f2c241c6a155fcb8e2771181ca2ece609d
---

# Accept a-date-of-birth-lies-in-the-past?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- todays-date-of-birth-is-refused: Given a visitor with no cookies on `/register`; When they submit an otherwise valid registration with today's date as Date of Birth; Then the registration page shows "Please check the email address, your name and your date of birth." — `dev.domaincentric.sample.ecommerce.e2e.RegistrationE2ETest#aDateOfBirthOfTodayIsRefusedWithTheCheckYourEntriesSentence`
- todays-date-of-birth-is-refused-through-the-api: Given no account exists for "grace@example.com"; When a client registers "grace@example.com" through the API with password "Secret123" and today's date as date of birth; Then the shop answers 400 with an `application/problem+json` body; And its title is "Unacceptable value" and its `errors` names `dateOfBirth` with the reason — `dev.domaincentric.sample.ecommerce.account.RegistrationApiIntegrationTest#aDateOfBirthOfTodayIsAnUnacceptableValueNamingTheField`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-21T21:59:09Z
