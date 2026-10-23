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
- Then the page refuses it with words that say the date must lie in the past

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
