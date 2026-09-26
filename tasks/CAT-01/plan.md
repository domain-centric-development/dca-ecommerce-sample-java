# Plan — CAT-01: The catalogue lists the range

Mode: **adopt** (`status: adopted`). The behaviour exists; nothing is built. No production code and no
existing test changes. Stage carried in-session; the profile names no `carrier.plan`. The profile's
knowledge skill `dca-knowledge` was not consulted: an adopt plan takes no pattern decision.

## Context

`product` — Product Catalog. The designed map lists it as `product | Product Catalog: master data,
categories, composite-article aggregation | Core` (`project/domain.md:21`). The generated map has it too
(`docs/architecture/context-map.md:27`). The two maps agree about this context. The story only reads the
catalogue's master data (name, description, image, id), so it crosses no relationship. Price and
availability are out of scope.

Surface: the product description lists the catalogue among the server-rendered shopper pages
(`project/product.md`, `## Surfaces`). It already exists as `GET /products` →
`ProductPageController.showProductCatalog`
(`src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java:63-74`),
rendered from `src/main/resources/templates/product/catalog.pug`. The visitor reaches it today, so no
surface is missing.

## Changes

| Element | Kind | Location | New or changed |
| --- | --- | --- | --- |
| — | — | — | none (adopt: the behaviour is in place) |

Where the behaviour lives:

| Element | Kind | Location |
| --- | --- | --- |
| `SampleDataInitializer` | incoming adapter (bootstrap) | `product/adapter/incoming/bootstrap/SampleDataInitializer.java` — 21 `createProduct(` calls in `loadSampleProducts()` |
| `InMemoryProductRepository.findAll` / `BY_NAME` | outgoing adapter (persistence) | `product/adapter/outgoing/persistence/InMemoryProductRepository.java:40-41, 69-71` — `Comparator.comparing(product -> product.name().value())`, i.e. `String.compareTo` (UTF-16 code-unit order) |
| `GetAllProductsUseCase` | use case / input port `GetAllProductsInputPort` | `product/application/getallproducts/GetAllProductsUseCase.java:46-65` — keeps the repository's order |
| `ProductPageController.showProductCatalog` | incoming adapter (web) | `product/adapter/incoming/web/ProductPageController.java:63-74` |
| `ProductCatalogPageViewModel` | view model | `product/adapter/incoming/web/ProductCatalogPageViewModel.java:30-34` — `pageTitle` "Product Catalog" |
| `catalog.pug` | template | `src/main/resources/templates/product/catalog.pug:4-30` |
| `layout.pug` `title` | template | `src/main/resources/templates/layout.pug:7` — `title= title` |

(Java paths are relative to `src/main/java/dev/domaincentric/sample/ecommerce/`.)

## Acceptance criteria

- catalogue-lists-the-seeded-range: Given the shop has started and seeded the sample catalogue, when a
  visitor opens the catalogue page, then it shows 21 product cards.
  → level: e2e (happy path), Playwright (`./gradlew test-e2e`)
  - Lives in: `SampleDataInitializer.loadSampleProducts()` (21 `createProduct(` calls). Each becomes one
    `.product-card(data-test="product-card")` (`catalog.pug:13-14`).
  - Covered by: `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/SeededCatalogE2ETest.java`,
    `SeededCatalogE2ETest.catalogListsTheSeededProductsInNameOrder` (`assertEquals(21, titles.size(), …)`, line 22).
    It counts card titles (`data-test="product-card-title"`), one per card.

- catalogue-is-in-name-order: Given the seeded sample catalogue, when a visitor opens the catalogue page,
  then the card titles are in ordinal order of the product name, and the first card is
  `"Bounded Context" Enamel Pin`.
  → level: integration, `./gradlew test-integration` (the `When` is a page request)
  - Lives in: `InMemoryProductRepository.BY_NAME` / `findAll()` (lines 40-41, 69-71), with the order
    kept by `GetAllProductsUseCase.execute` (line 47 onward). The seed name `"\"Bounded Context\" Enamel Pin"` is at
    `SampleDataInitializer.java:232`. `"` (U+0022) sorts before every letter.
  - Covered by: `SeededCatalogE2ETest.catalogListsTheSeededProductsInNameOrder`, lines 23-24 (e2e level,
    not integration). At unit level, `src/test/java/.../product/adapter/outgoing/persistence/InMemoryProductRepositoryTest.java`,
    `InMemoryProductRepositoryTest.findAllIsOrderedByProductName`, checks the comparator against its own
    fixture, not against the seed. No integration test covers it.

- card-shows-name-description-and-image: Given the seeded product "Domain-Driven Design", when a visitor
  opens the catalogue page, then its card shows the name "Domain-Driven Design", its description and
  its image.
  → level: integration, `./gradlew test-integration`
  - Lives in: seed `SampleDataInitializer.java:46-53` (name, description, `/images/products/ddd-book.webp`).
    Rendered by `catalog.pug:16-17` (`img(src=… alt=…)`), `:21` (title), `:22-23` (description).
    `ProductItemViewModel.fromEnrichedProduct` (`ProductCatalogPageViewModel.java:68-80`) maps the fields.
  - Covered by: none. No test asserts a card's name, description or image.

- card-leads-to-the-product: Given the seeded sample catalogue, when a visitor opens the catalogue page,
  then every card offers a "View Details" link to that product's page.
  → level: integration, `./gradlew test-integration`
  - Lives in: `catalog.pug:30` — `a.product-card__action(href="/products/" + product.productId() data-test="view-product") View Details`.
    The target is `ProductPageController.showProductDetail` (`@GetMapping("/{id}")`, line 86).
  - Covered by: none for every card with its label. `CookiePolicyTestBase.setCookieHeadersOfAPageWithAForm`
    (`src/test-integration/.../account/infrastructure/CookiePolicyTestBase.java:21, 33-34`) only finds
    one `href="/products/<uuid>"`, and only as setup for a cookie assertion.
    `EmbeddedShopE2ETest` (line 87) clicks the first `view-product`.

- catalogue-page-heading: Given the seeded sample catalogue, when a visitor opens the catalogue page,
  then the page title is "Product Catalog", the heading reads "Our Products" and the breadcrumb reads
  "Home / Products".
  → level: integration, `./gradlew test-integration`
  - Lives in: title from `ProductCatalogPageViewModel.fromResult` (line 33, `"Product Catalog"`), passed as
    `title` (`ProductPageController.java:71`) into `layout.pug:7`. Heading at `catalog.pug:9`
    (`h1 Our Products`). Breadcrumb at `catalog.pug:4-7`
    (`data-test="breadcrumb"`: link "Home", separator "/", "Products").
  - Covered by: none.

## Files

- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/bootstrap/SampleDataInitializer.java` — read: the 21 seeded products; "Domain-Driven Design" at 46-53, the pin at 232
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/outgoing/persistence/InMemoryProductRepository.java` — read: `BY_NAME`, `findAll()`
- `src/main/java/dev/domaincentric/sample/ecommerce/product/application/getallproducts/GetAllProductsUseCase.java` — read: the order is kept
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductPageController.java` — read: `GET /products`
- `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/web/ProductCatalogPageViewModel.java` — read: page title, card fields
- `src/main/resources/templates/product/catalog.pug` — read: breadcrumb, heading, cards, `data-test` selectors
- `src/main/resources/templates/layout.pug` — read: `<title>`
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/SeededCatalogE2ETest.java` — read: covers the happy path (and the order, at e2e level)
- `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductCatalogPage.java` — read: page object, `productTitles()`
- `src/test/java/dev/domaincentric/sample/ecommerce/product/adapter/outgoing/persistence/InMemoryProductRepositoryTest.java` — read: unit coverage of the comparator
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/account/infrastructure/CookiePolicyTestBase.java` — read: the only integration test fetching `GET /products` as HTML (`HttpClient` against the running context), a pattern to mirror for page-level integration tests
- `src/test-integration/java/dev/domaincentric/sample/ecommerce/account/infrastructure/CsrfProtectionIntegrationTest.java` — read: `MockMvc` `get("/products")` (lines 54, 91), the other way in at integration level

## Open assumptions

- The story's Given names `seed-catalogue.json` as the source of the sample catalogue. **This repository
  has no such file**: `Grep` for `seed-catalogue` finds only the story itself. The Java sample seeds the 21
  products in code, in `SampleDataInitializer.loadSampleProducts()`. The plan reads the Given as "the
  shared sample catalogue" (the story's note: shared scenario `scenario.catalog.seeded-in-name-order`)
  and treats the file name as the monorepo's shared data, not a file this sample loads. If the story
  means the sample must load that file, the story is wrong for this sample and the backlog should say so.
  That would be a change, not an adoption.
- "Ordinal order" is read as Java's `String.compareTo` (UTF-16 code units), which is what `BY_NAME` uses.
  For the seeded names, all in the BMP, this matches ordinal comparison in the .NET twin.
- The card's image counts as shown when an `img` with the product's `imageUrl` as `src` is rendered.
  The placeholder initial is out of scope.
- Four scenarios are covered only at e2e level or not at all. The test stage adds their integration
  tests; that adds tests and changes none.
