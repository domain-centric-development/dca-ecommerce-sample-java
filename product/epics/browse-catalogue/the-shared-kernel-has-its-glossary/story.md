---
id: the-shared-kernel-has-its-glossary
epic: browse-catalogue
kind: task
status: draft
created: 2026-11-02T10:34:23Z
context: product
title: The shared kernel has its glossary
closes: []
depends_on: []
---

# The shared kernel has its glossary

The shared kernel's words are written once, in `product/spec/shared-kernel/glossary.md`, in the format of a
context's glossary: each type of the shared kernel's domain — `ProductId`, `UserId`, `Money`, `Price`, `Role`,
`CurrencyMismatch`, the specification building blocks (`CompositeSpecification`, `And`, `Or`, `Not`, the visitor) —
and its one port, the identity port, each with its type (an id as `Value Object (ID)`), its definition and, for an
id, whose identity it is. Paging (`PageResult`, `PagingRequest`) is listed as a concept.

A context names a shared term where it uses it and no longer defines it: the entries of `UserId` in account and of
`Money` in pricing move into the shared kernel's glossary, and the context keeps the name where it uses it —
`` **Identity:** `ProductId` (Shared Kernel) `` on Product stays as it is. The shared kernel has no scenarios and no
features.

A change no actor sees and no code changes: no acceptance criterion, no test stage. It runs plan, build, tidy, judge
and document, and the existing suite, run whole, is its evidence.
