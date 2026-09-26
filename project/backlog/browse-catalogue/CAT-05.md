---
id: CAT-05
epic: browse-catalogue
status: adopted
context: portal
title: Every page shares the shop's header and footer
depends_on: [CAT-04]
---

# Every page shares the shop's header and footer

## Story

As a visitor I want the same header and footer on every page, so that I always know which shop I am in and
can get back to the home page or the products from anywhere.

## Acceptance criteria

<!-- happy-path: logo-leads-home -->

### Rule: The header carries the shop's name and leads home

#### logo-leads-home
- Given a visitor on the product page of "Domain-Driven Design"
- When they follow the logo "domaincentric.commerce" in the header
- Then the home page opens

### Rule: The header navigation reaches the home page and the catalogue

#### nav-home-opens-the-home-page
- Given a visitor on the catalogue page
- When they follow "Home" in the header navigation
- Then the home page opens

#### nav-products-opens-the-catalogue
- Given a visitor on the home page
- When they follow "Products" in the header navigation
- Then the catalogue page "Our Products" opens

### Rule: The footer names the shop and links to the event log

#### footer-names-the-shop
- Given the shop has started
- When a visitor opens the catalogue page
- Then the footer reads "domaincentric.commerce — Built with Domain-Centric Architecture"
- And it offers an "Event Log" link to `/backoffice/events`

#### header-and-footer-on-the-home-page
- Given the shop has started
- When a visitor opens the home page
- Then it shows the same header, with "Home" and "Products", and the same footer as the catalogue page

## Out of scope

- The cart link and mini-basket in the header — `fill-a-cart`.
- "Login", "Register", "My Account", "Welcome, <email>" and "Logout" in the header — `ACC-01`, `ACC-02`.
- The event log behind "Event Log" — `run-the-shop`.
- The theme switcher in the footer — `fits-its-surroundings`.
- Success and error messages above the page content: each belongs to the story whose action sets it.

## Notes

- Inventory: portal H6 (logo, navigation), H7.
