# Tests — CAT-04

Adopt mode (`status: adopted`). The plan found no existing test for any scenario, so each scenario
gets a characterization test. Each one passes on today's code and has a break under
`tasks/CAT-04/breaks/`. This stage changed no production code and no line of an existing test.

Carried in-session with the `e2e-testing` craft (`carrier.test`): a page object, `data-test`
selectors, one flow per test, Playwright's own waits. The profile's knowledge skill (`dca-knowledge`)
was not consulted, because characterizing existing behaviour involves no pattern decision.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| home-page-welcomes-the-visitor | dev.domaincentric.sample.ecommerce.e2e.HomePageE2ETest#homePageWelcomesTheVisitorWithWhatTheShopSells |
| home-page-title | dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#browserTabIsTitledWithTheShopsName |
| browse-products-opens-the-catalogue | dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#browseProductsOpensTheCatalogue |
| view-cart-links-to-the-cart | dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#sectionBelowTheHeadingOffersAViewCartLinkToTheCart |
| why-shop-with-us | dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#whyShopWithUsShowsFourFeatures |
| popular-categories | dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#popularCategoriesAreShownAsTextNotAsLinks |
| shop-now-opens-the-catalogue | dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#shopNowBelowTheCallToShopOpensTheCatalogue |

## Characterization
- dev.domaincentric.sample.ecommerce.e2e.HomePageE2ETest#homePageWelcomesTheVisitorWithWhatTheShopSells
- dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#browserTabIsTitledWithTheShopsName
- dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#browseProductsOpensTheCatalogue
- dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#sectionBelowTheHeadingOffersAViewCartLinkToTheCart
- dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#whyShopWithUsShowsFourFeatures
- dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#popularCategoriesAreShownAsTextNotAsLinks
- dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#shopNowBelowTheCallToShopOpensTheCatalogue

## Files
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/HomePageE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/HomePage.java
- src/test-integration/java/dev/domaincentric/sample/ecommerce/portal/HomePageIntegrationTest.java
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.e2e.HomePageE2ETest--homePageWelcomesTheVisitorWithWhatTheShopSells.patch
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest--browserTabIsTitledWithTheShopsName.patch
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest--browseProductsOpensTheCatalogue.patch
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest--sectionBelowTheHeadingOffersAViewCartLinkToTheCart.patch
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest--whyShopWithUsShowsFourFeatures.patch
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest--popularCategoriesAreShownAsTextNotAsLinks.patch
- tasks/CAT-04/breaks/dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest--shopNowBelowTheCallToShopOpensTheCatalogue.patch

## Notes
- Display names are the scenario keys written as words, because the story gives no `Title:` lines.
- `HomePageIntegrationTest` follows `ProductCatalogPageIntegrationTest`: MockMvc against the wired
  application, reading the rendered HTML by its `data-test` sections and unescaping it with
  `HtmlUtils`. The two link tests follow the link's `href` and check that the target page's `h1` is
  "Our Products". The home page reads no data, so the class uses the shared default context.
  Passes on today's code: `./gradlew test-integration --rerun --tests "*HomePageIntegrationTest*"` →
  6 tests, 0 failures (rerun after the Browse Products label assertion was added; `spotlessApply`
  run, `spotlessCheck` clean).
- `HomePageE2ETest` opens `/` in the browser, using the shop the suite starts itself
  (`ShopUnderTest`). Passes on today's code: `./gradlew test-e2e --rerun --tests "*HomePageE2ETest*"` →
  BUILD SUCCESSFUL. The page object reads `textContent`, not `innerText`. `.hero__subtitle` is shown
  in capitals by CSS `text-transform`, and `innerText` returns the transformed text ("BOOKS,
  MODELLING …"). The scenario asserts the words, not their styling.
- Breaks: each patch is the `git diff` of a real one-line change in the working tree, reverted
  afterwards. All seven were applied together and both classes run: 7 of 7 tests failed, each on its
  own assertion. After the revert the production tree was clean and both classes passed again:
  - welcome (e2e): subtitle changed to "Books and merchandise". Fails with expected the subtitle,
    but was "Books and merchandise".
  - tab title: `HomePageController` sets "Home". Fails with expected "domaincentric.commerce", but
    was "Home".
  - Browse Products: the label changed to "Shop". Fails with expected "Browse Products", but was
    "Shop" (the other 5 tests of the class passed). The judge found that this test picked the link
    only by its `data-test` and never checked the label. The test now asserts the label "Browse
    Products" before it follows the link. It also still follows the `href` to "Our Products": the
    earlier break `href="/product"` failed with status 200 expected, but was 404. The patch now holds
    the label break. This change was checked on its own and reverted, and the class passed again.
  - View Cart: `href="/basket"`. Fails with expected "/cart", but was "/basket".
  - features: "Easy Returns" changed to "Returns". Fails on `containsExactly` of the four features.
  - categories: the "Books" title rendered as a link (`a.highlight-card__title(href="/products")`).
    Fails on `containsExactly` of the five categories, because that card no longer reads as text.
  - Shop Now: `href="/"`. Fails because the target page has no plain `h1` ("… in the rendered page":
    expected true, but was false). The home page's heading holds a `span`, so it is not the
    catalogue heading.
  - Each break changes a line that only its own test asserts. The patches were not checked one at a
    time with `git apply`, because `git apply` and `git checkout` needed approval in this session.
    Each one was captured from the tree with only that change in it.
- No unit tests: the plan names no invariant, and the page is static copy with no domain model.
