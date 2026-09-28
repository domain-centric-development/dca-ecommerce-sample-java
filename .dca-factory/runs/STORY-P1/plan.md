# Plan — STORY-P1: Low stock overview

Stage carrier: the stack profile (`.agents/factory/factory.profile.yaml`) names no `carrier.plan`, so
this stage ran in-session as described by the `stage-plan` skill.

Knowledge source: the profile names `knowledge: dca-knowledge` (line 14), so that skill was used for
the pattern questions below. It resolved the bundle at `../dca-knowledge-catalog/bundle/` (a sibling
checkout, the live regenerable copy; the plugin also vendors a snapshot). Both decision nodes cited
below carry `review: draft`, i.e. they are proposals, not normative — the project's own rules,
markers and existing Inventory code are what the plan is anchored to.

## Context

**Inventory.** The story's `context: Inventory` is in the project's context map
(`docs/context-map.md:13` — `inventory`, "Stock management and stock reduction", Supporting), so no
new context and no new context relationship is needed. Inventory owns the available quantity: it is
a field of the `StockLevel` aggregate root
(`inventory/domain/model/StockLevel.java:42` — `private StockQuantity availableQuantity`) and the
concept is already in the context's glossary
(`inventory/domain/glossary.md:217-224` — "Available quantity").

The story adds a **read** over many `StockLevel` aggregates. Per `dca-knowledge`
(`/decision/read-model-vs-domain-query.md`, discriminator 1 and the stated default: "the plain query
use case is the default — start there for every read"), and because the aggregate already holds the
data and the read tolerates no staleness requirement of its own, this is a **plain query use case
over the existing repository**, not a read model or a projection. The threshold is a caller input
rather than a named, composed domain rule, so the selection is expressed as a **repository query
method** and not as a `Specification` (`/decision/specification-vs-query-method.md`, discriminators
1–3; that node also holds a Repository method to returning aggregate roots, which the method below
does).

Operator exposure: Inventory has no incoming web or API adapter today — its only incoming adapters
are the two event consumers (`inventory/adapter/incoming/event/`). The project's established
operator-facing exposure for a staff-only read is a REST resource under `/api/**` guarded in the
adapter by `IdentityProvider.Identity#hasRole(ROLE_STAFF)` — the pattern of
`product/adapter/incoming/api/ProductResource.java:61`, and what `AGENTS.md` ("API and
Authorization") prescribes for a claims-only check. `/api/stock/**` needs **no** change to
`account/infrastructure/SecurityConfiguration.java`: it falls through to
`anyRequest().authenticated()` (line 118), which every request satisfies because the JWT filter
always supplies an authentication; the resource is the guard. The alternative — a page in the
`backoffice` context — would require a new context relationship (`backoffice` → `inventory`, absent
from `docs/context-map.md`), which is a scoping decision no criterion asks for; see
*Open assumptions*.

## Changes

| Element | Kind | Location | New or changed |
| --- | --- | --- | --- |
| `GetLowStockProductsQuery` | Query (input model) | `inventory/application/getlowstockproducts/` | new |
| `GetLowStockProductsResult` (+ nested `LowStockProduct` record: `productId`, `availableQuantity`) | Result (output model) | `inventory/application/getlowstockproducts/` | new |
| `GetLowStockProductsInputPort` | Input port | `inventory/application/getlowstockproducts/` | new |
| `GetLowStockProductsUseCase` | Use case (`@Service`, `@Transactional(readOnly = true)`) | `inventory/application/getlowstockproducts/` | new |
| `StockLevelRepository#findWithAvailableQuantityBelow(StockQuantity threshold)` → `List<StockLevel>` | Output port method (Repository) | `inventory/application/shared/StockLevelRepository.java:25` | changed |
| `InMemoryStockLevelRepository` — implement the new finder | Outgoing adapter | `inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java:25` | changed |
| `LowStockResource` (`@RequestMapping("/api/stock")`, `GET /low?threshold=`), staff-guarded in the adapter | Incoming adapter (REST) | `inventory/adapter/incoming/api/` (new package) | new |
| `LowStockProductDto` (`productId`, `availableQuantity`) | Adapter DTO | `inventory/adapter/incoming/api/` | new |

No domain change: `StockLevel` gets no new method and no new event — the criteria only read. No
change to `inventory/api/InventoryService.java` (the Open Host Service): no other bounded context
asks for this overview, and adding it there would widen the story.

Layer discipline this respects: the domain stays framework-free (nothing added to `domain/`), the
new port is declared inward in `application/` and implemented in `adapter/outgoing/`, the resource
depends on `GetLowStockProductsInputPort` and never on the use-case class (`AGENTS.md`, "API and
Authorization"), and no aggregate is written, so there is no transaction-boundary question.

## Acceptance criteria

- `lists-products-below-the-threshold`: Asking for the products below a threshold returns every
  product whose available quantity is lower than that threshold.
  → test shape: HTTP-level `MockMvc` test against the booted application
  (`@SpringBootTest` + `@AutoConfigureMockMvc`), in `src/test-integration`, run by
  `./gradlew test-integration`. Existing example of exactly this shape:
  `src/test-integration/java/dev/domaincentric/sample/ecommerce/api/ApiAuthorizationIntegrationTest.java:29-45`.
- `excludes-products-at-or-above-the-threshold`: A product whose available quantity equals or exceeds
  the threshold is not part of the answer.
  → test shape: same HTTP-level `MockMvc` test (`./gradlew test-integration`), asserting the
  boundary value (`availableQuantity == threshold`) is absent. Plus a unit test of the repository
  finder's boundary in `src/test` (`./gradlew test`).
- `names-the-available-quantity`: Each product in the answer names its available quantity, so the
  operator can tell how urgent it is.
  → test shape: same HTTP-level `MockMvc` test (`./gradlew test-integration`), asserting the
  response body carries the available quantity per product.
- `empty-answer-when-nothing-is-low`: When no product is below the threshold, the answer is empty and
  not an error.
  → test shape: same HTTP-level `MockMvc` test (`./gradlew test-integration`), asserting `200` with
  an empty collection rather than `404` or an error status.

The browser shape is available in this project (Playwright, `src/test-e2e`, `./gradlew test-e2e`,
e.g. `BackofficeE2ETest`) but was not chosen: no criterion specifies a page, wording, or placement,
so a browser test would require inventing UI the story does not ask for. The HTTP level is the
highest end-user level this story's criteria actually describe.

## Glossary proposals

- **Low stock**: a product whose available quantity is lower than a threshold the operator gives for
  the question. A property of the answer to a question, not a state stored on `StockLevel`.
- **Reorder threshold**: the quantity an operator passes with the question, below which a product
  counts as low stock. Per-question in this story, not per-product.
- **Operator**: the staff member who watches stock and reorders. Maps to the existing
  `ROLE_STAFF` claim (`IdentityProvider.Identity.ROLE_STAFF`); no new role.

## Open assumptions

- **Story open point — reserved quantity.** The plan reads `StockLevel#availableQuantity` and does
  *not* subtract `reservedQuantity`, because the criteria say "available quantity" and the Inventory
  glossary defines it that way (`inventory/domain/glossary.md:217-224`: physical stock "regardless
  of whether parts of it are already reserved"). Note the name clash this creates:
  `GetStockForProductsResult.StockData#availableStock` already means
  `availableQuantity − reservedQuantity`
  (`inventory/application/getstockforproducts/GetStockForProductsUseCase.java:45-47`), i.e. the
  glossary's "unreserved quantity". If the domain contact answers the story's open question with
  "reserved counts", the use case's mapping changes and the glossary clash needs resolving first.
- **Story open point — one threshold for all products.** The plan takes the threshold as a query
  parameter, so no per-product reorder level is stored. Should a product carry its own reorder
  level, that is a new field on `StockLevel` and a different story.
- **Exposure was chosen, not specified.** No criterion names an endpoint, a path, a response format,
  or a page. `GET /api/stock/low?threshold=` and the staff guard follow the project's own API and
  authorization policy; an operator page in `backoffice` remains the alternative and would need a
  new context relationship in `docs/context-map.md`.
- **Ordering is unspecified.** No criterion asks for an order (e.g. most urgent first), so none is
  planned and none will be tested.
- **The threshold's own validation is unspecified.** No criterion says what a negative or missing
  threshold does. The plan leaves it to `StockQuantity`'s existing "quantity >= 0" invariant
  (`inventory/domain/model/StockQuantity.java`) surfacing as a client error; no criterion covers it.
- **The stack profile does not declare the integration source set.** `factory.profile.yaml` declares
  `compile`, `test`, `e2eTest`, `architecture`, `format` — not `./gradlew test-integration`, though
  the task exists (`gradle/plugins/test-integration.gradle:32`) and is wired into `check`
  (line 56). The gate will therefore not run the criteria tests unless the profile gains an entry
  for that task. Adding it is a profile change, not part of this story — flagging it so the test
  stage does not discover it as a silent pass.
- **Only the in-memory repository implementation exists** for `StockLevelRepository`
  (`inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java` is the sole
  implementation found), so the new finder needs one implementation, not two.
