---
id: the-notice-reaches-an-encoded-return-page
epic: account
status: approved
created: 2026-10-23T21:25:20Z
context: account
feature: registration
title: The registration notice reaches an encoded return page
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# The registration notice reaches an encoded return page

## Story

As a visitor who registers from a page whose address carries encoded characters I want to see the "account created" notice there too.

## Acceptance criteria

### Rule: The "account created" notice is shown on the page the registration returns to

#### the-notice-reaches-a-return-page-with-an-encoded-address (happy path)
- Given a visitor who opened `/register?returnUrl=/products?q=clean%20code`
- When they register
- Then the catalogue page opens and shows that the account was created

#### the-notice-reaches-a-return-page-with-a-fragment
- Given a visitor who opened `/register?returnUrl=/products%23top`
- When they register
- Then the catalogue page opens and shows that the account was created

## Assumptions

- answered: a return page with a fragment (`#top`) loses the notice the same way — the browser never sends the fragment; the user decided (2026-10-24) it belongs in this story.

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
