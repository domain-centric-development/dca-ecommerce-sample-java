# product — assortment

### Rule: Staff create a product; its price is always in euros

#### staff-create-a-product
- Given a client holding a token whose roles include staff
- When it sends `POST /api/products` with `{"sku":"MUG-002","name":"Aggregate Mug","description":"A mug with a consistency boundary.","imageUrl":"","price":14.99,"category":"Desk & Office","stock":12}`
- Then the shop answers 201
- And the body has an `id`, `sku` "MUG-002", `name` "Aggregate Mug", `price` 14.99, `currency` "EUR" and `category` "Desk & Office"
Test: dev.domaincentric.sample.ecommerce.e2e.ProductCreationApiE2ETest#staffCreateAProductAndTheShopAnswersWithItInEuros

#### a-created-product-joins-the-catalogue
- Given staff created "Aggregate Mug" with price 14.99 and stock 12
- When a visitor opens the catalogue page
- Then it shows 22 product cards
- And the card "Aggregate Mug" shows "14.99 EUR" and "In Stock"
Test: dev.domaincentric.sample.ecommerce.product.CreatedProductOnTheCatalogueIntegrationTest#aCreatedProductJoinsTheCatalogueWithItsPriceAndStock

#### description-and-image-may-be-left-out
- Given a client holding a staff token
- When it creates the product "Bounded Context Tote" with SKU "TOTE-001", price 19.99, category "Apparel" and stock 10, sending no `description` and no `imageUrl`
- Then the shop answers 201
- And the body has `description` "" and `imageUrl` ""
Test: dev.domaincentric.sample.ecommerce.product.ProductCreationApiIntegrationTest#aProductCreatedWithoutDescriptionAndImageCarriesEmptyOnes

### Rule: A product without an image shows the first letter of its name instead

#### a-card-without-image-shows-the-initial
- Given staff created "Aggregate Mug" with `imageUrl` ""
- When a visitor opens the catalogue page
- Then the card "Aggregate Mug" shows the letter "A" where the image would be
Test: dev.domaincentric.sample.ecommerce.product.CreatedProductOnTheCatalogueIntegrationTest#aCardWithoutAnImageShowsTheFirstLetterOfTheName

### Rule: A product without a description says so on its page

#### a-product-page-without-description
- Given staff created "Bounded Context Tote" without a description
- When a visitor opens its product page
- Then the description reads "No description available."
Test: dev.domaincentric.sample.ecommerce.product.ProductCreationApiIntegrationTest#aProductPageWithoutDescriptionSaysThatNoneIsAvailable

### Rule: Only staff may create; a stranger is challenged, a customer is forbidden

#### a-stranger-is-challenged
- Given a client without a token
- When it sends `POST /api/products` with a valid product
- Then the shop answers 401 with the header `WWW-Authenticate: Bearer`
- And the body is an `application/problem+json` document with title "Unauthorized"
- And the catalogue still holds 21 products
Test: dev.domaincentric.sample.ecommerce.product.ProductCreationRefusalIntegrationTest#aClientWithoutATokenIsChallengedAndNothingIsCreated

#### a-customer-is-forbidden
- Given a client holding the token of the customer account "ada@example.com"
- When it sends `POST /api/products` with a valid product
- Then the shop answers 403
- And the body is an `application/problem+json` document with title "Forbidden"
Test: dev.domaincentric.sample.ecommerce.product.ProductCreationRefusalIntegrationTest#aCustomersTokenIsForbidden

### Rule: A SKU is unique across the catalogue

#### a-duplicate-sku-is-a-conflict
- Given the seeded product "Domain-Driven Design" with SKU "BOOK-001" and a client holding a staff token
- When it creates a product "Another Book" with SKU "BOOK-001", price 19.99, category "Books" and stock 5
- Then the shop answers 409 with an `application/problem+json` body
- And its title is "Stock keeping unit already in use" and its detail "A product with SKU BOOK-001 already exists."
- And the catalogue still holds 21 products
Test: dev.domaincentric.sample.ecommerce.e2e.DuplicateSkuApiE2ETest#aSecondProductUnderAnSkuInUseIsAConflictAndTheCatalogueStaysAsItWas

### Rule: A SKU holds only upper-case letters, digits and hyphens

#### a-lower-case-sku-is-refused
- Given a client holding a staff token
- When it creates a product "Aggregate Mug" with SKU "mug-002", price 14.99, category "Desk & Office" and stock 12
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.product.ProductValueRefusalIntegrationTest#aLowerCaseSkuIsAnUnacceptableValueWithoutDetail

### Rule: A name is required and at most 255 characters long

#### an-overlong-name-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "LONG-001" and a name of 256 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.product.ProductValueRefusalIntegrationTest#aNameOfMoreThan255CharactersIsAnUnacceptableValueWithoutDetail

#### a-name-of-255-characters-is-accepted
- Given a client holding a staff token
- When it creates a product with SKU "LONG-002" and a name of 255 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 201
Test: dev.domaincentric.sample.ecommerce.product.ProductNameAtItsLimitIntegrationTest#aNameOf255CharactersIsAccepted

#### a-blank-name-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "BLANK-001", name "   ", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `name` with the reason
- And the catalogue still holds 21 products
Test: dev.domaincentric.sample.ecommerce.e2e.RefusedProductValueApiE2ETest#aBlankNameIsAnUnacceptableValueNamingTheNameAndNothingIsCreated

### Rule: A description and an image URL are at most 2000 characters long

#### an-overlong-description-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "DESC-001", name "Long Story", a description of 2001 letters "A", price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.product.ProductValueRefusalIntegrationTest#aDescriptionOfMoreThan2000CharactersIsAnUnacceptableValueWithoutDetail

#### an-overlong-image-url-is-refused
- Given a client holding a staff token
- When it creates a product with SKU "IMG-001", name "Big Picture", an `imageUrl` of 2001 characters, price 9.99, category "Books" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.product.ProductValueRefusalIntegrationTest#anImageUrlOfMoreThan2000CharactersIsAnUnacceptableValueWithoutDetail

### Rule: A price is rounded to two decimals, half up

#### a-price-is-rounded-half-up-to-cents
- Given a client holding a staff token
- When it creates "Rounding Mug" with SKU "ROUND-001", price 14.995, category "Desk & Office" and stock 5
- Then the shop answers 201 with `price` 15.00
- And the catalogue card "Rounding Mug" shows "15.00 EUR"
Test: dev.domaincentric.sample.ecommerce.e2e.PriceRoundingApiE2ETest#aPriceWithAFractionOfACentIsRoundedHalfUpInTheAnswerAndOnTheCard

#### the-answer-carries-the-rounded-price
- Given a client holding a staff token
- When it creates "Rounding Mug" with SKU "ROUND-001", price 14.995, category "Desk & Office" and stock 5
- Then the shop answers 201 with `price` 15.00
Test: dev.domaincentric.sample.ecommerce.e2e.ProductCreationApiE2ETest#theAnswerCarriesThePriceRoundedHalfUpToCents

### Rule: A price is greater than zero

#### a-price-of-zero-is-refused
- Given a client holding a staff token
- When it creates "Free Mug" with SKU "FREE-001", price 0, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `price` with the reason
- And the catalogue still holds 21 products
Test: dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockRefusalIntegrationTest#aPriceOfZeroIsAnUnacceptableValueNamingThePriceAndNothingIsCreated

#### a-price-that-rounds-to-zero-is-refused
- Given a client holding a staff token
- When it creates "Tiny Mug" with SKU "TINY-001", price 0.001, category "Desk & Office" and stock 5
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockRefusalIntegrationTest#aPriceThatRoundsToZeroIsAnUnacceptableValueWithoutDetail

### Rule: A price is at most 999999999999.99

#### the-highest-price-is-accepted
- Given a client holding a staff token
- When it creates "Priceless Model" with SKU "MAX-001", price 999999999999.99, category "Modeling" and stock 1
- Then the shop answers 201
- And the catalogue card "Priceless Model" shows "999999999999.99 EUR"
Test: dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockAtTheirLimitsIntegrationTest#theHighestPriceIsAcceptedAndShownOnTheCard

#### a-price-above-the-highest-is-refused
- Given a client holding a staff token
- When it creates "Too Precious Model" with SKU "MAX-002", price 1000000000000.00, category "Modeling" and stock 1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and it carries no detail
Test: dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockRefusalIntegrationTest#aPriceAboveTheHighestAmountIsAnUnacceptableValueWithoutDetail

### Rule: Initial stock may be zero but not negative

#### an-initial-stock-of-zero-is-accepted
- Given a client holding a staff token
- When it creates "Sold Out Pin" with SKU "PIN-002", price 12.99, category "Stickers & Pins" and stock 0
- Then the shop answers 201
- And the catalogue card "Sold Out Pin" shows "12.99 EUR" and "Out of Stock"
Test: dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockAtTheirLimitsIntegrationTest#anInitialStockOfZeroIsAcceptedAndTheCardShowsOutOfStock

#### a-negative-initial-stock-is-refused
- Given a client holding a staff token
- When it creates "Owed Pin" with SKU "PIN-003", price 12.99, category "Stickers & Pins" and stock -1
- Then the shop answers 400 with an `application/problem+json` body
- And its title is "Unacceptable value" and its `errors` names `stock` with the reason
- And the catalogue still holds 21 products
Test: dev.domaincentric.sample.ecommerce.product.ProductPriceAndStockRefusalIntegrationTest#aNegativeInitialStockIsAnUnacceptableValueNamingTheStockAndNothingIsCreated
