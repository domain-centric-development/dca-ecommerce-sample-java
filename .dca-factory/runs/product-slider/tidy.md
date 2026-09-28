# Tidy — product-slider

Carrier: in-session; no `carrier.tidy` is named in the profile. `dca-discipline` (`carrier.guard`) was
not invoked as a skill; its invariants were kept as the stage describes (the one move stays inside the
application layer, no new dependency, no cross-context import).

## Moves
| File | Move | Why it reads better |
| --- | --- | --- |
| src/main/java/dev/domaincentric/sample/ecommerce/product/application/getproductselection/GetProductSelectionUseCase.java | the catalogue's product ids are mapped once into `productIds` and reused for the price lookup and the priced-candidate filter, instead of `products.stream().map(Product::id)` twice | one name for "the ids of the catalogue", and the filter reads as "the ids that have a price" |

## Left alone
- `GetProductSelectionUseCase.article(...)` duplicates the stock/price assembly of `GetAllProductsUseCase`: lifting it into a shared helper would edit a file outside this story's footprint; a finding for the judge, not a tidy-up.
- The price text `amount + " " + currencyCode` in `ProductSliderViewModel.Card` repeats the formatting of `ProductDetailPageViewModel` and `ProductCatalogPageViewModel`: a shared price formatter would touch views outside the footprint.
- `home/index.pug`, `product/detail.pug`, `main.css`: taken over from the .NET sample ("One markup for both shops"); `main.css` is byte-identical to the .NET stylesheet, so nothing was touched.
- `ProductSelection`, `ProductSliderControllerAdvice`, `ProductSliderViewModel`, the query/result records and `ProductDomainConfiguration`: already small and named in the story's vocabulary.
- Tests: unchanged, as the stage requires.

## Checks
- ./gradlew testClasses: green
- ./gradlew test --rerun: green
- ./gradlew test-integration --rerun: green
- ./gradlew test-architecture: green
- ./gradlew spotlessApply, then ./gradlew spotlessCheck: green
