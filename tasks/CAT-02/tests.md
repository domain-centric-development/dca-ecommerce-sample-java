# Tests — CAT-02

Adopt mode (`status: adopted`): the plan found no existing test that covers any scenario as stated,
so every scenario gets a characterization test, green on today's code, with a break under
`tasks/CAT-02/breaks/`. No production code and no existing test line was changed.

Carried in-session with the `e2e-testing` craft (`carrier.test`): page objects, `data-test`
selectors, one flow per test, Playwright's own waits. The profile's knowledge skill
(`dca-knowledge`) was not consulted: characterizing existing behaviour takes no pattern decision.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| view-details-opens-the-product-page | dev.domaincentric.sample.ecommerce.e2e.ProductPageE2ETest#viewDetailsOpensTheProductsPageHeadedWithItsName |
| product-page-shows-image-description-and-category | dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#showsTheProductsImageDescriptionAndCategory |
| product-page-title-is-the-product-name | dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#tabIsTitledWithTheProductsName |
| product-page-breadcrumb | dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#breadcrumbLeadsFromHomeThroughTheCatalogueToTheProduct |
| back-to-products-returns-to-the-catalogue | dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#backToProductsOpensTheCatalogue |

## Characterization
- dev.domaincentric.sample.ecommerce.e2e.ProductPageE2ETest#viewDetailsOpensTheProductsPageHeadedWithItsName
- dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#showsTheProductsImageDescriptionAndCategory
- dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#tabIsTitledWithTheProductsName
- dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#breadcrumbLeadsFromHomeThroughTheCatalogueToTheProduct
- dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#backToProductsOpensTheCatalogue

## Files
- src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductDetailPageIntegrationTest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProductPageE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductCatalogPage.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/ProductDetailPage.java
- tasks/CAT-02/breaks/dev.domaincentric.sample.ecommerce.e2e.ProductPageE2ETest--viewDetailsOpensTheProductsPageHeadedWithItsName.patch
- tasks/CAT-02/breaks/dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest--showsTheProductsImageDescriptionAndCategory.patch
- tasks/CAT-02/breaks/dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest--tabIsTitledWithTheProductsName.patch
- tasks/CAT-02/breaks/dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest--breadcrumbLeadsFromHomeThroughTheCatalogueToTheProduct.patch
- tasks/CAT-02/breaks/dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest--backToProductsOpensTheCatalogue.patch

## Notes
- `ProductDetailPageIntegrationTest`: all four tests green on today's code
  (`./gradlew test-integration --rerun --tests '…ProductDetailPageIntegrationTest'`: 4 tests,
  0 failures). It reaches each product page as the scenarios do — through the "View Details" link
  of the product's card on `/products` — and mirrors `ProductCatalogPageIntegrationTest`: MockMvc
  against the wired application, its own H2 database (`product_page_test`) so the seeded products
  stay as seeded.
- `ProductPageE2ETest`: green on today's code (`./gradlew test-e2e --rerun --tests
  '…ProductPageE2ETest'`: 1 test, 0 failures, 2026-09-26). Since commit 33236c6 the suite starts
  this checkout's shop itself, in the test process on a free port (`ShopUnderTest`), so the earlier
  refusal (a foreign shop on :8080 without product cards) no longer applies. No test line changed.
- Page objects: only additions, no existing line changed — `ProductCatalogPage#viewProductNamed`
  (the card whose title is the given name) and `ProductDetailPage#heading` (the `h1` inside
  `data-test="product-detail"`; the heading has no `data-test` of its own — see the plan's open
  assumptions).
- Breaks, one minimal production change each, each touching only what its own test asserts:
  - heading: `detail.pug:13` renders "Product" instead of the name (e2e only; the breadcrumb still
    carries the name, so `ProductCatalogPageIntegrationTest` stays green)
  - image/description/category: `detail.pug:37` labels the category "Genre"
  - tab title: `ProductDetailPageViewModel.java:66` passes "Product" as the page title
  - breadcrumb: `detail.pug:7` labels the catalogue link "Catalogue"
  - back link: `detail.pug:54` points "Back to Products" at `/`
  Verified on 2026-09-26: each change was made in the working tree (the same diff as its patch),
  `ProductDetailPageIntegrationTest` and `ProductPageE2ETest` were run, and the change was
  reverted. Each break turns exactly its own test red, on its assertion, and leaves the other four
  green:
  - heading → `viewDetailsOpensTheProductsPageHeadedWithItsName`: expected "Domain-Driven Design"
    but was "Product"
  - category → `showsTheProductsImageDescriptionAndCategory`: no meta item labelled Category
  - breadcrumb → `breadcrumbLeadsFromHomeThroughTheCatalogueToTheProduct`: expected
    "Home / Products / Clean Architecture"
  - back link → `backToProductsOpensTheCatalogue`: the page behind the link has no "Our Products"
    heading
  - tab title → `tabIsTitledWithTheProductsName`: expected "Clean Architecture" but was "Product"
  After the reverts `git status src/main` is clean and all five tests are green again;
  `./gradlew spotlessApply` changed no file.
- `tests-kept` on `src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/BaseE2ETest.java`
  (`.gate-test.txt:20`): this stage left the file alone — the working tree equals `HEAD`. Commit
  `33236c6` changed it after the baseline was recorded, to carry out CAT-01-01. Decision CAT-02-01
  is answered (a: accepted), and gate 0.39.4 compares a test committed since the baseline against
  `HEAD`. No test line was changed for it.
- unit tests: none — the plan names no invariant; the story changes no domain type.
- uncovered: none.
