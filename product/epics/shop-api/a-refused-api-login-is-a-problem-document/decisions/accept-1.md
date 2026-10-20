---
id: a-refused-api-login-is-a-problem-document-accept-1
story: a-refused-api-login-is-a-problem-document
stage: document
kind: acceptance
asked: 2026-10-20T11:41:10Z
digest: 19d891af429ba76e6dfde4be794971f7a8f966a9bff5da8edb161e8cb2b6ee8b
---

# Accept a-refused-api-login-is-a-problem-document?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-wrong-password-is-refused: Given an account "ada@example.com" with password "Secret123"; When a client sends `POST /api/auth/login` with `{"email":"ada@example.com","password":"Wrong1234"}`; Then the shop answers 401 with an `application/problem+json` body and the header `WWW-Authenticate: Bearer`; And its title is "Authentication failed" and it carries no detail — `dev.domaincentric.sample.ecommerce.e2e.LoginApiE2ETest#aWrongPasswordIsRefusedWithAProblemDocumentAndABearerChallenge`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-20T18:32:15Z
