# product — productdetails

### Rule: Every product has a page of its own, reached from its card

#### view-details-opens-the-product-page
Title: View Details on a catalogue card opens that product's page
- Given the seeded product "Domain-Driven Design"
- When a visitor follows the "View Details" link on its catalogue card
- Then the product page of "Domain-Driven Design" opens
- And its heading reads "Domain-Driven Design"
Test: dev.domaincentric.sample.ecommerce.e2e.ProductPageE2ETest#viewDetailsOpensTheProductsPageHeadedWithItsName

### Rule: The page shows what the product is

#### product-page-shows-image-description-and-category
- Given the seeded product "Domain-Driven Design"
- When a visitor opens its product page
- Then the page shows the product's image, the description "The seminal work by Eric Evans that introduced the software industry to Domain-Driven Design. This essential guide teaches you how to tackle complexity in the heart of software by connecting implementation to an evolving model of the business domain."
- And it shows "Category" with the value "Books"
Test: dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#showsTheProductsImageDescriptionAndCategory

### Rule: The page is recognisable as that product's page

#### product-page-title-is-the-product-name
- Given the seeded product "Clean Architecture"
- When a visitor opens its product page
- Then the browser tab title is "Clean Architecture"
Test: dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#tabIsTitledWithTheProductsName

#### product-page-breadcrumb
- Given the seeded product "Clean Architecture"
- When a visitor opens its product page
- Then the breadcrumb reads "Home / Products / Clean Architecture"
- And "Home" links to the home page and "Products" to the catalogue page
Test: dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#breadcrumbLeadsFromHomeThroughTheCatalogueToTheProduct

### Rule: The visitor can always go back to the catalogue

#### back-to-products-returns-to-the-catalogue
- Given a visitor on the product page of "Team Topologies"
- When they follow "Back to Products"
- Then the catalogue page "Our Products" opens
Test: dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#backToProductsOpensTheCatalogue

### Rule: A product address no product has shows the not-found page

#### unknown-product-shows-the-not-found-page
- Given the seeded sample catalogue, in which no product has the id "no-such-product"
- When a visitor opens the product page `/products/no-such-product`
- Then the page shows "404" and the heading "Page Not Found"
- And the message reads "The page you're looking for doesn't exist or has been removed. Perhaps you were looking for one of our products?"
Test: dev.domaincentric.sample.ecommerce.e2e.ProductNotFoundE2ETest#unknownProductAddressShowsTheNotFoundPage

#### not-found-page-has-the-shop-title
- Given the seeded sample catalogue
- When a visitor opens the product page `/products/no-such-product`
- Then the browser tab title is "domaincentric.commerce"
Test: dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#browserTabOfTheNotFoundPageIsTitledWithTheShopsName

### Rule: The not-found page offers a way on

#### browse-all-products-leads-to-the-catalogue
- Given a visitor on the not-found page of `/products/no-such-product`
- When they follow "Browse All Products"
- Then the catalogue page "Our Products" opens
Test: dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#browseAllProductsOpensTheCatalogue

#### go-to-homepage-leads-to-the-home-page
- Given a visitor on the not-found page of `/products/no-such-product`
- When they follow "Go to Homepage"
- Then the home page "Welcome to domaincentric.commerce" opens
Test: dev.domaincentric.sample.ecommerce.product.ProductNotFoundPageIntegrationTest#goToHomepageOpensTheHomePage
