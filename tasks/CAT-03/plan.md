# Plan — CAT-03: A product that does not exist

Adopt mode: the story has `status: adopted`. It describes behaviour the shop already has; this plan
changes no production code and no existing test. For each scenario it names where the behaviour
lives and which existing test covers it.

Carried in-session; the profile names no `carrier.plan`. The profile's knowledge skill
(`dca-knowledge`) was not consulted: adopting existing behaviour takes no pattern decision.

## Context

`product` — the Product Catalog context (designed map `project/domain.md`, "Bounded Contexts":
Core subdomain; generated map `docs/architecture/context-map.md`: module `product`, "Product
Catalog"). The product page is one of the shopper's server-rendered surfaces
(`project/product.md`, `## Surfaces`: "catalogue, product page, …"), and the not-found answer for
an unknown product id is given by the product context's incoming web adapter
(`ProductPageController.showProductDetail`). The criteria name the surface themselves
(`/products/no-such-product`, "Browse All Products", "Go to Homepage"), so no new way in is needed.

The two links lead to the catalogue page (`product`) and the home page (`portal`); they are plain
hyperlinks, no Java dependency — consistent with `portal` as Separate Ways on both maps. The
designed and the generated map agree on `product`; this story touches none of its relationships.

## Changes

None — adopt mode.

| Element | Kind | Location | New or changed |
| --- | --- | --- | --- |
| — | — | — | — |

Where the behaviour lives today:

| Element | Kind | Location |
| --- | --- | --- |
| `ProductPageController.showProductDetail` | incoming web adapter (`GET /products/{id}`); renders `error/404` when the result is not found | `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java:86-92` |
| `GetProductByIdUseCase.execute` | use case; returns `GetProductByIdResult.notFound()` for an id no product has | `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductbyid/GetProductByIdUseCase.java:46-53` |
| `ProductId` | shared-kernel value object; accepts any non-blank value, so `no-such-product` reaches the repository | `src/main/java/dev/domaincentric/sample/ecommerce/sharedkernel/domain/model/ProductId.java:15-17`, `:25` |
| `error/404.pug` | template: code, heading, message, the two links | `src/main/resources/templates/error/404.pug:4-12` |
| `layout.pug` | shared layout; tab title falls back to "domaincentric.commerce" when no `title` is set | `src/main/resources/templates/layout.pug:7` |
| `product/catalog.pug` | catalogue heading "Our Products" | `src/main/resources/templates/product/catalog.pug:9` |
| `HomePageController.showHomePage` / `home/index.pug` | home page (`GET /`), heading "Welcome to domaincentric" + ".commerce" | `src/main/java/dev/domaincentric/sample/ecommerce/portal/adapter/incoming/web/HomePageController.java:29-33`; `src/main/resources/templates/home/index.pug:5-7` |

## Acceptance criteria

- unknown-product-shows-the-not-found-page: Given the seeded sample catalogue, in which no product
  has the id "no-such-product", when a visitor opens the product page `/products/no-such-product`,
  then the page shows "404" and the heading "Page Not Found", and the message reads "The page
  you're looking for doesn't exist or has been removed. Perhaps you were looking for one of our
  products?"
  → level: e2e — Playwright (`./gradlew test-e2e`), happy path (marked by the story).
  - Lives in: `ProductPageController.java:88-92` (`if (!result.found()) return "error/404";`) ←
    `GetProductByIdUseCase.java:50-53` → `404.pug:5` (`.error-page__code 404`), `404.pug:6`
    (`h2.error-page__title Page Not Found`), `404.pug:7-9` (the message, as a Pug text block over
    two source lines that renders as one paragraph).
  - Covered by: none. No test in `src/test`, `src/test-integration` or `src/test-e2e` opens an
    unknown product id or asserts "Page Not Found" (grep for `404`, `not-found`, `Page Not Found`,
    `no-such`, `error-browse-link`, `error-home-link` over `src/test*`: no hit on this page).

- not-found-page-has-the-shop-title: Given the seeded sample catalogue, when a visitor opens the
  product page `/products/no-such-product`, then the browser tab title is "domaincentric.commerce".
  → level: integration — `./gradlew test-integration` (MockMvc against the wired application; the
  `<title>` element of the rendered page).
  - Lives in: the not-found branch sets no `title` model attribute (`ProductPageController.java:90-92`
    returns before line 98), so `layout.pug:7` (`title= title || "domaincentric.commerce"`) falls
    back to the shop's name.
  - Covered by: none.

- browse-all-products-leads-to-the-catalogue: Given a visitor on the not-found page of
  `/products/no-such-product`, when they follow "Browse All Products", then the catalogue page
  "Our Products" opens.
  → level: integration — `./gradlew test-integration` (the link's target on the not-found page, and
  the page behind it headed "Our Products").
  - Lives in: `404.pug:11` (`a.btn.btn--accent(href="/products" data-test="error-browse-link")
    Browse All Products`) → `ProductPageController.java:63-74` → `catalog.pug:9` (`h1 Our Products`).
  - Covered by: none. (`ProductDetailPageIntegrationTest.backToProductsOpensTheCatalogue` and
    `HomePageIntegrationTest.browseProductsOpensTheCatalogue` assert the same destination from other
    pages.)

- go-to-homepage-leads-to-the-home-page: Given a visitor on the not-found page of
  `/products/no-such-product`, when they follow "Go to Homepage", then the home page "Welcome to
  domaincentric.commerce" opens.
  → level: integration — `./gradlew test-integration`.
  - Lives in: `404.pug:12` (`a.btn.btn--secondary(href="/" data-test="error-home-link") Go to
    Homepage`) → `HomePageController.java:29-33` → `home/index.pug:5-7` (`h1.hero__title` with the
    text "Welcome to domaincentric" and `span.brand-tld .commerce`).
  - Covered by: none. (`HomePageE2ETest.homePageWelcomesTheVisitorWithWhatTheShopSells` asserts the
    heading "Welcome to domaincentric.commerce", `HomePageE2ETest.java:18`, but reached directly,
    not from the not-found page.)

## Files

- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java` — read: the not-found branch of `GET /products/{id}`.
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductbyid/GetProductByIdUseCase.java` — read: where an unknown id becomes `notFound()`.
- `src/main/resources/templates/error/404.pug` — read: code, heading, message and the two links with their `data-test` attributes.
- `src/main/resources/templates/layout.pug` — read: the tab-title fallback.
- `src/main/resources/templates/product/catalog.pug`, `src/main/resources/templates/home/index.pug` — read: the headings of the two destinations.
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductDetailPageIntegrationTest.java` — read: the MockMvc pattern (title, heading, following a link's target) to mirror.
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/portal/HomePageIntegrationTest.java` — read: `linkTarget`/`open` helpers and the home/catalogue heading assertions.
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProductPageE2ETest.java`, `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/BasePage.java`, `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductDetailPage.java` — read: the page-object style a happy-path browser test would follow.
- `src/test/java/dev/domaincentric/sample/ecommerce/specification/SharedScenariosTest.java` — read: every browser test's `@DisplayName` must be a scenario title of the shared specification (checked locally with `-Pspecification.path=…`, `build.gradle:173`).

## Glossary proposals

- Not-found page: the page the shop shows for an address it has no page for — here a product id no
  product has; it shows "404", "Page Not Found" and offers the catalogue and the home page.

## Open assumptions

- The not-found page is rendered with HTTP status 200: the controller returns the view name
  `error/404` without setting a status (`ProductPageController.java:90-92`). No criterion names a
  status, so this plan neither asserts nor changes it.
- The code, heading and message in `404.pug:5-9` carry no `data-test` attribute (`project/product.md`,
  `## Look and feel`: "every element a test addresses carries a stable `data-test` attribute"); only
  the two links do (`error-browse-link`, `error-home-link`). A test that addresses them may need
  attributes added — a markup-only change for a later stage to decide, not planned here because
  adopt mode changes no production code.
- "The page shows …" is observed on the rendered text; the message's line break in `404.pug:8-9` is
  whitespace in the rendered paragraph, compared after normalising whitespace.
- The shared scenario catalogue (`SharedScenariosTest`) may lack a title for the happy path; a new
  browser test's `@DisplayName` would then need one there. That catalogue lives outside this
  repository and was not read.
- Four scenarios have no test that covers them as stated; in adopt mode that is recorded, not fixed
  here.
