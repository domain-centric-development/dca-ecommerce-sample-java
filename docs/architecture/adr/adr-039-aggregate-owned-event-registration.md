# ADR-039: Aggregate-owned event registration

Status: Accepted, 2026-09-09 (approved review batch)

## Context and decision

A factory delegates to Product.create/Create. Only the aggregate registers ProductCreated from that creation method;
reconstitution produces no creation event. Java BaseAggregateRoot.registerEvent becomes protected in building-blocks
0.2.0, an intentional break for published 0.1.2 clients calling it externally. Migrate by raising the event inside the
aggregate's creation method. The .NET base registration method was already protected. No package is released in this batch.

## Consequences and verification

The shared specification owns business vectors; neither language is the authority. Changes are verified in both samples.
Generic harness consequence: supplied-fact responsibility and invalid-default validation are catalog guidance; no new
rule or marker proves semantic ownership. Existing rules continue to check structure. See the local test suites and README
for the paired-checkout command; publication/pinning awaits a first specification commit, which this batch forbids.
