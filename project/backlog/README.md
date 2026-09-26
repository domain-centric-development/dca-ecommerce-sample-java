# Backlog

New work is written as markdown with front matter, one file per item:

```
project/backlog/<epic>/epic.md        intent, goal, metric (an outcome event), domain_contact
project/backlog/<epic>/<story>.md     status, context, acceptance criteria with named keys, assumptions
```

The contract and the field meanings live with the pipeline, in the `factory-run` skill's
`reference/backlog-contract.md`. The story gate refuses a story whose epic is incomplete, whose
context is not on the designed map (`project/domain.md`), or which is still `status: draft`.

## The delivered work stays where it is

`tasks/prd.json` and `tasks/prd.md` hold 146 stories, 145 of them delivered. They are **history**
and are not migrated: rewriting a finished story into the new shape would invent an intent, a goal,
an outcome event and a domain contact that nobody stated at the time, and every one of those epics
would then fail the gate for good reason. The contract applies to **new** stories — the same
brownfield rule the pipeline states for any project it enters.

## The shop's behaviour, described once

What the shop does today is described in the current contract elsewhere: the shared specification
carries a **replay backlog** — 11 epics and 62 stories that, delivered in order on an empty project in
any language, produce the shop as it behaves now. It is not a migration of the PRDs above. It was
written from the shop's tests, pages and interfaces, and every epic's intent, goal, metric and contact
was stated by the product owner when it was written, not reconstructed from a ticket.

In this shop those stories are not built again. They are taken over as **adopted** (`status: adopted`):
the pipeline maps each scenario to a test that exists here and is green, writes a characterization test
where none does — shown to work by a break that turns it red — and a fresh judge reads every test against
its scenario. Adoption is incremental: the replay's epics come into this folder as the next new story
needs them. `browse-catalogue` is the first — CAT-01 and CAT-02 adopted.

The one story that never shipped, `US-146` ("Decide the Anti-Corruption Layer's fate"), is not a
story either: it asks a question rather than describing behaviour a user can observe. It belongs to
a scoping step, not to a delivery run, and stays in `tasks/prd.json` until that step exists.
