# ADR-028: The Account Owner's Name Is Immutable by Type, Not by Rule

**Date**: August 12, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team

---

## Context

An account belongs to a natural person. The requirement is that a user may change the basic
information of their account, but **not their first or last name**. The date of birth of the same
person may be corrected.

### Problem

"Must not be changed" can be implemented in several places, and most of them decay:

1. **In the adapter** — no name input on the form. A second adapter (the REST `AuthResource`, an MCP
   tool, an admin screen) reintroduces the change, and nothing notices.
2. **In the use case** — the command carries a name and the use case ignores or rejects it. The
   aggregate still has a setter, so the next use case can call it.
3. **In the aggregate** — a `changeName` method that throws. The operation exists in the API and its
   absence of effect is a runtime fact, not a compile-time one.

A guard test can pin any of these, but a test only fails once someone has already written the
change; and a guard whose promise is broader than its assertion (a regex over a few literal
spellings, a check that only looks at form fields) reads as protection while providing none.

---

## Decision

**Model the person as an `Owner` Value Object and make the name unreachable for modification, so
that no rule has to be remembered and no adapter can route around it.**

```java
public record Owner(String firstName, String lastName, LocalDate dateOfBirth) implements Value {

  /** Carries both names over unchanged — the only derivation this type offers. */
  public Owner withDateOfBirth(final LocalDate newDateOfBirth) {
    return new Owner(firstName, lastName, newDateOfBirth);
  }
}
```

The aggregate holds the owner as a whole and exposes exactly one mutator for it:

```java
public void changeOwnerDateOfBirth(final LocalDate newDateOfBirth) {
    // ... status check, no-op check
    final LocalDate previousDateOfBirth = owner.dateOfBirth();
    this.owner = owner.withDateOfBirth(newDateOfBirth);
    registerEvent(
        AccountOwnerDateOfBirthChanged.now(this.id, previousDateOfBirth, newDateOfBirth));
}
```

Three properties follow, and together they leave no path to a changed name:

| Property | Consequence |
|---|---|
| `Owner` is a record | Components are final; a held reference cannot be mutated |
| No `Account` operation accepts an `Owner` | The owner cannot be swapped wholesale |
| The one derivation copies the names | Correcting the date of birth structurally preserves them |

No operation on a *loaded* account can therefore change the name.

> **Open question — `reconstitute` is not covered by this.** The rule above holds for the aggregate
> in memory. `Account.reconstitute` is public and accepts an arbitrary `Owner`, so application code
> could rebuild an account under its existing `AccountId` with a different name and `save` it:
>
> ```java
> final Account renamed = Account.reconstitute(
>     existing.id(), existing.email(), Owner.of("Someone", "Else", dob), ...);
> accountRepository.save(renamed);   // no test fails today
> ```
>
> Today this is theoretical: **no production code calls `reconstitute` at all** — the in-memory
> repository stores the aggregate itself, so the method exists for the tests and for the JPA adapter
> that will come. It becomes real the moment a mapping repository lands.
>
> Deliberately left open rather than closed with an ArchUnit rule restricting the callers — the team
> wants to decide first whether the constraint belongs in a static rule, in a narrower visibility for
> `reconstitute`, or nowhere. **Revisit when the first mapping persistence adapter for `account` is
> written.**

### Options on the table (discussion state, August 2026)

Java cannot express "visible to the persistence adapter only". There is no `internal` (C#, Kotlin),
no `private[package]` (Scala), no `friend` (C++); the one real mechanism is a JPMS qualified export
(`exports ...domain.model to ...adapter.persistence;`), which works per package and needs a
modularized build this project does not have. Every option below is therefore a trade, not a fix.

| Option | Closes the gap? | Cost | Assessment |
|---|---|---|---|
| **A — Keep `reconstitute`, document the gap** | No | none | Acceptable. Matches the DCA guide's aggregate factories (`create` + `reconstitute`), which prescribe exactly this factory. **Decided: this stays for now.** |
| **B — Reflection into private fields** | Yes, no public entry point at all | field names as strings | **Rejected and removed.** `cart`'s JPA and JDBC repositories read `getDeclaredField("items")` / `("status")`; renaming a field left the compiler silent and broke at runtime. Both now call `ShoppingCart.reconstitute` — `cart` and `account` demonstrate the same pattern. |
| **C — Snapshot / Memento** (`toSnapshot()` / `fromSnapshot(AccountSnapshot)`) | **No** — the factory stays public | one record per aggregate | Interesting **on its own merits**, not as an answer to this question: it shrinks a nine-parameter signature to one named type and would serve a future document/JSON store. Worth deciding separately. |
| **D — inheritance-gated factory** (`protected`, persistence adapter extends it) — **preferred alternative if reopened** | Yes, at compile time | ~8 lines **inside** the aggregate | Closes it honestly, and cheaper than it first looks. Best form is a nested `public abstract static class Reconstitution` inside the aggregate, extending a generic `Reconstitution<A, S>` from `sharedkernel`: no extra file, the aggregate's constructor can go back to fully `private`, and an adapter opts in visibly with `extends Account.Reconstitution`. Putting the `protected` member on the aggregate *itself* does not work — access is granted by inheritance, and the repository would have to extend the aggregate. |
| **E — ArchUnit rule limiting the callers** | Yes, at test time | one rule | Not pursued for now (explicit decision). |
| **F — JPMS qualified export** | Only across modules | modularizing the build | The only option needing no per-aggregate code, but a qualified export cannot separate `application` from `domain` inside the same module — closing the gap would require one module per layer. Not worth that structure here. |

Note that C and D answer different questions and combine well: with a snapshot as the `S` of the shared contract, D reduces to one record plus one thin subclass. If the snapshot is adopted for its own reasons, D becomes markedly cheaper.

Ruled out on inspection, so nobody re-derives them: a **capability token** typed in `sharedkernel`
(anyone may subclass it to mint one; `sealed` cannot help, since `permits` requires the same package
or module and the shared kernel must not name an adapter), and **empty instance + `restoreFrom(...)`**
(moves the hole to an accessible constructor and adds a mutator — worse than the status quo).

**Decision for now: A.** `reconstitute` stays a public static factory on the aggregate, as the book
prescribes, and the gap stays documented rather than closed. If the question is reopened, **D in its
nested form is the preferred candidate** — it closes the gap at compile time, costs no extra file,
and lets the aggregate's constructor go back to `private`.

**Also on the agenda for that revisit:** ADR-008 names `application/port/out/` for output ports while
the code uses `application/shared/`. The decision is unaffected, only the path drifted — amend or
supersede.

### What the guard tests assert

The regression guards in `AccountTest` are deliberately narrower than "the name cannot change" —
each states exactly what it checks:

- No **instance** method of `Account` accepts an `Owner` parameter. The static factories are
  exempt — `register` legitimately introduces the name, `reconstitute` is the open question above,
  not something these guards cover
- `Account` declares no member whose name mentions "name" at all
- `ChangeProfileCommand` has exactly the components `userId`, `email`, `dateOfBirth`
- Correcting the date of birth leaves both names equal to the registered ones

---

## Consequences

### Positive

✅ **Enforced at the type level** — a new adapter cannot introduce a name change without first
   changing the aggregate's API
✅ **No rule to remember** — `withDateOfBirth` cannot be called wrongly
✅ **Honest guards** — each test's name and message describe exactly its assertion
✅ **One place for the concept** — name and date of birth travel together as one Value Object

### Neutral

⚠️ **Registration must collect the name** — `register.pug`, `RegisterAccountCommand`,
   `RegisterRequest` and every account fixture carry first name, last name and date of birth
⚠️ **A correction rebuilds the Value Object** rather than assigning a field

### Negative

❌ **A legitimate rename needs a code change** — a legal name change (marriage, court order) has no
   path today. Deliberate: that is an administrative operation with its own authorization and audit
   requirements, and it should arrive as its own use case, not as a side effect of self-service
   profile editing

---

## Alternatives Considered

### Alternative 1: No name in the account context at all

The earlier iteration of the profile page implemented "the name cannot be changed" as **absence**:
the account had no name, and `firstName`/`lastName` existed only in `checkout.BuyerInfo`.

**Rejected**: an account with no owner cannot state whose account it is, and the constraint became
vacuous — nothing was protected because nothing existed. It also pushed identity information into a
per-order shipping concept.

### Alternative 2: `Owner` as an Entity inside the aggregate

**Rejected**: the owner has no identity of its own within the account and no lifecycle — it is
defined entirely by its values. A Value Object is the correct classification, and immutability comes
free with it.

### Alternative 3: A `changeOwner(Owner)` operation guarded by an invariant check

```java
public void changeOwner(final Owner newOwner) {
    if (!newOwner.firstName().equals(owner.firstName())) {
        throw new IllegalArgumentException("The name cannot be changed");
    }
    // ...
}
```

**Rejected**: the operation exists, so every caller must be reviewed, and the rule lives in a
runtime check rather than in the shape of the API. This is exactly the guard the chosen design makes
unnecessary.

---

## Related ADRs

- [ADR-006: Domain Events as Immutable Records](adr-006-domain-events-immutable-records.md)
- [ADR-011: Bounded Context Isolation](adr-011-bounded-context-isolation.md) — why the checkout
  buyer's name is a separate concept from the account owner's name
- [ADR-023: Optional Results Instead of Exceptions](adr-023-optional-results-not-found.md) — how a
  rejected profile change is reported

---

## Validation

- [x] `./gradlew test` passes (316 tests)
- [x] `./gradlew test-architecture` passes (90 rules)
- [x] Glossary entry for `Owner` in `account/domain/glossary.md`
- [x] README and `architecture-principles.md` describe the constraint

---

**Accepted by**: Architecture Team
**Date**: August 12, 2026
**Version**: 1.0
