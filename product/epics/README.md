# Backlog

New work is written as markdown with front matter, one file per item:

```
product/epics/<epic>/epic.md                  intent, goal, metric (an outcome event), domain_contact, depends_on
product/epics/<epic>/<story>/story.md         status, context, acceptance criteria with named keys, assumptions
product/epics/<epic>/<story>/decisions/       the questions a run asked and the answers, the acceptances
```

The contract and the field meanings live with the pipeline, in the `factory-run` skill's
`reference/backlog-contract.md`. The story gate refuses a story whose epic is incomplete, whose
context is not on the designed map (`product/domain.md`), or which is still `status: draft`.

## The delivered work stays where it is

`tasks/prd.json` and `tasks/prd.md` hold 146 stories, 145 of them delivered. They are **history**
and are not migrated: rewriting a finished story into the new shape would invent an intent, a goal,
an outcome event and a domain contact that nobody stated at the time, and every one of those epics
would then fail the gate for good reason. The contract applies to **new** stories — the same
brownfield rule the pipeline states for any project it enters.

## The shop's behaviour, described once

Behaviour the shop already had is described by **adopted** stories (`status: adopted`), taken from the
shared specification's replay backlog — not a migration of the PRDs above: written from the shop's tests, pages and
interfaces, with every epic's intent, goal, metric and contact stated by the product owner, not reconstructed from a
ticket. An adopted story is not built again: the pipeline maps each scenario to a test that exists here and is green, writes a characterization test
where none does — shown to work by a break that turns it red — and a fresh judge reads every test against its
scenario; the adopt gate then delivers it, and it reads `status: delivered` like a built story. Every epic of the
replay backlog is in this folder, together with the stories built since.

The one story that never shipped, `US-146` ("Decide the Anti-Corruption Layer's fate"), is not a
story either: it asks a question rather than describing behaviour a user can observe. It belongs to
a scoping step, not to a delivery run, and stays in `tasks/prd.json` until that step exists.
