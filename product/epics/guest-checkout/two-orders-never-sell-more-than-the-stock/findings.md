---
story: two-orders-never-sell-more-than-the-stock
epic: guest-checkout
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (7 rows, 0 without a fitting value)
---

# Findings — two-orders-never-sell-more-than-the-stock

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/reducestock/ReduceStockUseCase.java:83 | minor | introduced |  | A sale now reports a cancelled reservation, and the rule lives outside the stock level. | The use case composes release + decreaseStock and repeats its guard; a sale raises `StockReleased` ("a reservation was cancelled"). | One `StockLevel` operation for a sale consuming its reservation, with its own event. | open | code | domain-invariant | incorrect |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/reservestock/ReserveStockUseCase.java:63 | minor | introduced |  | Three use cases redo the stock level's own arithmetic. | `unreserved` here and the `Math.min` clamps in ReleaseStock/ReduceStock compute the glossary's unreserved quantity outside `StockLevel`. | `StockLevel.unreservedQuantity()` and `releaseUpTo(int)`. | open | code | duplication | extraneous |
| 3 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/reservestock/ReserveStockUseCase.java:53 | minor | introduced |  | Reserving pairs stock and quantity by position in two lists. | Index pairing relies on the check loop adding exactly one entry per line. | Collect `(stockLevel, quantity)` pairs in the check loop. | open | code | algorithm | incorrect |
| 4 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/checkoutcompletion/confirmcheckout/ConfirmCheckoutUseCase.java:110 | minor | introduced |  | Every placing waits behind the call into Inventory, and why is not recorded. | The reservation runs inside the repository-wide `inCartSession` monitor; lock order checkout → inventory is not recorded. | Record the placement and the lock order in ADR-041, or reserve before the monitor and re-assert inside it. | done | docs | documentation | missing |
| 5 | test | src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/releasestock/ReleaseStockUseCase.java:35 | minor | introduced |  | Giving back stock after a failed order is never exercised by any test. | No test reaches ReleaseStock or the compensation in ConfirmCheckout. | One use-case test through `ReleaseStockInputPort`. | open | test | checking | missing |
| 6 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/inventory/application/reservestock/ReserveStockResult.java:22 | minor | introduced |  | A result method nobody calls. | `success()` has no caller. | Delete it or use it in `InventoryService.reserveStock`. | open | code | dead-code | extraneous |
| 7 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/application/shared/StockReservationPort.java:23 | minor | introduced |  | An order line accepts a zero quantity it says it refuses. | `Line` documents `quantity` as positive but guards only `productId`. | Guard `quantity > 0`. | done | code | checking | missing |
