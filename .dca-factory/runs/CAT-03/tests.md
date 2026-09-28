# Tests — CAT-03

Adopt mode (`status: adopted`). The plan found no existing test for any scenario, so each scenario
gets a characterization test. Each one passes on today's code and has a break under
`tasks/CAT-03/breaks/`. This stage changed no production code and no line of an existing test.

Carried in-session, following the `e2e-testing` craft (`carrier.test`) through the project's own
page-object pattern (`BasePage`, one flow per test, Playwright's own waits). The skill itself was
not invoked. The profile's knowledge skill (`dca-knowledge`) was not consulted, because
characterizing existing behaviour involves no pattern decision.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| unknown-product-shows-the-not-found-page | dev.domaincentric.sample.ecommerce.e2e.ProductNotFoundE2ETest#unknownProductAddressShowsTheNotFoundPage |
| not-found-page-has-the-shop-title | dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#browserTabOfTheNotFoundPageIsTitledWithTheShopsName |
| browse-all-products-leads-to-the-catalogue | dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#browseAllProductsOpensTheCatalogue |
| go-to-homepage-leads-to-the-home-page | dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#goToHomepageOpensTheHomePage |

## Characterization
- dev.domaincentric.sample.ecommerce.e2e.ProductNotFoundE2ETest#unknownProductAddressShowsTheNotFoundPage
- dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#browserTabOfTheNotFoundPageIsTitledWithTheShopsName
- dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#browseAllProductsOpensTheCatalogue
- dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#goToHomepageOpensTheHomePage

## Files
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/ProductNotFoundE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/NotFoundPage.java
- src/test-integration/java/dev/domaincentric/sample/ecommerce/product/ProductNotFoundPageIntegrationTest.java
- tasks/CAT-03/breaks/dev.domaincentric.sample.ecommerce.e2e.ProductNotFoundE2ETest--unknownProductAddressShowsTheNotFoundPage.patch
- tasks/CAT-03/breaks/dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest--browserTabOfTheNotFoundPageIsTitledWithTheShopsName.patch
- tasks/CAT-03/breaks/dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest--browseAllProductsOpensTheCatalogue.patch
- tasks/CAT-03/breaks/dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest--goToHomepageOpensTheHomePage.patch

## Notes
- Display names are the scenario keys written as words, because the story gives no `Title:` lines.
  The browser test's display name "Unknown product shows the not-found page" must also be a title
  in the shared `scenarios.md` for `SharedScenariosTest` (run only with `-Pspecification.path`).
  That catalogue lives outside this repository and was not checked.
- `ProductNotFoundE2ETest` opens `/products/no-such-product` in the browser, using the shop the suite
  starts itself (`ShopUnderTest`). The `NotFoundPage` page object waits for `error-browse-link`. It
  reads the code, heading and message by their classes (`.error-page__code`, `__title`,
  `__message`), because only the two links carry a `data-test` attribute (plan, open assumptions).
  Adopt mode adds no attributes. The message's source line break is collapsed to one space. Passes
  on today's code: `./gradlew test-e2e --rerun --tests "*ProductNotFoundE2ETest*"` → 1 test,
  0 failures.
- `ProductNotFoundPageIntegrationTest` follows `HomePageIntegrationTest`: MockMvc against the wired
  application with the shared default context and the seeded catalogue. The not-found page is
  requested without a status assertion, because no scenario names one (the page renders with 200
  today, plan, open assumptions). Each link test asserts the label, then follows the `href` and
  asserts the target's visible `h1`, with markup inside it dropped. That is needed for the home
  heading, whose ".commerce" sits in a `span`. Passes on today's code:
  `./gradlew test-integration --rerun --tests "*ProductNotFoundPageIntegrationTest*"` → 3 tests,
  0 failures.
- Breaks: each patch is the `git diff` of a real one-line change in the working tree, reverted
  afterwards. All four were applied together and both classes run: 4 of 4 tests failed, each on its
  own assertion:
  - message (e2e): "doesn't exist or has been removed." changed to "doesn't exist.". Fails with
    expected the full message, but was the shortened one.
  - tab title: the not-found branch of `ProductPageController` sets `title` "Not Found". Fails with
    expected "domaincentric.commerce", but was "Not Found".
  - Browse All Products: label changed to "Browse Products". Fails with expected "Browse All
    Products", but was "Browse Products".
  - Go to Homepage: label changed to "Go Home". Fails with expected "Go to Homepage", but was
    "Go Home".
  - After the revert, `git diff -- src/main` was empty and both classes passed again. The patches
    were not checked one at a time with `git apply`. Each one was captured from the tree with only
    that change in it, and each changes a line that only its own test asserts.
- `spotlessApply` run; `spotlessCheck` and `testClasses` pass.
- No unit tests: the plan names no invariant, and the behaviour is a view choice in the web adapter.
