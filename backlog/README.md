# Backlog

New work is written as markdown with front matter, one file per item:

```
backlog/<epic>/epic.md        intent, goal, metric (an outcome event), domain_contact
backlog/<epic>/<story>.md     status, context, acceptance criteria with named keys, assumptions
```

The contract and the field meanings live with the pipeline, in the `factory-run` skill's
`reference/backlog-contract.md`. The story gate refuses a story whose epic is incomplete, whose
context is not on the context map, or which is still `status: draft`.

## The delivered work stays where it is

`tasks/prd.json` and `tasks/prd.md` hold 146 stories, 145 of them delivered. They are **history**
and are not migrated: rewriting a finished story into the new shape would invent an intent, a goal,
an outcome event and a domain contact that nobody stated at the time, and every one of those epics
would then fail the gate for good reason. The contract applies to **new** stories — the same
brownfield rule the pipeline states for any project it enters.

The one story that never shipped, `US-146` ("Decide the Anti-Corruption Layer's fate"), is not a
story either: it asks a question rather than describing behaviour a user can observe. It belongs to
a scoping step, not to a delivery run, and stays in `tasks/prd.json` until that step exists.
