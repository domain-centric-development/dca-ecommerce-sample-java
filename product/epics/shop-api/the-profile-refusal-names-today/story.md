---
id: the-profile-refusal-names-today
epic: shop-api
status: draft
created: 2026-10-23T21:25:20Z
context: account
feature: profile
title: The profile's date-of-birth refusal names today
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The profile's date-of-birth refusal names today

## Story

As a customer changing my profile I want the date-of-birth refusal to say that today is refused too, so that the words match the rule.

## Acceptance criteria

### Rule: The date-of-birth refusal says what the rule refuses

#### todays-date-of-birth-is-refused-in-words-that-name-it (happy path)
- Given a customer logged in on the profile page
- When they save today's date as the date of birth
- Then the profile shows "Please enter a date of birth in the past."

#### future-date-is-refused-in-the-profile
- Given the customer "anna.schmidt@example.com" is logged in on `/account/profile`
- When they save Date of Birth 2999-01-01
- Then the profile shows "Please enter a date of birth in the past."

## Changed expectations

- A date of birth in the future is refused on the profile today with "Please enter a date of birth that is not in
  the future."; after, with "Please enter a date of birth in the past." — the same words today's date gets.

## Assumptions

- answered: the profile refuses today and every later date with "Please enter a date of birth in the past." (the person, 2026-11-01)

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
