---
id: product-refusals-name-the-field-in-the-shops-words
epic: shop-api
status: draft
created: 2026-10-23T21:25:20Z
context: product
title: Product refusals name the field in the shop's words
depends_on: [the-take-over-leads-only-to-pages-of-the-shop]
---

# Product refusals name the field in the shop's words

## Story

As a partner creating a product through the API I want each refusal to name the field and say why in the shop's words, so that I can correct the request without reading framework messages.

## Acceptance criteria

### Rule: A refused product request names the field in the shop's words

#### a-missing-sku-names-the-field (happy path)
- Given a member of staff with a token
- When they create a product without a SKU
- Then the answer is a problem document whose error names the field `sku` with the shop's sentence for a missing value

#### a-too-long-name-names-the-field
- Given a member of staff with a token
- When they create a product whose name is longer than allowed
- Then the problem document's error names the field `name`

## Notes

- From the findings triage of 2026-10-23 (WP-151): a finding that changes behaviour is a story, not a task.
- .NET answers a missing SKU or category with the framework's sentence, a long name without the field, an unreadable value with the serializer's message.
