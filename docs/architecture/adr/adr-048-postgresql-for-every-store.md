# ADR-048: Every Store on PostgreSQL — In-Memory Repositories Are Test Fakes

**Date:** 2026-11-09

**Status:** Accepted — supersedes the *Scope* of [ADR-031](adr-031-persistence-adapters-as-the-default.md) and its
`inmemory` profile; ADR-031's rule "a repository hands out copies" stands.

**Context:**

ADR-031 made real persistence the default for `account` (JDBC) and `cart` (JPA) and left `product`, `pricing`,
`inventory` and `checkout` in memory, handing out live references. The `inmemory` profile was partial, H2 was the
database, and the .NET twin kept every store in memory. Three things followed from that:

- A store in memory hands out the aggregate it holds and undoes nothing on rollback. A test that passes there can
  assume a rollback a real store would give — or not give one a real store would.
- Two shops on two databases (H2 here, in-memory or SQLite there) would let the two implementations diverge exactly
  where they must agree: how a cart's checkout or a product's stock is locked.
- The integration suite isolated its data by giving 81 test classes an in-memory database of their own, so the Spring
  context cache missed and most of the suite's time went into starting contexts.

**Decision:**

1. **PostgreSQL for every store, in both shops.** Each repository is a JDBC adapter (`JdbcClient`/`JdbcTemplate`, SQL
   written by hand, mapping in the adapter through the aggregate's `reconstitute`). JPA goes. Uniqueness claims are
   database constraints (the SKU, an account's email and user id, the active cart as a partial unique index —
   ADR-042, ADR-045). A processed-events record is a table in the same transaction as the effect it guards.
2. **Flyway migrates the schema**, one versioned script per context under `db/migration/` (`V1__account.sql` …),
   plain SQL with the same file names as the .NET shop's DbUp scripts. The event publication table stays Spring
   Modulith's.
3. **Check-then-change under a row lock.** A stock level, and a checkout session read by its cart, are read
   `FOR UPDATE` inside a transaction, so the one-at-a-time of `inStockLevels` and `inCartSession` holds across
   processes too.
4. **In-memory repositories are test fakes.** They live in the unit-test source set, store and hand out copies, and
   run the same contract tests as the JDBC adapters where a contract test exists. The `inmemory` profile goes.
5. **Tests and the local start run PostgreSQL in a container** (Testcontainers, Docker or Podman): one container per
   test JVM, one database for every Spring context in it. Before each test the database goes back to the state the
   start left — the seeded catalogue — so the suite shares its contexts instead of starting one per class.
   `./gradlew bootTestRun` starts the shop with a container of its own; `bootRun` and the image connect to
   `DATABASE_URL`.
6. **The start is ready when its events are delivered.** The seeder fills an empty catalogue only, and the start waits
   until Pricing and Inventory have provisioned what it created (`StartupEventDelivery`).

**Consequences:**

- Docker or Podman is required for the integration and browser suites and for `bootTestRun`, also in the factory's
  gates.
- The integration suite went from 4:02 (PostgreSQL, a database per class) to 2:17 with the shared contexts; on H2 it
  was about 3:20.
- A restarted shop keeps its data; the sample catalogue is not seeded twice.
- A test that sets up data once per class no longer can: every test starts from the seeded catalogue. A test about the
  scheduled redelivery turns it on for its own context and closes the context afterwards, since a cached context with
  redelivery on would resubmit what other tests leave failed.
- `@AsyncInitialize` and its processor went with the in-memory repositories, their only users.
