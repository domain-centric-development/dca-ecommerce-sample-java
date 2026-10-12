---
id: the-visitor-chooses-the-shops-look
epic: fits-its-surroundings
status: adopted
context: portal
feature: appearance
title: The visitor chooses the shop's look
depends_on: [CAT-01, the-cart-page]
---

# The visitor chooses the shop's look

## Story

As a visitor I want to pick the look of the shop from a few themes and keep it on every page and visit, so that
I shop in a look that suits me.

## Acceptance criteria

<!-- happy-path: choosing-a-theme-applies-at-once -->

### Rule: Every page offers five themes in its footer; Blueprint is the default

#### the-footer-offers-five-themes
- Given a browser that has never chosen a theme
- When the visitor opens the catalogue page
- Then the footer shows a "Theme" choice with the options Blueprint, Organic, Sharp & Minimal, Classic and Neon + Dark, in that order
- And Blueprint is selected and the page is shown in the Blueprint theme

### Rule: A chosen theme applies at once

#### choosing-a-theme-applies-at-once
- Given the visitor is on the catalogue page in the Blueprint theme
- When they choose "Organic" in the footer's theme choice
- Then the page is shown in the Organic theme without being reloaded

### Rule: The browser remembers the choice across pages and visits

#### the-choice-follows-to-other-pages
- Given the visitor chose "Neon + Dark" on the catalogue page
- When they open the cart page
- Then the cart page is shown in the Neon + Dark theme and the footer shows "Neon + Dark" selected

#### the-choice-survives-a-new-visit
- Given the visitor chose "Classic" and closed the shop
- When they open the home page again in the same browser
- Then it is shown in the Classic theme and the footer shows "Classic" selected

#### choosing-blueprint-again-returns-to-the-default
- Given the visitor chose "Sharp & Minimal"
- When they choose "Blueprint" and open the catalogue page again
- Then the catalogue page is shown in the Blueprint theme

### Rule: The choice belongs to one browser

#### another-browser-keeps-the-default
- Given the visitor chose "Organic" in one browser
- When the catalogue page is opened in another browser
- Then that browser shows it in the Blueprint theme

## Out of scope

- Page transitions — `moving-between-pages-is-animated`.
- The rest of the shared header and footer — the catalogue epic.

## Notes

- Replay story: `FIT-01` (fits-its-surroundings).
- Inventory: portal H8; cross-cutting X16.
