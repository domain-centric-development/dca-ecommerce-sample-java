# ADR-038: Product-created notification payload

Status: Accepted, 2026-09-09 (approved review batch)

## Context and decision

Product creation and Pricing hold Price(Money), strictly greater than zero. Initial stock is non-negative.
The unpublished product-created v1 contract is changed in place to exactly eventId, occurredOn, productId, amount
(decimal string), currency (ISO 4217) and initialStock. No shared-kernel object, sku, name or category is carried.
Both consumers belong to this sample family; Pricing reconstructs its own Price and validates it. Later payload changes
require v2. Both serializer paths and shared vectors verify this contract.

## Consequences and verification

The shared specification owns business vectors; neither language is the authority. Changes are verified in both samples.
Generic harness consequence: supplied-fact responsibility and invalid-default validation are catalog guidance; no new
rule or marker proves semantic ownership. Existing rules continue to check structure. See the local test suites and README
for the paired-checkout command; publication/pinning awaits a first specification commit, which this batch forbids.
