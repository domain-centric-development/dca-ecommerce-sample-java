# Judge — CAT-05

Mode: **adopt** (`status: adopted`). Nothing was built — the change adds three test files only
(`.verify/changed.txt`) — so the review is the claim that each mapped test proves its scenario.
Nothing else was reviewed.

## Verdict
verdict: pass

## Perspectives covered
- ddd: not applicable in adopt mode — no production code changed; `review-ddd` (the profile's carrier) not invoked
- hexagonal: not applicable in adopt mode — no production code changed; `review-hexagonal` not invoked
- clean-code: not applicable in adopt mode — no production code changed; `review-clean-code` not invoked
- dca (added): not applicable in adopt mode — no production code changed; `dca-review` not invoked
- scenario proof (the adopt-mode review): in-session
- Knowledge source: `dca-knowledge` is named in the profile; no pattern question came up, so it was not consulted.

## Confirmed defects
| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |

None.

## Considered and dropped
- `SiteHeaderAndFooterIntegrationTest.java:37,48` take the page's first `<nav>` (`navigation(open(...))`), not
  the `<nav>` inside the `site-header`. The scenarios say "in the header navigation". Dropped: the first `<nav>` in
  every rendered page is the header's (`layout.pug:34`, inside `header.site-header` from `:19`; nothing before it
  renders a `<nav>`), so the test does read the header navigation today. Scoping it to
  `element(page, "header", "site-header")`, as `homePageShowsTheSameHeaderAndFooterAsTheCatalogue` does, would be
  more robust. That is a hardening, not a defect.
- The integration tests "follow" a link by reading its `href` and requesting that path, without driving a browser.
  Dropped: these are integration tests, not end-user tests. By the test-level rule only the happy path
  (`logo-leads-home`) belongs in the browser, and it is driven through Playwright (`SiteHeaderE2ETest.java:18-25`).
- `SiteHeader.logoName()` (`SiteHeader.java:31`) strips all whitespace before comparing it with
  "domaincentric.commerce". Dropped: the wordmark is two text nodes, "domaincentric" and ".commerce"
  (`layout.pug:31-33`), and the plan's open assumptions record that they are read together as one name.
- `homePageShowsTheSameHeaderAndFooterAsTheCatalogue` compares only the logo, the navigation and the footer's text
  and link, not the whole header and footer. Dropped: the rest of the header (cart, mini-basket, login and
  register) and of the footer (the theme switcher) is out of scope in the story (`## Out of scope`), and the
  plan records that restriction.

## Criteria re-checked
- logo-leads-home: met. Given: the visitor opens the product page of "Domain-Driven Design" through the catalogue
  (`SiteHeaderE2ETest.java:18`). When: they follow the logo. The test first confirms that the logo's name is
  "domaincentric.commerce" (`:20`), then follows it (`:22`). Then: the path is `/` and the heading reads
  "Welcome to domaincentric.commerce" (`:24-25`). The test drives a browser, the story's happy-path level.
- nav-home-opens-the-home-page: met. Given: the catalogue page `/products` is open (`SiteHeaderAndFooterIntegrationTest.java:37`).
  When: the visitor follows the navigation link labelled "Home" (`:39`, `:41`). Then: the page it opens is the home
  page, recognised by its heading (`:41-42`).
- nav-products-opens-the-catalogue: met. Given: the home page `/` is open (`:48`). When: the visitor follows
  "Products" (`:50`, `:52`). Then: the heading reads "Our Products" (`:52-53`).
- footer-names-the-shop: met. The catalogue page is open (`:59`). Then: the footer text reads exactly
  "domaincentric.commerce — Built with Domain-Centric Architecture" (`:61-62`). And: the "Event Log" link points to
  `/backoffice/events` (`:63-64`).
- header-and-footer-on-the-home-page: met. The home page's `site-header` carries "Home" and "Products" (`:74-75`).
  Its logo and navigation equal the catalogue page's (`:76-77`), and so do its footer text and Event Log link (`:78`).
  The break that hides the link on the home page only makes the test red (`tests.md`, Notes).
- Breaks: each of the five scenarios has a break under `tasks/CAT-05/breaks/`, and each makes its own test red
  (`tests.md`, Notes). So every test depends on the behaviour it claims to prove.
