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

### Rule: A changed price is the price every page shows from then on

#### a-changed-price-shows-on-the-product-page
- Given a client holding a staff token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":49.99}`
- Then the product page of "Domain-Driven Design" shows "49.99 EUR"
- And its catalogue card shows "49.99 EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.PriceChangeApiE2ETest#aPriceAStaffClientChangedIsThePriceOnTheProductPageAndTheCard

### Rule: Only staff change a price; a stranger is challenged, a customer is forbidden

#### a-stranger-cannot-change-a-price
- Given a client without a token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":49.99}`
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"
Test: dev.domaincentric.sample.ecommerce.pricing.PriceResourceIntegrationTest#aClientWithoutATokenIsChallengedWithoutAskingForAChange, dev.domaincentric.sample.ecommerce.pricing.PriceChangeRefusalIntegrationTest#aStrangersPriceChangeLeavesThePrice

#### a-customer-cannot-change-a-price
- Given a client holding the customer token of Ada, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":49.99}`
- Then the shop answers 403
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"
Test: dev.domaincentric.sample.ecommerce.pricing.PriceResourceIntegrationTest#aClientWithACustomerTokenIsForbiddenWithoutAskingForAChange, dev.domaincentric.sample.ecommerce.pricing.PriceChangeRefusalIntegrationTest#aCustomersPriceChangeLeavesThePrice

### Rule: A new price is greater than zero

#### a-new-price-of-zero-is-refused
- Given a client holding a staff token, and "Domain-Driven Design" costs 54.99 EUR
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":0}`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `price` with the reason
- And the product page of "Domain-Driven Design" still shows "54.99 EUR"
Test: dev.domaincentric.sample.ecommerce.pricing.PriceResourceIntegrationTest#aNewPriceOfZeroIsAnUnacceptableValueNamingThePrice, dev.domaincentric.sample.ecommerce.pricing.PriceChangeRefusalIntegrationTest#aNewPriceOfZeroLeavesThePrice

#### a-new-price-of-one-cent-is-accepted
- Given a client holding a staff token
- When it sends `PUT /api/products/<id of Domain-Driven Design>/price` with `{"price":0.01}`
- Then the product page of "Domain-Driven Design" shows "0.01 EUR"
Test: dev.domaincentric.sample.ecommerce.pricing.UpdatePriceIntegrationTest#aNewPriceOfOneCentIsThePriceTheProductPageShows

### Rule: The price address names a product of the catalogue by its id

#### changing-the-price-of-an-unknown-product-finds-nothing
- Given a client holding a staff token, and no product with the id "00000000-0000-4000-8000-000000000001"
- When it sends `PUT /api/products/00000000-0000-4000-8000-000000000001/price` with `{"price":49.99}`
- Then the shop answers 404
Test: dev.domaincentric.sample.ecommerce.pricing.PriceResourceIntegrationTest#aProductPricingHoldsNoPriceForIsNotFound

#### a-malformed-product-id-in-the-price-address-is-a-bad-request
- Given a client holding a staff token
- When it sends `PUT /api/products/no-such-product/price` with `{"price":49.99}`
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value"
Test: dev.domaincentric.sample.ecommerce.pricing.PriceResourceIntegrationTest#anIdThatIsNoUuidIsAnUnacceptableValueWithoutAskingForAChange
