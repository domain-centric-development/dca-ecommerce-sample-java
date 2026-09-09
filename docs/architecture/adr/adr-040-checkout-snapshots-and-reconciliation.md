# ADR-040: Checkout snapshots and reconciliation

Status: Accepted, 2026-09-09 (approved review batch)

## Context and decision

An explicit checkout action captures immutable positions, quantities and prices into a session. Cart edits do not
create or mutate sessions. A new action supersedes the previous OPEN/Active session; confirmed/completed orders remain.
Confirmation and replacement serialize through the same repository operation, including transaction completion.
Superseded confirmation has no completion effect. Abandonment/expiry closes only an open session and leaves cart contents.

Cart reconciliation intersects purchased unit intervals with the current stable position id. Later additions (also of
the same product), removed/re-added positions and other contents survive. Replay and overlapping completed snapshots
cannot remove a unit twice. JDBC/JPA cart persistence preserves the interval allocation watermark; in-memory persistence
retains the same domain state. Legacy CheckedOut/Completed cart statuses remain readable, but snapshot checkout leaves
an active cart editable and never completes the whole cart.

Confirmation retrieves current price/availability/stock facts before its local transaction. Pure domain services consume
immutable line/fact snapshots. Any changed price or shortage reports affected lines and leaves state, totals and events
unchanged. The buyer explicitly starts a fresh checkout against the new prices. Success stores the recomputed total and
publishes the same total; there is no no-argument confirmation path. Local in-memory repository serialization is not a
claim of durable distributed transactions or universal rollback of unenlisted resources.

## Consequences and verification

The shared specification owns business vectors; neither language is the authority. Changes are verified in both samples.
Generic harness consequence: supplied-fact responsibility and invalid-default validation are catalog guidance; no new
rule or marker proves semantic ownership. Existing rules continue to check structure. See the local test suites and README
for the paired-checkout command; both samples pin the initial specification review commit; remote consumption awaits publication.

## Event schema migration

`checkout-confirmed` becomes v2 in both samples: each line now requires stable position/interval correlation. This is a contract change, not a safe reinterpretation of historical v1 payloads. Retained v1 snapshots must not be rebuilt from the current cart or treated as whole-cart completion; a missing correlation fails reconciliation and requires operator review. No migration invents purchased units. The separately approved `product-created` change adjusts its unpublished v1 in place.
