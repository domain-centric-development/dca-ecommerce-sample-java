---
id: registration-refusals-name-the-field-in-the-shops-words
epic: shop-api
status: approved
created: 2026-11-01T12:00:08Z
context: account
feature: registration
title: Registration refusals through the API name the field in the shop's words
depends_on: [product-refusals-name-the-field-in-the-shops-words]
---

# Registration refusals through the API name the field in the shop's words

## Story

As a client registering through the API I want every refused value to name the field, with a code and a message
in the shop's words, so that I can correct the request without reading framework messages — and one rule answers
one way, on the page and through the API.

## Acceptance criteria

`errors` holds one entry under the field as the request spells it: `{ "code": <code>, "message": <message> }`,
the shape the product refusals answer with.

### Rule: Registration refusals are problem documents

#### a-malformed-address-is-refused (happy path)
- Given no account exists for "grace@example"
- When a client registers "grace@example" through the API with password "Secret123"
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `email` with code `email-malformed` and the message "Please enter a valid email address."
- And no account exists for "grace@example"

#### a-weak-password-is-refused
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "secret123"
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `password` with code `password-without-capital-letter` and the message "A password needs a capital letter."
- And no account exists for "grace@example.com"

#### a-missing-address-is-refused
- Given no account exists for "grace@example.com"
- When a client registers through the API without an email, with password "Secret123", first name "Grace", last name "Hopper" and date of birth "1906-12-09"
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `email` with code `email-missing` and the message "Please enter an email address."
- And no account exists for "grace@example.com"

#### a-short-password-is-refused-through-the-api
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "Sec123"
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `password` with code `password-too-short` and the message "A password holds at least 8 characters."
- And no account exists for "grace@example.com"

#### a-blank-first-name-is-refused-through-the-api
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with first name "   "
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `firstName` with code `first-name-missing` and the message "Please enter your first name."
- And no account exists for "grace@example.com"

#### an-overlong-last-name-is-refused-through-the-api
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with a last name of 101 characters
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `lastName` with code `last-name-too-long` and the message "A last name holds at most 100 characters."
- And no account exists for "grace@example.com"

#### a-missing-date-of-birth-is-refused-through-the-api
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API without a date of birth
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `dateOfBirth` with code `date-of-birth-missing` and the message "Please enter your date of birth."
- And no account exists for "grace@example.com"

### Rule: The date of birth must be a real date in the past

#### todays-date-of-birth-is-refused-through-the-api
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "Secret123" and today's date as date of
  birth
- Then the shop answers 400 with an `application/problem+json` body titled "Unacceptable value"
- And its `errors` names `dateOfBirth` with code `date-of-birth-not-in-the-past` and the message "A date of birth lies in the past."
- And no account exists for "grace@example.com"

## Changed expectations

- A malformed address through the API is refused today without any detail; after, with `errors` naming the email,
  its code and message.
- A date of birth of today through the API is refused today with `errors` naming it and a reason the framework
  words; after, with the code and message of its rule.
- A password without a capital letter through the API is refused today with 422 "Password too weak" and a detail;
  after, with 400 "Unacceptable value" and `errors` naming the password, its code and message — like every other
  refused value.
- A registration through the API without an address, with a short password, a blank first name, an overlong last
  name or no date of birth is refused today with the framework's reason; after, with the code and message of its
  rule.

## Assumptions

- answered: every refusal of a value from input names its field, with a code in the domain's words and a message
  in the shop's words (the person, 2026-11-01, WP-170 decision 4)
- answered: one named failure per rule of the value — missing (null or blank alike), too long, malformed, out of
  range — each carrying the figures it compared (the person, 2026-11-01, WP-170 decision 5)
- answered: a weak password is refused like every other value, in the `errors` shape (the person, 2026-11-01)
- answered: the rule is checked once, in the value object; the request carries no annotation repeating it and no
  default turning a missing value into one; the message is the adapter's, never the exception's (WP-170
  decisions 1 and 2)

## Notes

- WP-170 (a rule is checked once, in the value object): `RegisterRequest` and `LoginRequest` check email, password,
  names and date of birth with request validation, and the account's values check them again.
- A weak password joins the `errors` shape (the person, 2026-11-01): each part of the password policy has its code
  — `password-too-short`, `password-too-long`, `password-without-capital-letter`, `password-without-small-letter`,
  `password-without-digit` — mapped from the one failure the policy raises and the part it names. The registration
  page keeps its one message for every refused value.
- Logging in through the API is the story `api-login-refusals-answer-like-a-wrong-password`.
- Spec (`account/registration`): changes `a-malformed-address-is-refused`, `a-weak-password-is-refused` and
  `todays-date-of-birth-is-refused-through-the-api`; adds `a-missing-address-is-refused`,
  `a-short-password-is-refused-through-the-api`, `a-blank-first-name-is-refused-through-the-api`,
  `an-overlong-last-name-is-refused-through-the-api`, `a-missing-date-of-birth-is-refused-through-the-api`.
