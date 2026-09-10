# Tests — STORY-P1

Stage carrier: the profile names `carrier.test: e2e-testing`, and that skill is available here, so it
was used. Its browser specifics (Page Objects, `data-test` selectors) do not apply — the plan chose
the HTTP level, not a page — but its discipline does: one flow per test, assertions on what the
caller can observe, no assertion against the repository inside the criteria tests, explicit
arrangement instead of reliance on shop sample data.

Knowledge source: the profile names `knowledge: dca-knowledge`. Nothing in this stage turned on a
pattern question the plan had not already settled (the plan cites
`/decision/read-model-vs-domain-query.md` and `/decision/specification-vs-query-method.md`), so no
node was consulted and none is cited here.

<!-- gate:tests -->
| criterion | test |
| --- | --- |
| lists-products-below-the-threshold | dev.domaincentric.sample.ecommerce.inventory.LowStockOverviewIntegrationTest#listsEveryProductWithLessOnHandThanAskedAbout |
| excludes-products-at-or-above-the-threshold | dev.domaincentric.sample.ecommerce.inventory.LowStockOverviewIntegrationTest#leavesOutProductsWithAsMuchOrMoreOnHandThanAskedAbout |
| names-the-available-quantity | dev.domaincentric.sample.ecommerce.inventory.LowStockOverviewIntegrationTest#namesHowMuchOfEachListedProductIsLeft |
| empty-answer-when-nothing-is-low | dev.domaincentric.sample.ecommerce.inventory.LowStockOverviewIntegrationTest#answersWithAnEmptyListWhenNothingIsRunningLow |

## Notes

- All four `LowStockOverviewIntegrationTest` tests currently fail on `status().isOk()` with
  `Status expected:<200> but was:<404>`, because no `GET /api/stock/low` route exists yet: neither
  the resource, nor the input port, nor the use case is written. The status assertion is the first
  one each test reaches; the body assertions behind it are the ones the build stage has to satisfy.
- `InMemoryStockLevelRepositoryTest#findsOnlyStockLevelsStrictlyBelowTheGivenQuantity` fails on
  `expected: <[ProductId[value=…]]> but was: <[]>`, because the new finder is a stub. It pins the
  boundary the HTTP test cannot pin cheaply: a stock level *at* the quantity is not part of the
  answer, one below it is.
- Stubs added, no behaviour: `StockLevelRepository#findWithAvailableQuantityBelow(StockQuantity)`
  (the output-port method the plan names) and its `InMemoryStockLevelRepository` implementation
  returning `List.of()`. Nothing else in `main` was touched — no use case, no resource, no DTO.
- Unit tests for invariants: none written, because the plan introduces no domain change and no new
  invariant (`StockLevel` gets no method, no event, no field). `StockQuantity`'s existing
  "quantity ≥ 0" rule is untouched and no criterion covers the threshold's own validation, so
  testing it here would test code this story does not write.
- Test arrangement is deterministic despite the shop's sample data: each test seeds its own
  products through `SetStockLevelInputPort` with freshly generated `ProductId`s and asserts only
  about those, never about the size of the answer. The empty-answer test asks for products below a
  quantity of `0`, which no stock level can undercut — the only quantity whose answer is empty
  regardless of what the sample data holds.
- The overview is staff-only per the plan, so the tests mint a staff token the way
  `ApiAuthorizationIntegrationTest` does. Authorization itself is arrangement here, not an
  assertion; no criterion covers it and no test was added for it.
- **The gate will not run the four criteria tests.** `.agents/factory/factory.profile.yaml`
  declares `compile`, `test`, `e2eTest`, `architecture`, `format` — not `./gradlew test-integration`,
  which is where this project keeps `@SpringBootTest` + `MockMvc` tests (and where the plan placed
  them, section *Open assumptions*). Only the repository unit test is inside `./gradlew test`.
  Adding an `integrationTest:` entry to the profile is a stack decision, not part of this story, so
  no build or profile file was changed. Until it is added, the build stage has to run
  `./gradlew test-integration --tests "*LowStockOverviewIntegrationTest*"` by hand or the story
  passes the gate on the unit test alone.

## Verification run

- `./gradlew testClasses testIntegrationClasses` — BUILD SUCCESSFUL (both test source sets compile).
- `./gradlew test` — 373 tests, 1 failed: the new repository test, on its assertion. No other test
  changed state.
- `./gradlew test-integration --tests "*LowStockOverviewIntegrationTest*"` — 4 failed, all on
  `status().isOk()`.
- `./gradlew test-architecture` — BUILD SUCCESSFUL.
- `./gradlew spotlessCheck` — BUILD SUCCESSFUL (after `spotlessApply` on the new test file).

## Cut back to the shape the story asked for (2026-09-10)

The run's own shape was revised afterwards, by hand, and this file follows the code:

- The rule is a **Specification** (`AvailableQuantityBelow`) handed to `StockLevelRepository#findBy`,
  not a finder named after the rule — the decision node the plan cited was sharpened after this run
  and now answers that case with both, the way this shop's Cart already does it.
- **No REST resource, no DTO and no staff guard.** No criterion names a route or a page, and giving
  the overview a surface — including who may call it — is a scoping question, not part of this story.
  The criteria are therefore driven through the wired application at the input port, the highest
  level that exists today. `stage-plan` now refuses to invent such a surface at all.
- `StockLevelSpecificationTest` pins the rule's boundary (satisfied below the threshold, not at it,
  reservations ignored) as an **invariant** test, not as a criterion's test — the same place the
  .NET run put it. The criterion `excludes-products-at-or-above-the-threshold` keeps the end-user
  test it was verified red and green with; the former repository-adapter test is gone with the
  finder it tested.
