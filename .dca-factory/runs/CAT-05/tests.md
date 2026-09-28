# Tests — CAT-05

Mode: **adopt**. The plan found no existing test for any scenario, so every scenario has a
characterization test, green on today's code, with a break under `breaks/`. No production code and
no existing test changed.

Carrier: `e2e-testing` (the profile's `carrier.test`) is named but was not invoked as a skill; its
craft was applied in-session: page objects, `data-test` selectors, one flow per test, Playwright's
own waits and no sleeps, and the shop started by the suite (`ShopUnderTest`). Knowledge source:
`dca-knowledge` is named in the profile but was not consulted, because characterization takes no
pattern decision.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| logo-leads-home | dev.domaincentric.sample.ecommerce.e2e.SiteHeaderE2ETest#logoOnAProductPageLeadsToTheHomePage |
| nav-home-opens-the-home-page | dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#homeInTheHeaderNavigationOpensTheHomePage |
| nav-products-opens-the-catalogue | dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#productsInTheHeaderNavigationOpensTheCatalogue |
| footer-names-the-shop | dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#footerOfTheCatalogueNamesTheShopAndLinksToTheEventLog |
| header-and-footer-on-the-home-page | dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#homePageShowsTheSameHeaderAndFooterAsTheCatalogue |

## Characterization
- dev.domaincentric.sample.ecommerce.e2e.SiteHeaderE2ETest#logoOnAProductPageLeadsToTheHomePage
- dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#homeInTheHeaderNavigationOpensTheHomePage
- dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#productsInTheHeaderNavigationOpensTheCatalogue
- dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#footerOfTheCatalogueNamesTheShopAndLinksToTheEventLog
- dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#homePageShowsTheSameHeaderAndFooterAsTheCatalogue

## Files
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/SiteHeaderE2ETest.java
- src/test-e2e/java/dev/domaincentric/sample/ecommerce/e2e/pages/SiteHeader.java
- src/test-integration/java/dev/domaincentric/sample/ecommerce/portal/SiteHeaderAndFooterIntegrationTest.java
- tasks/CAT-05/breaks/dev.domaincentric.sample.ecommerce.e2e.SiteHeaderE2ETest--logoOnAProductPageLeadsToTheHomePage.patch
- tasks/CAT-05/breaks/dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest--homeInTheHeaderNavigationOpensTheHomePage.patch
- tasks/CAT-05/breaks/dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest--productsInTheHeaderNavigationOpensTheCatalogue.patch
- tasks/CAT-05/breaks/dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest--footerOfTheCatalogueNamesTheShopAndLinksToTheEventLog.patch
- tasks/CAT-05/breaks/dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest--homePageShowsTheSameHeaderAndFooterAsTheCatalogue.patch

## Notes
- Green on today's code: `./gradlew test-integration --rerun --tests '*SiteHeaderAndFooterIntegrationTest'`
  (4 passed) and `./gradlew test-e2e --rerun --tests '*SiteHeaderE2ETest'` (1 passed).
- Each break changes one line of `src/main/resources/templates/layout.pug`. Each was applied, the test
  was run red, and the change was reverted:
  - logo → `href="/products"`: the e2e test fails because the home page never opens (Playwright
    `TimeoutError` in `HomePage`'s wait for `/`).
  - nav Home → `href="/products"`: expected "Welcome to domaincentric.commerce" but was "Our Products".
  - nav Products → `href="/"`: expected "Our Products" but was "Welcome to domaincentric.commerce".
  - footer text "Built with" → "Made with": the footer text assertion fails.
  - the Event Log link wrapped in `unless title == "domaincentric.commerce"` (hidden on the home page
    only): the footer-link assertion fails on the home page. The other four tests stay green.
    Under each break, the other integration tests in the class stay green.
- The home page is recognised by its hero heading "Welcome to domaincentric.commerce". The catalogue
  is recognised by its heading "Our Products". The logo's name is its visible text, read without
  whitespace ("domaincentric" + ".commerce"). These are the plan's open assumptions.
- "The same header and footer" compares only the parts in scope: the logo (target and name), the header
  navigation markup, the footer text and the Event Log link. The cart and mini-basket, the login and
  register links and the theme switcher are out of scope and not asserted.
- New page object `SiteHeader` for the shared header. It lives beside the existing page objects and
  extends `BasePage`.
- Unit tests: none. The plan names no invariant, and the behaviour lives in a template.
