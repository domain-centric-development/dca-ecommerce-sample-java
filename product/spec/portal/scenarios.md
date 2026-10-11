# portal

### Rule: The home page says what the shop is

#### home-page-welcomes-the-visitor
- Given the shop has started
- When a visitor opens the home page `/`
- Then the heading reads "Welcome to domaincentric.commerce"
- And the subtitle reads "Books, modelling supplies and hexagon merchandise for people who draw boundaries"
- And below it reads "Everything this architecture is made of: the books it grew out of, the supplies a design workshop runs on, and hexagons for your desk."
Test: dev.domaincentric.sample.ecommerce.e2e.HomePageE2ETest#homePageWelcomesTheVisitorWithWhatTheShopSells

#### home-page-title
- Given the shop has started
- When a visitor opens the home page
- Then the browser tab title is "domaincentric.commerce"
Test: dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#browserTabIsTitledWithTheShopsName

### Rule: The home page leads to the products and the cart

#### browse-products-opens-the-catalogue
- Given a visitor on the home page
- When they follow "Browse Products"
- Then the catalogue page "Our Products" opens
Test: dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#browseProductsOpensTheCatalogue

#### view-cart-links-to-the-cart
- Given the shop has started
- When a visitor opens the home page
- Then the section below the heading offers a "View Cart" link to the cart page `/cart`
Test: dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#sectionBelowTheHeadingOffersAViewCartLinkToTheCart

### Rule: The home page explains why to shop here

#### why-shop-with-us
- Given the shop has started
- When a visitor opens the home page
- Then a section "Why Shop With Us" shows four features:
  "Free Shipping" — "Books ship cushioned, posters rolled in a tube, hexagons in a fitted box. Free over €50.";
  "Secure Payments" — "Card or invoice, encrypted end to end. Your payment details never touch our order history.";
  "Built to Last" — "Beech and oiled oak, hard enamel, heavyweight cotton. Objects that survive a decade of workshops.";
  "Easy Returns" — "Not the hexagon you pictured? Send any item back within 30 days for a full refund."
Test: dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#whyShopWithUsShowsFourFeatures

#### popular-categories
- Given the shop has started
- When a visitor opens the home page
- Then a section "Popular Categories" shows, as text and not as links:
  "Books" — "The works this architecture was synthesized from — Evans, Vernon, Cockburn, Fowler, Martin.";
  "Modeling" — "Sticky notes, hexagon magnets, posters and card decks for your next design workshop.";
  "Apparel" — "Shirts, hoodies and caps that explain your architecture before you open your laptop.";
  "Desk & Office" — "Mugs, coasters, a hex-grid notebook, and the wooden hexagon for your desk.";
  "Stickers & Pins" — "Small enough for a laptop lid, loud enough for a conference hallway."
Test: dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#popularCategoriesAreShownAsTextNotAsLinks

### Rule: The page closes with a call to shop

#### shop-now-opens-the-catalogue
- Given a visitor on the home page, below "Ready to Draw Some Boundaries?" and "Browse the full catalog: the books, the modelling supplies, and the hexagons."
- When they follow "Shop Now"
- Then the catalogue page "Our Products" opens
Test: dev.domaincentric.sample.ecommerce.portal.HomePageIntegrationTest#shopNowBelowTheCallToShopOpensTheCatalogue

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
