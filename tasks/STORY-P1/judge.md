# Judge — STORY-P1

Round 2. Round 1 returned `changes-requested` on one `major` and two `minor` findings; this round
re-reviews the whole change, not only the delta, and closes those three.

## Verdict

verdict: pass

The `major` finding of round 1 is fixed and the fix is the one that was prescribed. Of the two
`minor` findings, one is fixed; the other is withdrawn as a mis-call — see *Considered and dropped*.
Nothing new was found from any of the three perspectives, and no finding remains that blocks.

Knowledge source: the profile names `knowledge: dca-knowledge`. No pattern question arose in this
round — the two decision nodes the plan settled on (`/decision/read-model-vs-domain-query.md`,
`/decision/specification-vs-query-method.md`) are untouched by round 2's fixes — so no node is cited
here. The findings and the withdrawal below stand on the project's own artifacts: `AGENTS.md`,
`inventory/domain/glossary.md`, the existing Inventory and Product code, and the architecture suite's
own report.

## Perspectives covered

- domain: `dca-core:review-domain` (the carrier `review.domain: review-domain` resolves to it here);
  run in this session.
- boundaries: `dca-core:review-boundaries` (carrier `review.boundaries: review-boundaries`); run in
  this session.
- craft: `software-craftsmanship:review-craft` (carrier `review.craft: review-craft`); run in this
  session.
- No additional perspectives: the profile declares no `reviews:` key.

Verification run (to check the build stage's claims, not to re-report rule findings):

- `./gradlew test test-integration test-architecture spotlessCheck` — BUILD SUCCESSFUL.
- `./gradlew test test-integration --rerun-tasks` — BUILD SUCCESSFUL. Re-run from scratch rather
  than accepted as `UP-TO-DATE`, because an up-to-date task is the build stage's own result reported
  back, not an independent check.

## Confirmed defects

| Perspective | File:line | Severity | Defect | Fix |
| --- | --- | --- | --- | --- |

None. Round 1's three findings are closed below.

## Round 1 findings, re-checked

- **`major` — the staff guard on `/api/stock/low` was untested. Fixed, verified in the code.**
  `ApiAuthorizationIntegrationTest#readingTheLowStockOverviewNeedsStaff`
  (`src/test-integration/java/dev/domaincentric/sample/ecommerce/api/ApiAuthorizationIntegrationTest.java:87-109`)
  asserts the three cases the finding named — no token → `403`, customer token → `403`, staff token
  → `200` — in the class whose JavaDoc (lines 23-28) declares itself the place the resource guards
  are held to, and in the shape of the sibling `listingEveryCartNeedsStaff` (lines 71-85). The guard
  it pins is the only thing that can produce those refusals: `/api/stock/**` is not listed in
  `account/infrastructure/SecurityConfiguration.java` and falls through to
  `anyRequest().authenticated()`, which the JWT filter satisfies for an anonymous caller too, so the
  `403` can come from nowhere but `LowStockResource.java:48-50`. The build stage additionally reports
  a mutation check (guard removed → the case fails; restored → green); that claim is consistent with
  the code but was not re-run here, since re-running it would mean editing production code.
- **`minor` — the `try` spanned the use-case call. Fixed.** `LowStockResource.java:54-62`: the `try`
  now encloses only `new GetLowStockProductsQuery(threshold)`, and `execute` is called outside it, so
  an `IllegalArgumentException` from the repository, the mapping or `StockQuantity` is no longer
  answered as `400`. The comment on line 58 ("the caller's mistake") is true again, and line 52-53
  states why the scope is narrow.
- **`minor` — handler `getLowStockProducts` has the same name as the input-port field. Withdrawn,
  not deferred.** See *Considered and dropped*.

## Considered and dropped

- **The handler/field name collision in `LowStockResource.java:34,45` — withdrawn as a round 1
  mis-call.** The build stage declined it and gave a reason that checks out:
  `ProductResource.java:38-39` holds `getAllProducts` and `getProductById` fields called from
  `getAllProducts()` (line 86-87) and `getProductById()` (line 95-96) handlers, and `createProduct`
  likewise (lines 38, 58, 77). The Inventory resource therefore follows the project's established
  form exactly, and round 1 applied a stricter standard to it than it applied one row lower, where
  the `LowStockProduct` validation was dropped for being "identical in shape to the sibling". If the
  reading is worth changing it is worth changing for every resource, which is a rename of its own,
  not this story's. Recorded so the withdrawal is visible rather than silent.
- **`availableQuantity` here means on-hand, while `GetStockForProductsResult.StockData#availableStock`
  means on-hand minus reserved** (`getstockforproducts/GetStockForProductsUseCase.java:45-47`). Real
  vocabulary drift inside one context, but not introduced by this change. The new code matches
  `inventory/domain/glossary.md:217-221` ("Available quantity: quantity … physically held in the
  warehouse, regardless of whether parts of it are already reserved") — re-read this round and
  confirmed — and the older field is the one that misnames the glossary's "Unreserved quantity
  (available-to-promise)". Renaming a field another context reads is its own story.
- **Reserved stock is not subtracted for the overview.** The story's own open assumption. The criteria
  say "available quantity" and the glossary defines that term exactly as the code uses it, so the plan
  follows the project's vocabulary rather than inventing one — not a conflict the judge can decide
  differently, and therefore not a `story-conflict`. Practical consequence worth a human's eye, not a
  code defect: a product with 10 on hand and 10 reserved is never reported as low. Belongs in the
  story's answer.
- **Threshold validated twice** (`GetLowStockProductsQuery:12-16` and `StockQuantity`'s "≥ 0"
  invariant). The input model validating its own input at the application boundary is the context's
  convention (`GetStockForProductsQuery`), and the domain invariant is not redundant with it.
- **`GetLowStockProductsResult.LowStockProduct` re-checks `availableQuantity >= 0`** although it is
  built from a `StockQuantity` that cannot be negative. Defensive, but identical in shape to the
  sibling `GetStockForProductsResult.StockData` — preference, not a defect.
- **`LowStockProduct` carries the domain `ProductId`.** Allowed: a result may not carry an aggregate
  or an entity; a shared-kernel value object is what `StockData` carries too.
- **`@Transactional(readOnly = true)` on a use case reading an in-memory repository.** No transaction
  manager governs `ConcurrentHashMap`, but this is the context's own form for a query use case
  (`GetStockForProductsUseCase:22-23`) and survives the persistence swap the in-memory adapter's own
  JavaDoc anticipates.
- **No ordering, no per-product reorder level, no page in `backoffice`.** All unspecified by the
  criteria and recorded as open assumptions in `plan.md:114-125`; inventing them here would widen the
  story.
- **In-memory persistence only.** `InMemoryStockLevelRepository` is the sole `StockLevelRepository`
  implementation, so the new finder needs exactly one. Pre-existing state of Inventory.
- **Domain, boundaries: nothing found, this round either.** No `domain/` file was touched; the port is
  declared in `application/shared/StockLevelRepository.java:51` and implemented in
  `adapter/outgoing/persistence/InMemoryStockLevelRepository.java:56-61`; the finder hangs off the
  repository of the aggregate root whose data it reads and carries domain language; the resource
  depends on `GetLowStockProductsInputPort` and never on the use-case class; the query carries an
  `int` and the conversion to `StockQuantity` happens in the use case, so no domain value object
  reaches the adapter; the DTO mapping is in the adapter (`LowStockResource:67-69`); no cross-context
  import was added. Reported here rather than as invented findings.
- **Anything the rule suite covers.** `./gradlew test-architecture` is green; its findings are the
  gate's business, not this report's.

Not a defect, but the next stage's input: `inventory/domain/glossary.md` does not yet carry the
plan's proposed terms ("Low stock", "Reorder threshold"), and `docs/architecture/context-map.md`
does not record Inventory's new incoming API adapter. Both are `stage-document`'s work, which runs
after this stage — noted so they are not lost, not held against the build.

## Criteria re-checked

- `lists-products-below-the-threshold`: met —
  `LowStockOverviewIntegrationTest#listsEveryProductWithLessOnHandThanAskedAbout:63-71` seeds 1 and 4
  against a threshold of 5 and finds both entries in the HTTP answer, backed by the strict `<` filter
  in `InMemoryStockLevelRepository:57-60`.
- `excludes-products-at-or-above-the-threshold`: met — the boundary value (`5` against threshold `5`)
  and a well-stocked product are both asserted absent at the HTTP level
  (`LowStockOverviewIntegrationTest:75-86`), and
  `InMemoryStockLevelRepositoryTest#findsOnlyStockLevelsStrictlyBelowTheGivenQuantity:27-40` pins the
  same boundary directly.
- `names-the-available-quantity`: met — the answer's `availableQuantity` field is asserted to be the
  exact quantity seeded (`3`), not merely present (`LowStockOverviewIntegrationTest:97-100`).
- `empty-answer-when-nothing-is-low`: met, with a narrow arrangement — the test can only reach the
  empty case with a threshold of `0`, because the shop's sample data is outside its control, so an
  implementation that special-cased `threshold == 0` would also pass it. It does assert what the
  criterion says (`200`, a JSON array, empty) rather than an error, so it is not nominal. Unchanged
  from round 1 and accepted again: the criterion asks for the absence of an error, and that is what
  is asserted.
