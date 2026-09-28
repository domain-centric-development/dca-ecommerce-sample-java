# Plan — CAT-04: The home page

**Mode: adopt** (`status: adopted`). The story describes behaviour the shop already has; this plan
builds nothing, changes no production code and no existing test. For each scenario it names where
the behaviour lives and which existing test covers it.

Carrier: the profile names no `carrier.plan:`; the stage was done in-session as the skill describes.
Knowledge: the profile names `knowledge: dca-knowledge`; no pattern question arises in an adoption
(nothing is designed), so the catalog was not consulted.

## Context

`portal` — the story's `context`. On the designed map (`project/domain.md`, "Bounded Contexts":
`portal` — "Storefront UI / cross-context views", Generic (UI); relationship "Separate Ways",
aggregation client-side, no Java cross-context imports) and on the generated map
(`docs/architecture/context-map.md`, "Bounded Contexts": `portal` — "Web portal, user interface
composition, and cross-context views", no published interfaces, no upstream relationships). The two
maps agree: the generated one shows no edge for `portal`, which is what Separate Ways means.

It belongs here because the home page is the portal's one own term: the portal glossary
(`src/main/java/dev/domaincentric/sample/ecommerce/portal/domain/glossary.md`, "Own Terms →
HomePage": "Entry page … `GET /` → `home/index`") and the package declaration
(`portal/package-info.java:4` — "Responsible for the web portal: the landing page, navigation …";
`:13` `allowedDependencies = {"sharedkernel", "infrastructure"}`). The page links to the catalogue
and the cart by URL, never by call — Separate Ways holds.

Surface: the product description lists "home page" among the shopper's server-rendered pages
(`project/product.md`, `## Surfaces`); the technical description prescribes Pug templates and no
client framework (`project/tech.md`, `## Frontend approach`). The page exists, so step 4's check
passes: the actor (a visitor) reaches it at `GET /`.

## Changes

None — an adopted story builds nothing. Where each element lives today:

| Element | Kind | Location | New or changed |
| --- | --- | --- | --- |
| `HomePageController.showHomePage` | incoming web adapter (portal) | `src/main/java/dev/domaincentric/sample/ecommerce/portal/adapter/incoming/web/HomePageController.java:29-34` | unchanged |
| `home/index` | Pug template | `src/main/resources/templates/home/index.pug` | unchanged |
| `layout` (`<title>`) | shared Pug layout | `src/main/resources/templates/layout.pug:7` | unchanged |
| catalogue page (target of two links) | product's incoming web adapter + template | `product/adapter/incoming/web/ProductPageController.java:41,63`; `src/main/resources/templates/product/catalog.pug:9` | unchanged |

## Acceptance criteria

Keys verbatim from the story. Levels: the happy path `home-page-welcomes-the-visitor` → `e2e`
(Playwright, `./gradlew test-e2e`; profile `e2eTest:` + `browser: playwright`); every other
scenario → `integration` (`src/test-integration/java`, `./gradlew test-integration`; profile
`test.integration:`), the page fetched over its HTTP surface through the wired application with
MockMvc, as `product/ProductCatalogPageIntegrationTest.java` does for the catalogue. No scenario's
`Then` needs a browser after the page has loaded, so none is `browser-only`.

### Rule: The home page says what the shop is

- **home-page-welcomes-the-visitor** (happy path): Given the shop has started, when a visitor opens
  `/`, then the heading reads "Welcome to domaincentric.commerce", the subtitle reads "Books,
  modelling supplies and hexagon merchandise for people who draw boundaries", and below it reads
  "Everything this architecture is made of: the books it grew out of, the supplies a design
  workshop runs on, and hexagons for your desk."
  → level: e2e — Playwright via `./gradlew test-e2e`
  - Lives in: `GET /` → `HomePageController.java:29-33` returns `home/index`; heading
    `index.pug:5-7` (`h1.hero__title` = text "Welcome to domaincentric" + `span.brand-tld`
    ".commerce"), subtitle `index.pug:8` (`p.hero__subtitle`), description `index.pug:9`
    (`p.hero__description`), inside `section.hero(data-test="hero")` `index.pug:4`.
  - Covered by: **none**. No test under `src/test`, `src/test-integration` or `src/test-e2e`
    opens `/` (searched for `"/"` navigations, `hero`, `HomePageController`, `home/index`,
    `Welcome`); `BaseE2ETest.navigateTo` (`BaseE2ETest.java:127`) is never called with `/`.

- **home-page-title**: Given the shop has started, when a visitor opens the home page, then the
  browser tab title is "domaincentric.commerce".
  → level: integration — MockMvc over `GET /` in `src/test-integration/java`
  - Lives in: `HomePageController.java:31` (`model.addAttribute("title", "domaincentric.commerce")`)
    rendered by `layout.pug:7` (`title= title || "domaincentric.commerce"`); `index.pug:1`
    `extends ../layout`.
  - Covered by: **none**.

### Rule: The home page leads to the products and the cart

- **browse-products-opens-the-catalogue**: Given a visitor on the home page, when they follow
  "Browse Products", then the catalogue page "Our Products" opens.
  → level: integration — MockMvc: `GET /` yields the link's target, `GET` of that target yields
  the heading "Our Products"
  - Lives in: `index.pug:11` (`a … (href="/products" data-test="browse-products-link") Browse
    Products`); target `ProductPageController.java:41` (`@RequestMapping("/products")`) + `:63`
    (`@GetMapping`), heading `catalog.pug:9` (`h1 Our Products`).
  - Covered by: **none** for the link. The catalogue heading itself is covered by
    `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductCatalogPageIntegrationTest.java`,
    `ProductCatalogPageIntegrationTest.pageIsTitledHeadedAndPlacedAsTheCatalogue` (line 105:
    `text(HEADING, page)` is "Our Products") — it does not start from the home page.

- **view-cart-links-to-the-cart**: Given the shop has started, when a visitor opens the home page,
  then the section below the heading offers a "View Cart" link to `/cart`.
  → level: integration — MockMvc over `GET /`
  - Lives in: `index.pug:12` (`a … (href="/cart" data-test="view-cart-link") View Cart`), inside
    `.hero__actions` (`index.pug:10`) of the hero section, after the heading and texts
    (`index.pug:5-9`).
  - Covered by: **none**. (The cart page behind the link is out of scope — `CRT-02`.)

### Rule: The home page explains why to shop here

- **why-shop-with-us**: Given the shop has started, when a visitor opens the home page, then a
  section "Why Shop With Us" shows four features, in this order, each title with its text:
  "Free Shipping" — "Books ship cushioned, posters rolled in a tube, hexagons in a fitted box. Free
  over €50."; "Secure Payments" — "Card or invoice, encrypted end to end. Your payment details never
  touch our order history."; "Built to Last" — "Beech and oiled oak, hard enamel, heavyweight
  cotton. Objects that survive a decade of workshops."; "Easy Returns" — "Not the hexagon you
  pictured? Send any item back within 30 days for a full refund."
  → level: integration — MockMvc over `GET /`
  - Lives in: `index.pug:14-35` — `section(data-test="features")`, `h2.section__title Why Shop
    With Us` (`:15`), four `.feature-card`s with `h3.feature-card__title` /
    `p.feature-card__description` (`:17-35`); "€" is the entity `&#8364;` (`:20`).
  - Covered by: **none**.

- **popular-categories**: Given the shop has started, when a visitor opens the home page, then a
  section "Popular Categories" shows, as text and not as links, in this order: "Books" — "The works
  this architecture was synthesized from — Evans, Vernon, Cockburn, Fowler, Martin."; "Modeling" —
  "Sticky notes, hexagon magnets, posters and card decks for your next design workshop.";
  "Apparel" — "Shirts, hoodies and caps that explain your architecture before you open your
  laptop."; "Desk & Office" — "Mugs, coasters, a hex-grid notebook, and the wooden hexagon for your
  desk."; "Stickers & Pins" — "Small enough for a laptop lid, loud enough for a conference
  hallway."
  → level: integration — MockMvc over `GET /`
  - Lives in: `index.pug:37-58` — `section(data-test="categories")`, `h2.section__title Popular
    Categories` (`:38`), five `.highlight-card`s whose title is a `span.highlight-card__title`, not
    an `a` (`:41,45,49,53,57`); "&" is `&amp;` (`:53,57`).
  - Covered by: **none**.

### Rule: The page closes with a call to shop

- **shop-now-opens-the-catalogue**: Given a visitor on the home page, below "Ready to Draw Some
  Boundaries?" and "Browse the full catalog: the books, the modelling supplies, and the hexagons.",
  when they follow "Shop Now", then the catalogue page "Our Products" opens.
  → level: integration — MockMvc: `GET /` yields the link's target, `GET` of that target yields
  the heading "Our Products"
  - Lives in: `index.pug:60-65` — `section(data-test="cta-section")` / `.hero(data-test="cta-banner")`,
    `h2.hero__title Ready to Draw Some Boundaries?` (`:62`), `p.hero__subtitle Browse the full
    catalog: …` (`:63`), `a … (href="/products" data-test="shop-now-link") Shop Now` (`:65`);
    target as in `browse-products-opens-the-catalogue`.
  - Covered by: **none** for the link; the catalogue heading as above.

Every scenario is shown by the code, so the story is adoptable; no scenario is covered by an
existing test.

## Files

- `src/main/java/dev/domaincentric/sample/ecommerce/portal/adapter/incoming/web/HomePageController.java` — read: the `GET /` handler and the page title (`:29-34`)
- `src/main/resources/templates/home/index.pug` — read: every text, link and `data-test` hook the scenarios name
- `src/main/resources/templates/layout.pug` — read: the `<title>` (`:7`)
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java` — read: the catalogue route the two links reach (`:41,63`)
- `src/main/resources/templates/product/catalog.pug` — read: the heading "Our Products" (`:9`)
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductCatalogPageIntegrationTest.java` — read: the pattern for a page test over MockMvc (`catalogPage()`, `text(HEADING, …)`) to mirror for a home-page integration test
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/BaseE2ETest.java`, `.../e2e/pages/ProductCatalogPage.java`, `.../e2e/pages/BasePage.java` — read: the Playwright base class and page-object pattern (`navigateTo`, `BASE_URL`) for a home-page e2e test

No file is planned to change.

## Glossary proposals

None. The one own term the story relies on, **HomePage**, is already in the portal glossary; the
catalogue and cart are referenced terms listed there ("Referenced Terms"). "Feature" and "Popular
Category" appear only as page copy, not as domain concepts — the portal has no model of them
(`index.pug` holds them as static text), so no term is proposed.

## Open assumptions

- The heading is asserted as the element's visible text: `index.pug:5-7` renders the text
  "Welcome to domaincentric" followed by `<span class="brand-tld">.commerce</span>`; Pug adds no
  whitespace between piped text and a following tag, so the text reads
  "Welcome to domaincentric.commerce". Not verified by rendering (this stage runs nothing).
- The story's "section below the heading" (`view-cart-links-to-the-cart`) is the hero's
  `.hero__actions` block (`index.pug:10-12`), which follows the heading, subtitle and description.
- Out of scope and not planned: the cart page behind "View Cart" (`CRT-02`), the shared header and
  footer (`CAT-05`), a product slider, the theme switcher and phone layout
  (`fits-its-surroundings`). The layout's `meta name="description"` (`layout.pug:6`) is not named
  by any scenario and is not planned.
- Adoption changes no production code. Whether the test stage adds tests for the uncovered
  scenarios is for that stage and the adopt process; the levels above are what such tests would use.
