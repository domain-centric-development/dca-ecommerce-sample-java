# Portal — Ubiquitous Language (Bootstrap)

> **Bootstrap status:** This glossary was initially derived from the existing code.
> Portal is the Bounded Context of a **generic subdomain — UI composition**: it owns
> the landing page and the navigation and *displays* concepts of other Bounded
> Contexts without *owning* them. It has no rich domain model yet. No terms are
> invented here on purpose; instead, this glossary lists the concepts the Portal
> refers to.

## Module Character

Portal is declared a **Bounded Context** in `package-info.java`
(`@BoundedContext(name = "Portal")`). Its subdomain is generic — UI composition —
so it carries the thin shape the pattern-selection decision (ADR-025) allows:

- A landing page (`HomePageController`, `GET /`)
- Navigation to Product Catalog, Shopping Cart, Checkout
- No Aggregates, Entities, Value Objects, Events, or Domain Services
- No application use cases (purely presentational)
- Allowed dependencies: `sharedkernel`, `infrastructure`

**Classification:** a Bounded Context with a generic subdomain (UI composition).
The boundary is a language boundary, not a model size: the Portal's terms —
landing page, navigation — are its own, and the eight contexts of this sample
stay eight. See "Open Questions" below for how the context may grow.

---

## Own Terms

### HomePage

**Definition:** Entry page of the e-commerce portal. Displays the application
title and navigation elements for products, cart, and checkout.

**Type:** Concept (UI view, not a domain concept)

**Operations:** `GET /` → `home/index` (Pug template)

**Notes:** Currently the module's only endpoint.

---

## Referenced Terms (displayed from other contexts)

These terms **do not belong to Portal** — it only displays them or links to
them. Definitions can be found in the respective context glossaries.

| Term              | Owning Context        | Usage in Portal                               |
|-------------------|-----------------------|-----------------------------------------------|
| Product / Catalog | `product`             | Navigation to product listing                 |
| Cart              | `cart`                | Navigation to shopping cart                   |
| Checkout / Order  | `checkout`            | Navigation to checkout flow                   |
| Account / User    | `account`             | Login/profile entry points (once available)   |

---

## Open Questions

- Should Portal eventually get its own view model for cross-context dashboards
  (e.g. "Recommendations", "My Dashboard")? If so, real application use cases
  would emerge here — but still no Aggregates of its own, only read models
  derived from other contexts.
- How will Portal handle aggregated views once they become necessary — via
  integration events plus a local read model, or via Open Host Services of
  other contexts?
