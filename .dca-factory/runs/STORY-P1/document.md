# Document — STORY-P1

Stage carrier: the stack profile (`.agents/factory/factory.profile.yaml`) names no `carrier.document`,
so this stage ran in-session as described by the `stage-document` skill.

Knowledge source: the profile names `knowledge: dca-knowledge`
(`.agents/factory/factory.profile.yaml:17`). No question arose here that would decide anything — the
story's pattern decisions were settled in `tasks/STORY-P1/plan.md` and no new one was taken in this
stage — so no node was consulted and none is cited. Every document below was decided from the
project's own artifacts: `AGENTS.md`, the existing README and package-structure documents, the
Inventory glossary, and the code named in each row.

## Glossary

| Term | Context | Added or changed | Definition source |
| --- | --- | --- | --- |
| Low stock | Inventory | added — `src/main/java/dev/domaincentric/sample/ecommerce/inventory/domain/glossary.md:250` | `tasks/STORY-P1/plan.md:96-97`, narrowed to what the code does: strict `<` (`src/main/java/dev/domaincentric/sample/ecommerce/inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java:56-61`), measured on `StockLevel#availableQuantity` and not on unreserved quantity (`src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/getlowstockproducts/GetLowStockProductsResult.java:24,26`) |
| Low stock threshold | Inventory | added — `src/main/java/dev/domaincentric/sample/ecommerce/inventory/domain/glossary.md:270` | `tasks/STORY-P1/plan.md:98-99`, renamed: the code says `threshold` (`src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/getlowstockproducts/GetLowStockProductsQuery.java:9`) and never "reorder", and no product carries a level of its own — an entry called "Reorder threshold" would name a concept the model does not have |

Both entries sit under *Concepts (not in code, but in conversation)*
(`src/main/java/dev/domaincentric/sample/ecommerce/inventory/domain/glossary.md:215`) rather than
under *Value Objects* (line 51): neither term is a type. "Low stock" is a property of an answer, and
the threshold is an `int` component of a query record, not a `StockQuantity`.

Not added: **Operator**. `tasks/STORY-P1/plan.md:100-101` proposed it, but the Inventory domain does
not carry it — it is the caller of a REST route, mapped to the existing `ROLE_STAFF` claim
(`src/main/java/dev/domaincentric/sample/ecommerce/inventory/adapter/incoming/api/LowStockResource.java:48`).
The Inventory glossary is a domain glossary; a role from the exposure would put adapter vocabulary
into the model's language.

## Documents updated

| File | What changed | Verified by |
| --- | --- | --- |
| `src/main/java/dev/domaincentric/sample/ecommerce/inventory/domain/glossary.md` | two entries under *Concepts* — "Low stock" (line 250), "Low stock threshold" (line 270) — each naming the types that carry it, the strict comparison, the on-hand measure and the per-question threshold | read `src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/getlowstockproducts/GetLowStockProductsQuery.java:12-16` (non-negative), `.../GetLowStockProductsResult.java:26` (`LowStockProduct`), `src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/shared/StockLevelRepository.java:44-52` (`findWithAvailableQuantityBelow`), `src/main/java/dev/domaincentric/sample/ecommerce/inventory/adapter/outgoing/persistence/InMemoryStockLevelRepository.java:56-61` (strict `<`) |
| `README.md` | the Inventory line in the context list names the low stock overview (line 16); the Inventory package tree gains `application/getlowstockproducts/` (line 588) and `adapter/incoming/api/` (line 597); the `/api/**` authorization table gains `GET /api/stock/low?threshold=` → staff role (line 727); a new *Stock API* section (line 791) with the curl call and the response shape | the six new files listed by `find src/main/java/dev/domaincentric/sample/ecommerce/inventory/{adapter/incoming/api,application/getlowstockproducts} -type f` are exactly the ones the trees name; route and guard read in `src/main/java/dev/domaincentric/sample/ecommerce/inventory/adapter/incoming/api/LowStockResource.java:31,44,48-50`; response shape in `src/main/java/dev/domaincentric/sample/ecommerce/inventory/adapter/incoming/api/LowStockProductDto.java:9`; `400` on a negative threshold in `LowStockResource.java:54-60` together with `GetLowStockProductsQuery.java:13-14` |
| `docs/architecture/package-structure.md` | the Inventory tree gains the same two packages — `getlowstockproducts/` (line 350) and `adapter/incoming/api/` (line 357) — with the route noted on `LowStockResource` (line 358) | same `find`; route read in `LowStockResource.java:31,44` |

Not changed, and checked:

- **`docs/context-map.md`** — the profile's `contextMap`
  (`.agents/factory/factory.profile.yaml:28`). No relationship changed: the story added an incoming
  REST adapter *inside* Inventory and no cross-context import. Inventory's rows
  (`docs/context-map.md:13,35,38,41,45,47`) and its Open Host Service `inventory.api.InventoryService`
  are untouched — `git diff` lists no file under `src/main/java/.../inventory/api/`.
- **`docs/architecture/context-map.md`** — generated from the package annotations by
  `ContextMapDocumentationTest`. `./gradlew test-architecture --rerun-tasks --tests
  "*ContextMapDocumentationTest*"`: BUILD SUCCESSFUL, and `git status --porcelain docs/` afterwards
  reports only `docs/architecture/package-structure.md`, i.e. the regenerated map is byte-identical to
  the committed one. `tasks/STORY-P1/judge.md:119-122` expected this file to need Inventory's new
  incoming adapter; it does not — the map records contexts, published interfaces and relationships
  (`docs/architecture/context-map.md:24,76-86`), not adapters.
- **`docs/architecture/README.md`** — line 28 lists the contexts only to say each follows the same
  package template; `inventory` is in that list and the template is unchanged by this story. (The line
  omits `backoffice`; pre-existing, unrelated to this story, and therefore not touched from here.)
- **`docs/architecture/architecture-principles.md`** — no pattern in it changed. The story introduces
  no new kind of element: a query use case, an input port, a REST resource and a repository finder are
  all shown there already. Its module-dependency listing still holds:
  `inventory` remains a leaf with no business-context dependency (line 754), because the new adapter
  imports nothing outside Inventory and the shared kernel (`LowStockResource.java:3-7`).
- **`docs/architecture/adr/adr-036-api-authorization-at-the-adapter.md`** — the ADR states the rule
  the new guard follows; it names routes as the evidence for its decision, not as a live registry of
  them. The current route table lives in `README.md:722-729` and carries the new row. An accepted ADR
  records a decision at its date and is not rewritten as routes are added.

## Not documented

- **Whether reserved quantity belongs in the measure** — the story's own open point
  (`backlog/stock-oversight/STORY-P1.md:30-31`, `tasks/STORY-P1/judge.md:85-90`). The glossary entry
  states what the code does and names the question; it does not answer it. Waits on the domain
  contact. Its practical consequence, for the same person's eye: a product with 10 on hand and 10
  reserved is never reported as low.
- **The `availableQuantity` / `availableStock` vocabulary clash inside Inventory** —
  `GetStockForProductsResult.StockData#availableStock` means on-hand minus reserved, i.e. the
  glossary's *Unreserved quantity (available-to-promise)*
  (`src/main/java/dev/domaincentric/sample/ecommerce/inventory/domain/glossary.md:239`), while the new
  `LowStockProduct#availableQuantity` means on-hand (`tasks/STORY-P1/judge.md:78-84`). Pre-existing
  and not introduced here; the glossary already defines all three terms correctly, so no entry was
  changed. Renaming a field another context reads is its own story, and this stage may not rename.
- **`GET /api/stock/low` as an architecture decision.** Choosing a staff-guarded `/api/**` route over
  an operator page in `backoffice` was decided in `tasks/STORY-P1/plan.md:32-43` and specified by no
  criterion. It follows ADR-035 and ADR-036 as they stand and needs no new ADR; should the
  `backoffice` alternative ever be taken, *that* is a new context relationship and an ADR. Recorded
  here rather than in a reader document, because a reader document says what is, not what was weighed.
- **Ordering, a per-product reorder level, threshold-validation semantics** — unspecified by the
  criteria and recorded as open assumptions in `tasks/STORY-P1/plan.md:114-125`. The README states
  only the behaviours that exist and are pinned by a test or readable in the code (strict comparison,
  empty array, `400` on a negative threshold); it promises no order.
- **The plan-stage gap named by `tasks/STORY-P1/build.md:110-116`** — that a story adding a guarded
  `/api/**` route must name
  `src/test-integration/java/dev/domaincentric/sample/ecommerce/api/ApiAuthorizationIntegrationTest.java`
  as an affected element. Process guidance for the plan stage, not a statement about this system, so
  it stays in the run's files.
