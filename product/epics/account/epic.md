---
id: account
title: Have an account
intent: Returning customers type their details again for every order and cannot change what the shop knows about them
goal: A returning customer logs in, finds their details already filled in, and keeps profile and password up to date
metric: AccountRegistered
domain_contact: shop-owner
---

# Have an account

## Intent

Returning customers type their details again for every order and cannot change what the shop knows about them.

## Goal

A returning customer logs in, finds their details already filled in, and keeps profile and password up to date.

## Outcome

The epic is measured by the outcome event `AccountRegistered`: as long as it is not published in production, the epic has not delivered, however much code exists.

## Stories

Delivery order is `depends_on`, ties by id.

- register-an-account — Register an account
- log-in-and-log-out — Log in and log out
- registration-refuses-an-unusable-form — Registration refuses an unusable form
- one-account-per-address-and-per-browser — One account per address and per browser
- a-failed-login-gives-nothing-away — A failed login gives nothing away
- my-account-overview — My account overview
- keep-the-profile-up-to-date — Keep the profile up to date
- the-profile-refuses-an-unusable-change — The profile refuses an unusable change
- change-the-password — Change the password
- a-refused-password-change-changes — A refused password change changes nothing
- the-password-policy — The password policy
- accounts-start-fresh-with-the-shop — Accounts start fresh with the shop
