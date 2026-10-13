# portal

### Rule: The header carries the shop's name and leads home

#### logo-leads-home
- Given a visitor on the product page of "Domain-Driven Design"
- When they follow the logo "domaincentric.commerce" in the header
- Then the home page opens
Test: dev.domaincentric.sample.ecommerce.e2e.SiteHeaderE2ETest#logoOnAProductPageLeadsToTheHomePage

### Rule: The header navigation reaches the home page and the catalogue

#### nav-home-opens-the-home-page
- Given a visitor on the catalogue page
- When they follow "Home" in the header navigation
- Then the home page opens
Test: dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#homeInTheHeaderNavigationOpensTheHomePage

#### nav-products-opens-the-catalogue
- Given a visitor on the home page
- When they follow "Products" in the header navigation
- Then the catalogue page "Our Products" opens
Test: dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#productsInTheHeaderNavigationOpensTheCatalogue

### Rule: The footer names the shop and links to the event log

#### footer-names-the-shop
- Given the shop has started
- When a visitor opens the catalogue page
- Then the footer reads "domaincentric.commerce — Built with Domain-Centric Architecture"
- And it offers an "Event Log" link to `/backoffice/events`
Test: dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#footerOfTheCatalogueNamesTheShopAndLinksToTheEventLog

#### header-and-footer-on-the-home-page
- Given the shop has started
- When a visitor opens the home page
- Then it shows the same header, with "Home" and "Products", and the same footer as the catalogue page
Test: dev.domaincentric.sample.ecommerce.portal.SiteHeaderAndFooterIntegrationTest#homePageShowsTheSameHeaderAndFooterAsTheCatalogue

### Rule: The not-found page carries the shop's name as its title

#### not-found-tab-reads-the-shop-name
Title: The not-found page is titled with the shop's name
- Given the seeded sample catalogue, in which no product has the id "no-such-product"
- When a visitor opens the product page `/products/no-such-product`
- Then the browser tab title is "domaincentric.commerce"
Test: dev.domaincentric.sample.ecommerce.e2e.ProductNotFoundE2ETest#notFoundPageIsTitledWithTheShopsName
