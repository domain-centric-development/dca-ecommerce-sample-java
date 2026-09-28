# Plan — CAT-02: The product page

Adopt mode: the story has `status: adopted`. It describes behaviour the shop already has; this plan
changes no production code and no existing test. For each scenario it names where the behaviour
lives and which existing test covers it.

Carried in-session; the profile names no `carrier.plan`. The profile's knowledge skill
(`dca-knowledge`) was not consulted: adopting existing behaviour takes no pattern decision.

## Context

`product` — the Product Catalog context (designed map `project/domain.md`, "Bounded Contexts":
Core subdomain; generated map `docs/architecture/context-map.md`: module `product`, "Product
Catalog"). The product page is one of the shopper's server-rendered surfaces
(`project/product.md`, `## Surfaces`: "catalogue, product page, …") and is served by the product
context's incoming web adapter. The story's criteria name the surface themselves (the "View
Details" link, the product page, "Back to Products"), so no new way in is needed.

The designed and the generated map agree on `product` and on its relationships that matter here
(it reads `pricing` and `inventory` through their `api`); this story touches neither relationship
— price and availability are out of scope (`PRC-01`, `AVL-01`).

## Changes

None — adopt mode.

| Element | Kind | Location | New or changed |
| --- | --- | --- | --- |
| — | — | — | — |

Where the behaviour lives today:

| Element | Kind | Location |
| --- | --- | --- |
| `ProductPageController.showProductDetail` | incoming web adapter (`GET /products/{id}`) | `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java:86-101` |
| `GetProductByIdInputPort` / `GetProductByIdQuery` / `GetProductByIdResult` | use case input port | `product/application/getproductbyid/` (imported at `ProductPageController.java:6-8`) |
| `ProductDetailPageViewModel.fromResult` | page view model (title = product name) | `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductDetailPageViewModel.java:48-67` (`pageTitle` ← `product.name()`, line 66) |
| `product/detail.pug` | template | `src/main/resources/templates/product/detail.pug` |
| `product/catalog.pug` | template (the card's "View Details" link) | `src/main/resources/templates/product/catalog.pug:30` |
| `layout.pug` | shared layout (`<title>`) | `src/main/resources/templates/layout.pug:7` |
| `SampleDataInitializer` | seed data | `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/bootstrap/SampleDataInitializer.java:46-53` (Domain-Driven Design), `:56-62` (Clean Architecture), `:110-116` (Team Topologies) |

## Acceptance criteria

- view-details-opens-the-product-page: Given the seeded product "Domain-Driven Design", when a
  visitor follows the "View Details" link on its catalogue card, then the product page of
  "Domain-Driven Design" opens, and its heading reads "Domain-Driven Design".
  → level: e2e — Playwright (`./gradlew test-e2e`), happy path (marked by the story).
  - Lives in: `catalog.pug:30` (`a.product-card__action(href="/products/" + product.productId()
    data-test="view-product") View Details`) → `ProductPageController.java:86-101` renders
    `product/detail` → `detail.pug:13` (`h1.product-detail__title #{productDetail.name()}`).
  - Covered by: **no test asserts it as stated.** Partial coverage:
    - `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductCatalogPageIntegrationTest.java`,
      `ProductCatalogPageIntegrationTest.everyCardOffersAViewDetailsLinkToThatProductsPage` follows
      every card's "View Details" link (the "Domain-Driven Design" card among them) and asserts
      status 200 and that the page *contains* the title (lines 79-101) — not that the heading reads
      it. The file is untracked in the working tree (`git status`: `?? src/test-integration/…/product/`),
      i.e. written for CAT-01 and not committed yet.
    - The e2e page objects reach a product page (`ProductCatalogPage.viewFirstProduct`,
      `ProductDetailPage` waits for `data-test="product-detail"`, `ProductDetailPage.java:22-25`),
      used by `CartMergeE2ETest.fullCartMergeFlow`, `CheckoutGuestE2ETest.completeGuestCheckoutFlow`,
      `MobileLayoutE2ETest.shopFitsAPhoneViewport` and others — but through the first card
      ("\"Bounded Context\" Enamel Pin", per `ProductCatalogPageIntegrationTest.java:62`), not
      "Domain-Driven Design", and none asserts the heading.
    - No e2e test of the happy path exists.

- product-page-shows-image-description-and-category: Given the seeded product "Domain-Driven
  Design", when a visitor opens its product page, then the page shows the product's image, the
  description "The seminal work by Eric Evans that introduced the software industry to
  Domain-Driven Design. This essential guide teaches you how to tackle complexity in the heart of
  software by connecting implementation to an evolving model of the business domain.", and it
  shows "Category" with the value "Books".
  → level: integration — `./gradlew test-integration` (MockMvc against the wired application).
  - Lives in: `detail.pug:20-22` (`img(src=productDetail.imageUrl() alt=productDetail.name())`;
    seed image `/images/products/ddd-book.webp`, `SampleDataInitializer.java:50`),
    `detail.pug:31-33` (description paragraph), `detail.pug:35-38` (meta label "Category", value
    `productDetail.category()`; seed "Books", `SampleDataInitializer.java:52`).
  - Covered by: none. (`ProductCatalogPageIntegrationTest.cardShowsTheProductsNameDescriptionAndImage`
    asserts the same description and image on the catalogue *card*, not on the product page.)

- product-page-title-is-the-product-name: Given the seeded product "Clean Architecture", when a
  visitor opens its product page, then the browser tab title is "Clean Architecture".
  → level: integration — `./gradlew test-integration` (the `<title>` element of the rendered page).
  - Lives in: `ProductDetailPageViewModel.java:66` (`pageTitle` = product name) →
    `ProductPageController.java:98` (`model.addAttribute("title", viewModel.pageTitle())`) →
    `layout.pug:7` (`title= title || "domaincentric.commerce"`).
  - Covered by: none.

- product-page-breadcrumb: Given the seeded product "Clean Architecture", when a visitor opens its
  product page, then the breadcrumb reads "Home / Products / Clean Architecture", and "Home" links
  to the home page and "Products" to the catalogue page.
  → level: integration — `./gradlew test-integration`.
  - Lives in: `detail.pug:4-9` (`data-test="breadcrumb"`; `a(href="/") Home`, separator `/`,
    `a(href="/products") Products`, separator `/`, current `#{productDetail.name()}`).
  - Covered by: none. (`ProductCatalogPageIntegrationTest.pageIsTitledHeadedAndPlacedAsTheCatalogue`
    asserts the catalogue page's breadcrumb "Home / Products", not the product page's.)

- back-to-products-returns-to-the-catalogue: Given a visitor on the product page of "Team
  Topologies", when they follow "Back to Products", then the catalogue page "Our Products" opens.
  → level: integration — `./gradlew test-integration` (the link's target on the product page, and
  the page behind it headed "Our Products").
  - Lives in: `detail.pug:54` (`a.btn.btn--ghost(href="/products" data-test="product-back-link")
    Back to Products`) → `ProductPageController.java:63-74` → `catalog.pug:9` (`h1 Our Products`).
  - Covered by: none. The e2e page object offers `ProductDetailPage.backToCatalog`
    (`ProductDetailPage.java:55-58`), but no test calls it (grep of `src/test-e2e` outside
    `pages/`: no hit).

## Files

- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java` — read: the route `GET /products/{id}` and the title attribute.
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductDetailPageViewModel.java` — read: page title and the description fallback (the fallback is out of scope).
- `src/main/resources/templates/product/detail.pug` — read: heading, image, description, category, breadcrumb, back link.
- `src/main/resources/templates/product/catalog.pug` — read: the card's "View Details" link and the catalogue heading.
- `src/main/resources/templates/layout.pug` — read: where the tab title is rendered.
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/bootstrap/SampleDataInitializer.java` — read: the seeded products the scenarios name.
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductCatalogPageIntegrationTest.java` — read: the MockMvc pattern and the HTML matchers (title, heading, breadcrumb, image) to mirror; its own context per class (lines 24-29).
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductCatalogPage.java`, `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductDetailPage.java` — read: the page objects the happy-path test would drive.
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/SeededCatalogE2ETest.java` — read: the existing e2e test against the seeded catalogue.

## Open assumptions

- The product page's heading is the `h1` in `detail.pug:13`; it carries no `data-test` attribute
  (`project/product.md`, `## Look and feel`: "every element a test addresses carries a stable
  `data-test` attribute"). Likewise the image, description, category and title have none. A test
  that addresses them may need attributes added — a markup-only change for a later stage to
  decide, not planned here because adopt mode changes no production code.
- "Opens the product page of X" is observed as: the URL is `/products/<id>` of that product and the
  page renders `data-test="product-detail"` with the product's name.
- `ProductCatalogPageIntegrationTest` is uncommitted CAT-01 work; this plan relies on it only as
  partial coverage and pattern, not as proof.
- Five scenarios have no test that covers them as stated; in adopt mode that is recorded, not
  fixed here.
