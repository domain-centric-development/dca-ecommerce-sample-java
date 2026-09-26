# Plan — CAT-05: Every page shares the shop's header and footer

Mode: **adopt** (`status: adopted` in `project/backlog/browse-catalogue/CAT-05.md`). The story describes
behaviour the shop already has; nothing is built. This plan names, per scenario, where the behaviour
lives and which existing test covers it. It plans no change to production code and no change to an
existing test.

Carrier: in-session; the profile names no `carrier.plan`. Knowledge source: the profile names
`knowledge: dca-knowledge`; adoption takes no pattern decision, so it was not consulted.

## Context

`portal` — the story's `context`. On the designed map (`project/domain.md`, table "Bounded Contexts")
it is "Storefront UI / cross-context views", Generic (UI), related to all others as **Separate Ways**.
On the generated map (`docs/architecture/context-map.md:25`) it is "Web portal, user interface
composition, and cross-context views" with no published `api`/`events` and no relationship edge. The
two maps agree; no finding there. In code the context is `src/main/java/dev/domaincentric/sample/ecommerce/portal/`
(`package-info.java:10-13`, `@BoundedContext`, `allowedDependencies = {"sharedkernel", "infrastructure"}`),
and its glossary says it owns "the landing page and the navigation"
(`portal/domain/glossary.md`, "Module Character").

**Finding (not settled here):** the header and footer are not in any Java package. They are the
shared Pug layout `src/main/resources/templates/layout.pug`, which every page template extends —
also the pages rendered by `product`'s controller (`product/catalog.pug:1`, `product/detail.pug:1`
`extends ../layout`). The portal owns them by language (its glossary's "navigation"), not by a
package; the architecture suite cannot see that ownership. Recorded so the document stage can state it;
it blocks nothing.

The project description decides the shape: server-rendered Pug pages with no client framework
(`project/tech.md`, "Frontend approach"); every element a test addresses carries a stable `data-test`
attribute (`project/product.md`, "Look and feel") — all elements below have one. No size is named by
the story, so none is planned.

Surface check (step 4): the actor is a visitor using the server-rendered pages `/`, `/products` and
`/products/{id}` — all exist (`HomePageController.java:29`, `ProductPageController.java:41,63,86`).
No new surface.

## Changes

| Element | Kind | Location | New or changed |
| --- | --- | --- | --- |
| — | — | — | none (adopt mode: no production change) |

## Acceptance criteria

- **logo-leads-home** (happy path): Given a visitor on the product page of "Domain-Driven Design",
  when they follow the logo "domaincentric.commerce" in the header, then the home page opens.
  → level: **e2e** — Playwright, `./gradlew test-e2e` (`src/test-e2e`), happy path as the story marks it
  (`<!-- happy-path: logo-leads-home -->`).
  - Lives in: `layout.pug:21` `a.site-header__logo(href="/" data-test="site-logo")`, wordmark
    `layout.pug:31-33` ("domaincentric" + `span.brand-tld .commerce`, read as "domaincentric.commerce");
    the product page renders it through `product/detail.pug:1` (`extends ../layout`), route
    `ProductPageController.java:86` (`GET /products/{id}` → `product/detail`); the product
    "Domain-Driven Design" is seeded as `BOOK-001` (`SampleDataInitializer.java:47-48`). The home page
    it opens: `HomePageController.java:29-33` → `home/index.pug`, hero `data-test="hero"` (`index.pug:4`)
    with heading "Welcome to domaincentric.commerce" (`index.pug:5-7`).
  - Covered by: **none**. No test under `src/test`, `src/test-integration` or `src/test-e2e` refers to
    `site-logo` (searched the test sources for `site-logo`, `logo`, `site-header`).

- **nav-home-opens-the-home-page**: Given a visitor on the catalogue page, when they follow "Home" in
  the header navigation, then the home page opens.
  → level: **integration** — MockMvc against the wired application, `./gradlew test-integration`
  (`src/test-integration`), following the link's `href` from the rendered catalogue page.
  - Lives in: `layout.pug:34-37` `nav.nav(aria-label="Main navigation")` →
    `a.nav__link(href="/" data-test="nav-home-link") Home`; catalogue page `product/catalog.pug:1`
    (`extends ../layout`), route `ProductPageController.java:63` (`GET /products`); home page as above.
  - Covered by: **none** (no test refers to `nav-home-link`).

- **nav-products-opens-the-catalogue**: Given a visitor on the home page, when they follow "Products"
  in the header navigation, then the catalogue page "Our Products" opens.
  → level: **integration** — MockMvc, `src/test-integration`.
  - Lives in: `layout.pug:38-39` `a.nav__link(href="/products" data-test="nav-products-link") Products`;
    home page `home/index.pug:1` (`extends ../layout`); target heading `h1 Our Products`
    (`product/catalog.pug:9`).
  - Covered by: **none** for the link (no test refers to `nav-products-link`). The catalogue heading
    alone is asserted by `HomePageIntegrationTest.browseProductsOpensTheCatalogue` — through the hero
    link, not the navigation.

- **footer-names-the-shop**: Given the shop has started, when a visitor opens the catalogue page, then
  the footer reads "domaincentric.commerce — Built with Domain-Centric Architecture", and it offers an
  "Event Log" link to `/backoffice/events`.
  → level: **integration** — MockMvc, `src/test-integration`.
  - Lives in: `layout.pug:76` `footer.site-footer(data-test="site-footer")`; text `layout.pug:78`
    `.site-footer__text domaincentric.commerce &mdash; Built with Domain-Centric Architecture`
    (`&mdash;` renders as "—"); link `layout.pug:80`
    `a.site-footer__link(href="/backoffice/events" data-test="footer-event-log-link") Event Log`.
  - Covered by: **none**. `BackofficeE2ETest` (`:100`) and `BackofficeEventsPage` (`:46`) navigate to
    `/backoffice/events` directly; neither reads the footer link.

- **header-and-footer-on-the-home-page**: Given the shop has started, when a visitor opens the home
  page, then it shows the same header, with "Home" and "Products", and the same footer as the
  catalogue page.
  → level: **integration** — MockMvc, `src/test-integration`, rendering `/` and `/products` and
  comparing their header and footer.
  - Lives in: both pages extend the one layout — `home/index.pug:1` and `product/catalog.pug:1`
    (`extends ../layout`) — so header (`layout.pug:19-66`) and footer (`layout.pug:76-88`) come from
    the same source.
  - Covered by: **none**.

Details the story specifies, each part of a criterion above: the logo's text "domaincentric.commerce"
(logo-leads-home); the labels "Home" and "Products" and their place in the header navigation
(nav-*); the footer's exact wording with the em dash and the link label "Event Log" with target
`/backoffice/events` (footer-names-the-shop); "Our Products" as the catalogue heading
(nav-products-opens-the-catalogue).

## Files

- `src/main/resources/templates/layout.pug` — read: header (`:19-39`: logo, navigation), footer
  (`:76-80`: text, Event Log link) and their `data-test` hooks
- `src/main/resources/templates/home/index.pug` — read: `extends ../layout` (`:1`), the hero that marks
  the home page (`:4-7`)
- `src/main/resources/templates/product/catalog.pug` — read: `extends ../layout` (`:1`), heading
  "Our Products" (`:9`)
- `src/main/resources/templates/product/detail.pug` — read: `extends ../layout` (`:1`)
- `src/main/java/dev/domaincentric/sample/ecommerce/portal/adapter/incoming/web/HomePageController.java` — read: `GET /` (`:29-33`)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java` — read: `/products` (`:63`) and `/products/{id}` (`:86`)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/bootstrap/SampleDataInitializer.java` — read: "Domain-Driven Design" is seeded (`:47-48`)
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/portal/HomePageIntegrationTest.java` — read: the MockMvc page-test pattern (`open`, `link`, `linkTarget`, `text(H1, …)`) to mirror for header/footer tests
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductDetailPageIntegrationTest.java` — read: how a product page is opened by name (`productPageOf("Domain-Driven Design")`, `:67`)
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProductPageE2ETest.java`, `.../e2e/pages/ProductDetailPage.java`, `.../e2e/pages/HomePage.java`, `.../e2e/pages/BasePage.java` — read: the Playwright page objects to reach the product page and recognise the home page (`HomePage` waits for `hero`, `:20-23`; `BasePage.click(dataTest)`, `:49`)

## Glossary proposals

- Site header: the band at the top of every storefront page carrying the shop's logo and the header
  navigation; shared by all pages through one layout.
- Header navigation: the links "Home" and "Products" in the site header that reach the home page and
  the catalogue from any page.
- Site footer: the band at the bottom of every storefront page naming the shop and linking to the
  event log.

(The portal glossary already claims "navigation" as its own under "Module Character" but defines no
term for it.)

## Open assumptions

- "The home page opens" is recognised by the home page's hero (`data-test="hero"`, heading "Welcome to
  domaincentric.commerce"); "the catalogue page opens" by its heading "Our Products".
- The logo's name "domaincentric.commerce" is its visible text: two text nodes, "domaincentric" and the
  `.brand-tld` span ".commerce" (`layout.pug:31-33`), read together.
- "The same header … and the same footer" (header-and-footer-on-the-home-page) is compared on the
  in-scope parts only — logo, "Home", "Products"; footer text and the Event Log link. The header also
  holds the cart link, mini-basket and login/register links, and the footer the theme switcher; all are
  out of scope (`fill-a-cart`, `ACC-01`, `ACC-02`, `fits-its-surroundings`) and not asserted.
- Out of scope and not planned: the event log page behind "Event Log" (`run-the-shop`); success and
  error messages (`layout.pug:70-73`).
- Adoption changes no production code. Whether the test stage adds tests for the five uncovered
  scenarios is the test stage's call; this plan only names that none exists today.
