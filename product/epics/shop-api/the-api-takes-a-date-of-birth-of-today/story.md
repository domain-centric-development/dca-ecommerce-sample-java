---
id: the-api-takes-a-date-of-birth-of-today
epic: shop-api
status: draft
context: account
feature: registration
title: Registering through the API takes the dates of birth the page takes
depends_on: []
---

# Registering through the API takes the dates of birth the page takes

## Story

As a partner application I want the API to accept the same dates of birth as the registration page, so that a person
who can register on the shop's page can register through my application too.

## Acceptance criteria

### Rule: The date of birth must be a real date, and not in the future

#### registering-with-todays-date-of-birth-through-the-api (happy path)
- Given no account exists for "grace@example.com"
- When a client registers "grace@example.com" through the API with password "Secret123" and today's date as date of
  birth
- Then the shop answers 201
- And the body has `success` true, a non-empty `token` and `email` "grace@example.com"

## Changed expectations

- A date of birth of today is accepted on the registration page and refused with 400 through the API now; after, both
  accept it.

## Assumptions

- open: is a date of birth of today wanted at all — accept it on both ways (as written), or refuse it on both, so the
  rule reads "in the past"?
- open: the scenario already holds in one of the two shops — adopt it there, and build it only where it does not hold?

## Notes

- From TODO #95 (c), checked 2026-10-15: still true in the Java shop; fixed in the .NET shop (both ways accept today).
  Where the person decides to refuse today instead, the story changes `future-date-is-refused` and is red in the .NET
  shop instead.
- Spec (`account/registration`): adds `registering-with-todays-date-of-birth-through-the-api`; holds
  `future-date-is-refused` and `registering-hands-out-a-token`.
