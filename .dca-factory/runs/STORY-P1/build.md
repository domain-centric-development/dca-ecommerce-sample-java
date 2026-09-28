# Build — STORY-P1

Round 2 — a repeat round. The judge returned `changes-requested` (`tasks/STORY-P1/judge.md`), so this
round works only on the three defects that report confirmed and changes nothing else. The first
round's change set is unchanged and is repeated below under *Changed (round 1, unchanged)* so this
file stays the whole record of the build stage.

Stage carrier: the profile names `carrier.build: ddd-modelling`, and that skill is available here, so
it was loaded and followed. As in round 1 its scope barely touches this story: no domain type is
introduced or edited, and this round's changes are an adapter's control flow and one authorization
test. What it decided in round 1 still holds: the repository finder stays on `StockLevelRepository`
(one repository per aggregate root, domain-language finder name), the answer is mapped in the
application layer rather than handing `StockLevel` outwards, and nothing framework-shaped entered
`domain/`.

Knowledge source: the profile names `knowledge: dca-knowledge`. No pattern question came up in this
round — the two decision nodes the plan settled on
(`/decision/read-model-vs-domain-query.md`, `/decision/specification-vs-query-method.md`) are
untouched by these fixes — so no node was consulted and none is cited.

## Changed

### Round 2 — the judge's findings

| File | Why |
| --- | --- |
| `src/test-integration/java/dev/domaincentric/sample/ecommerce/api/ApiAuthorizationIntegrationTest.java` | new test `readingTheLowStockOverviewNeedsStaff` — pins the staff guard on `GET /api/stock/low` in the class that carries this project's authorization contract (judge finding 1, `major`): no token → `403`, customer token → `403`, staff token → `200` |
| `inventory/adapter/incoming/api/LowStockResource.java` | the `try` now spans only `new GetLowStockProductsQuery(threshold)`; `execute` is called outside it, so an `IllegalArgumentException` from the repository, the mapping or `StockQuantity` is no longer answered as `400` (judge finding 2, `minor`) |

### Round 1 — unchanged

| File | Why |
| --- | --- |
| `inventory/application/getlowstockproducts/GetLowStockProductsQuery.java` | new — the input model; carries the threshold as an `int` and rejects a negative one |
| `inventory/application/getlowstockproducts/GetLowStockProductsResult.java` | new — the output model with the nested `LowStockProduct` (`productId`, `availableQuantity`) |
| `inventory/application/getlowstockproducts/GetLowStockProductsInputPort.java` | new — the input port the adapter depends on, `UseCase<Query, Result>` |
| `inventory/application/getlowstockproducts/GetLowStockProductsUseCase.java` | new — `@Service`, `@Transactional(readOnly = true)`; converts the threshold to `StockQuantity`, asks the repository, maps the answer |
| `inventory/application/shared/StockLevelRepository.java` | changed — the output-port method `findWithAvailableQuantityBelow(StockQuantity)` (declared as a stub by the test stage, unchanged here) |
| `inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java` | changed — the finder filters the stored stock levels on `availableQuantity < quantity` instead of returning `List.of()` |
| `inventory/adapter/incoming/api/LowStockResource.java` | new — `GET /api/stock/low?threshold=`, staff-guarded in the adapter, depends on the input port |
| `inventory/adapter/incoming/api/LowStockProductDto.java` | new — the two-field response DTO (`productId` as `String`, `availableQuantity`) |

No change to `domain/`, to `inventory/api/InventoryService.java` (the Open Host Service), to
`account/infrastructure/SecurityConfiguration.java` (`/api/stock/**` falls through to
`anyRequest().authenticated()`, which the JWT filter always satisfies — the resource is the guard),
and no change to any criterion test: the five tests the test stage mapped are untouched.

## Criteria

- `lists-products-below-the-threshold`: met by `InMemoryStockLevelRepository#findWithAvailableQuantityBelow`
  returning every stored stock level whose `availableQuantity` is lower than the asked quantity, which
  the use case maps one-to-one into the answer and the resource renders as a JSON array.
- `excludes-products-at-or-above-the-threshold`: met by the finder's strict `<` comparison — a stock
  level at exactly the quantity, and any above it, is filtered out. Pinned both at the HTTP level and
  by the repository unit test.
- `names-the-available-quantity`: met by `LowStockProduct#availableQuantity`, taken from
  `StockLevel#availableQuantity().value()` and rendered as the `availableQuantity` field of each entry.
- `empty-answer-when-nothing-is-low`: met by the finder returning an empty list and the resource
  answering `200` with `[]` — no branch turns "nothing found" into an error or a `404`.

Round 2 changed no criterion's answer: the narrowed `try` only affects a path no criterion covers
(a server-side `IllegalArgumentException`), and the new test asserts authorization, not a criterion.

## Judge findings

- **`major` — the staff guard on `/api/stock/low` was untested.** Fixed. The new case was verified to
  bite: with the three guard lines removed from `LowStockResource`, `./gradlew test-integration
  --tests "*ApiAuthorizationIntegrationTest*"` reported
  `ApiAuthorizationIntegrationTest > Reading the low stock overview needs the staff role FAILED`
  (4 tests, 1 failed); with them restored the class is green. Writing a test in the build stage is
  test-stage-shaped work, done here because the judge confirmed the defect and named the fix; no
  existing test was changed or weakened.
- **`minor` — the `try` spanned the use-case call.** Fixed as the judge prescribed: the query is
  built inside the `try`, `execute` is called outside it. The stale comment the finding names is now
  true, and a second comment says why the narrow scope is the point.
- **`minor` — handler `getLowStockProducts` has the same name as the input-port field.** *Not
  changed*, deliberately. The shape the finding objects to is this project's own established form:
  `ProductResource` has a `getAllProducts` field (`ProductResource.java:39`) called from a
  `getAllProducts()` handler (`ProductResource.java:86-87`), and the same holds for `createProduct` and
  `getProductById`. Renaming only the Inventory resource would make it the one adapter that reads
  differently, trading a local readability nit for an inconsistency across the codebase. If the
  reading is right, it is right for every resource — that is a rename of its own, not this story's.
  Reported here rather than silently dropped.

## Deviations from the plan

Round 1's deviations, all still standing:

- **The resource translates a negative threshold into `400`.** The plan left the threshold's own
  validation to `StockQuantity`'s "quantity ≥ 0" invariant "surfacing as a client error", but the
  project has no `@RestControllerAdvice` for `/api/**`, so an uncaught `IllegalArgumentException`
  would surface as `500` — not a client error. `LowStockResource` therefore catches it and answers
  `400`. Round 2 narrowed *where* it catches (see above) but kept the behaviour. No criterion covers
  this and no test asserts it.
- **The query carries an `int`, not a `StockQuantity`.** The plan named the element but not its
  shape. `int` follows the context's own convention for outer-facing input models
  (`SetStockLevelCommand(String productId, int quantity)`), keeps the domain value object out of the
  adapter, and puts the conversion in the use case. The result model does carry the domain
  `ProductId`, matching `GetStockForProductsResult.StockData`.
- **No DTO converter class.** The plan named `LowStockProductDto` and no converter; a two-field
  mapping is done in the resource, as `AuthResource` does. `ProductDtoConverter`-style extraction
  would add a class the story does not need.
- **`.agents/factory/factory.profile.yaml` gained a `test.integration:` entry** during round 1's run,
  but not from that stage — no file outside the change tables was written by the build stage in
  either round. The plan and the test stage both flagged the missing entry as the reason the gate
  would not run the four criteria tests; with the entry present the gate now covers them.

New in round 2:

- **The build stage wrote a test.** `ApiAuthorizationIntegrationTest` is not in the plan's change
  list and is not one of the story's mapped criterion tests. It exists because the judge's `major`
  finding is a test-coverage defect and prescribed exactly this case. Per the stage's own rule that
  an element the plan did not name is a sign the plan was wrong: the plan chose a staff-guarded
  `/api/**` route (`plan.md:34-43`) without naming the project's authorization test as an affected
  element — that is the gap, and it should be named in the plan for any future story that adds a
  guarded route.

## Checks

- `./gradlew testClasses testIntegrationClasses`: BUILD SUCCESSFUL.
- `./gradlew test`: BUILD SUCCESSFUL.
- `./gradlew test-integration`: BUILD SUCCESSFUL — the four criteria tests and the new authorization
  case green, no other integration test changed state.
- `./gradlew test-architecture`: BUILD SUCCESSFUL.
- `./gradlew spotlessCheck`: BUILD SUCCESSFUL (after `spotlessApply` on the edited test file).
- `./gradlew test-e2e`: BUILD SUCCESSFUL, with `./gradlew bootRun` up. The task drives a browser
  against an already-running application and boots none itself
  (`gradle/plugins/test-e2e.gradle:50,71`), so without the app every test fails on
  `net::ERR_CONNECTION_REFUSED at http://localhost:8080/…`; that failure mode is the missing app, not
  this change.
- Mutation check on the new authorization case (guard removed, run, restored): FAILED as intended,
  then green again. Recorded above under *Judge findings*.
