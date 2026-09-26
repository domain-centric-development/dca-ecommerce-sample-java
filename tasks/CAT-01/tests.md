# Tests — CAT-01

Mode: **adopt**. No production code and no existing test changed. Stage carried in-session; the profile's
`carrier.test` (`e2e-testing`) was not needed, because the only e2e criterion maps to an existing test and no
browser test was written. The knowledge skill `dca-knowledge` was not consulted: no test decided a pattern.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| catalogue-lists-the-seeded-range | dev.domaincentric.sample.ecommerce.e2e.SeededCatalogE2ETest#catalogListsTheSeededProductsInNameOrder |
| catalogue-is-in-name-order | dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#listsTheCardTitlesInOrdinalNameOrder |
| card-shows-name-description-and-image | dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#cardShowsTheProductsNameDescriptionAndImage |
| card-leads-to-the-product | dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#everyCardOffersAViewDetailsLinkToThatProductsPage |
| catalogue-page-heading | dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#pageIsTitledHeadedAndPlacedAsTheCatalogue |

## Characterization
- ProductCatalogPageIntegrationTest#listsTheCardTitlesInOrdinalNameOrder
- ProductCatalogPageIntegrationTest#cardShowsTheProductsNameDescriptionAndImage
- ProductCatalogPageIntegrationTest#everyCardOffersAViewDetailsLinkToThatProductsPage
- ProductCatalogPageIntegrationTest#pageIsTitledHeadedAndPlacedAsTheCatalogue

## Files
- src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductCatalogPageIntegrationTest.java
- tasks/CAT-01/breaks/ProductCatalogPageIntegrationTest--listsTheCardTitlesInOrdinalNameOrder.patch
- tasks/CAT-01/breaks/ProductCatalogPageIntegrationTest--cardShowsTheProductsNameDescriptionAndImage.patch
- tasks/CAT-01/breaks/ProductCatalogPageIntegrationTest--everyCardOffersAViewDetailsLinkToThatProductsPage.patch
- tasks/CAT-01/breaks/ProductCatalogPageIntegrationTest--pageIsTitledHeadedAndPlacedAsTheCatalogue.patch

## Notes
- The integration tests request `GET /products` through `MockMvc` in the wired application, following
  `CsrfProtectionIntegrationTest`. They read the rendered HTML by its `data-test` selectors and unescape it
  with `HtmlUtils`, because `"` and `&` in the seeded names come out escaped. The class uses its own
  datasource URL, so it gets its own Spring context: a test class that adds a product in a shared context
  would change the seeded range.
- All four are green on today's code (`./gradlew test-integration --rerun --tests "*ProductCatalogPageIntegrationTest*"`: 4 tests, 0 failures).
  Checked again in round 2 on the current working tree: 4 tests, 0 failures.
- Breaks, each checked by making the change in the working tree, running the class, then reverting it.
  All four were applied together; each touches a separate line and each test failed on its own assertion.
  Round 2 repeated this check: 4 tests completed, 4 failed, each on the assertion below. After the revert,
  `git status --short src/main` was empty and the class was green again:
  - `listsTheCardTitlesInOrdinalNameOrder`: `BY_NAME` reversed in `InMemoryProductRepository`. Fails on
    `isEqualTo(sorted titles)`.
  - `cardShowsTheProductsNameDescriptionAndImage`: description text dropped from `catalog.pug`. Fails on
    the description: expected the seeded text, but it was empty.
  - `everyCardOffersAViewDetailsLinkToThatProductsPage`: label "View Details" → "View" in `catalog.pug`.
    Fails on `label on <title>`.
  - `pageIsTitledHeadedAndPlacedAsTheCatalogue`: `h1 Our Products` → `h1 Products`. Fails with
    expected "Our Products" but was "Products".
- The patch files were written by hand, not produced by `git diff`: this session was not allowed to run
  `git diff`/`git apply` on production files. Their blank context lines are stored as empty lines (the
  editor strips the single space). `git apply` accepts empty lines as blank context, but `git apply
  --check` was not run on them. In round 2, `git apply --check`, `patch --dry-run` and `python3` still needed
  approval, so each hunk's header and old side were compared by hand against the current
  `InMemoryProductRepository.java` (lines 38-42) and `catalog.pug` (lines 6-12, 20-25, 27-33): every
  hunk matches line for line.
- The happy path maps to the existing `SeededCatalogE2ETest` (21 cards). The test is unchanged. The first
  gate run failed because `test-e2e` started no shop and a foreign service answered on `localhost:8080`.
  Decision `CAT-01-01` was answered: the e2e suite now starts the shop itself, in the test process, on a
  free port (`ShopUnderTest`). The test is green as it is:
  `./gradlew test-e2e --rerun --tests "*SeededCatalogE2ETest*"` → BUILD SUCCESSFUL.
- The link test also opens each card's target page and checks that the page shows the card's product
  name. The product page itself is `CAT-02`; this test only checks that the link leads to the right product.
- No unit tests: the plan names no new invariant. `InMemoryProductRepositoryTest#findAllIsOrderedByProductName`
  already covers the comparator.
