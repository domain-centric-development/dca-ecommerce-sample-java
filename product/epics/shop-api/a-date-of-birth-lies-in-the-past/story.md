---
id: a-date-of-birth-lies-in-the-past
epic: shop-api
status: approved
context: account
feature: registration
title: A date of birth lies in the past, on the page and through the API
depends_on: [registration-leads-only-to-shop-pages]
---

# A date of birth lies in the past, on the page and through the API

## Story

As the shop owner I want the registration page and the API to accept the same dates of birth — only dates in the
past — so that a person who is refused on one way is refused on the other as well.

## Acceptance criteria

### Rule: The date of birth must be a real date in the past

#### todays-date-of-birth-is-refused (happy path)
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with today's date as Date of Birth
- Then the registration page shows "Please check the email address, your name and your date of birth."

#### todays-date-of-birth-is-refused-through-the-api
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "Secret123" and today's date as date of
  birth
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `dateOfBirth` with the reason

#### unreadable-date-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Date of Birth "17.05.1990"
- Then the registration page shows "Please enter a date like 1990-05-17"

#### future-date-is-refused
- Given a visitor with no cookies on `/register`
- When they submit an otherwise valid registration with Date of Birth 2999-01-01
- Then the registration page shows "Please check the email address, your name and your date of birth."

## Changed expectations

- A date of birth of today is accepted on the registration page now, and through the API either accepted or refused
  with a problem document that is not titled "Unacceptable value"; after, both refuse it — the page with "Please
  check the email address, your name and your date of birth.", the API with "Unacceptable value".
- The rule "The date of birth must be a real date, and not in the future" reads "The date of birth must be a real
  date in the past".

## Assumptions

- answered: a date of birth lies in the past — today is refused in the API and on the form, in both shops; no minimum
  age (planning/questions-2026-10-15.md Q4.6)
- answered: a refused value through the API is a problem document titled "Unacceptable value" with an `errors` map
  that names each field and why (planning/questions-2026-10-15.md Q4.6)

## Notes

- From TODO #95 (c), checked 2026-10-15: the Java shop accepts today on the page and refuses it through the API, but
  not with "Unacceptable value"; the .NET shop accepts it on both ways. Both scenarios of today are red in both shops.
- Was the draft `the-api-takes-a-date-of-birth-of-today`; the answer turned it round (refuse today instead of
  accepting it).
- The limit's acceptance — yesterday's date is accepted — holds today on both ways; it is a guard for the plan, not a
  criterion.
- `unreadable-date-is-refused` and `future-date-is-refused` are carried word for word because the story renames their
  rule.
- Depends on `registration-leads-only-to-shop-pages`: both change `account/registration`, one after the other.
- Spec (`account/registration`): adds `todays-date-of-birth-is-refused` and
  `todays-date-of-birth-is-refused-through-the-api`; holds `unreadable-date-is-refused`, `future-date-is-refused` and
  `registering-hands-out-a-token`; rule "The date of birth must be a real date, and not in the future" → "The date of
  birth must be a real date in the past".
