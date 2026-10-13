# pricing

### Rule: A product starts with the price it was created with

#### card-shows-the-price
- Given the seeded product "Domain-Driven Design", created with the price 54.99 EUR
- When a visitor opens the catalogue page
- Then its card shows the price "54.99 EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.ProductCatalogE2ETest#cardShowsThePriceTheProductWasCreatedWith

#### product-page-shows-the-retail-price
- Given the seeded product "Domain-Driven Design", created with the price 54.99 EUR
- When a visitor opens its product page
- Then the page shows the price "54.99 EUR" labelled "Retail Price"
Test: dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#showsThePriceTheProductWasCreatedWithLabelledRetailPrice

### Rule: A price is shown as its amount with two decimals, followed by the currency code

#### price-keeps-two-decimals
- Given the seeded product "Hexagon Sticker Sheet", created with the price 9.99 EUR
- When a visitor opens the catalogue page
- Then its card shows the price "9.99 EUR"
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#cardShowsThePriceWithTwoDecimalsAndItsCurrency

### Rule: Every product shows its own price, the same on card and page

#### every-card-shows-its-seeded-price
- Given the seeded sample catalogue of `seed-catalogue.json`
- When a visitor opens the catalogue page
- Then each of the 21 cards shows the price its product was seeded with, for example "Wooden Hexagon Desk Model" "39.99 EUR" and `"Bounded Context" Enamel Pin` "12.99 EUR"
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#everyCardShowsThePriceItsProductWasSeededWith

#### page-and-card-agree
- Given the seeded product "Patterns of Enterprise Application Architecture", whose card shows "59.99 EUR"
- When a visitor opens its product page
- Then the product page shows the same price "59.99 EUR"
Test: dev.domaincentric.sample.ecommerce.product.ProductDetailPageIntegrationTest#showsTheSamePriceAsTheProductsCard
