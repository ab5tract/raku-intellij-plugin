# Level 3 — the fluency penalty

Levels 1 and 2 measure *finished code*: what a byte costs to tokenize, and how many
tokens the final program takes. Neither counts the cost of **getting** to a working
program. That cost is what hypothesis **H2** in `../00-preregistration.md` speaks to,
and it is the one place a minority language plausibly still hurts after Level 2 found
the per-byte penalty and the concision advantage cancelling out.

The preregistration marked this level BLOCKED, with the reason and the method both
written down:

> It cannot be run in a single context, because "throw away the Python and continue
> with Raku" is not achievable by deleting a file — once Python is in context it
> anchors everything downstream. Real discard requires a session boundary: each arm in
> a fresh agent that never sees the other, with only measurements returned.
>
> **What to do to run it:** N tasks × 2 arms × fresh sub-agents, each given only the
> task spec and the repo; record first-attempt pass/fail against the expected output,
> number of correction rounds, and total tokens emitted until the arm passes.

`run-01` is that, plus one extension.

## Design

**12 tasks × 2 languages × 2 models = 48 arms.** The model axis was added because the
fluency penalty is a property of the model as much as of the language: a frontier
model might erase it entirely, which is a measurable claim rather than a guess. Arms
ran on Sonnet and on Opus, the same 12 specs in each cell.

Each arm was a fresh `general-purpose` sub-agent that received its language, one
`tasks/<task>/SPEC.md`, and one output directory. Fresh context is the control the
preregistration asked for, and it is the one that matters: no arm could be anchored on
another arm's solution, because no arm ever saw one.

### The task set

Twelve **new** text-processing tasks over committed repo files. None overlap the five
in `../paired/`, whose solutions sit in the repo in both languages and would have been
an oracle. None touch `build/`, which is gitignored and machine-specific, so the whole
set is reproducible from a clean checkout.

The specs are language-neutral and pin down exactly what two independent
implementations diverge on: sort order, tie-breaks, tab separators, trailing newline,
characters-versus-bytes, rounding mode. They range from `l12-first-line-census`
(four counters) to `l10-camel-segments` (a two-rule CamelCase split), deliberately, so
the first-attempt pass rate would land away from both 0% and 100%.

### The protocol each arm followed

1. Save the program as `attempt-NN.<ext>` **before** running it.
2. Run it from the repo root, stdout to `attempt-NN.out`, stderr to `attempt-NN.err`.
3. A fix is never an edit — it is a new attempt number. Every version actually run
   survives on disk.
4. Write `journal.md`: one line per attempt, what changed and what was wrong.

Rule 3 is the whole measurement. An arm that iterates in its head before ever running
anything leaves no trace, and its silence is indistinguishable from getting it right
first time.

Arms were also told: do not read anything else under `../` or `../../report/`
(sibling solutions, prior findings, and a list of Raku gotchas that would hand one arm
an unearned advantage), and do not compute the answer by any means other than the
program — no `grep`/`awk` pipeline, no second implementation. That second rule is what
keeps first-attempt correctness meaningful.

Python arms additionally got the `CLAUDE.md` carve-out restated. `CLAUDE.md` forbids
Python for text processing and auto-loads into sub-agents; an arm not told it is a
*measured artifact* may refuse or hedge the task.

## Layout

```
tasks/<task>/SPEC.md              the spec an arm was given, and nothing else
runs/run-01/<model>/<task>/<lang>/
    attempt-NN.<ext>              every version actually run
    attempt-NN.out                its stdout
    attempt-NN.err                its stderr
    journal.md                    what changed each round, and why
runs/run-01/expected/<task>.out   the adjudicated correct answer
```

## Grading

Four independent implementations per task — 2 languages × 2 models. Agreement across
all four is strong evidence of ground truth, which is a real bonus of the 2×2 over the
two arms originally planned: it turns grading from a judgement call into a vote.
Disagreements are adjudicated by hand against the spec and the adjudication recorded.

Everything downstream is then mechanical, in `../70-measure-level3.raku`:

- `attempts` — number of `attempt-*` source files
- `first_ok` — `attempt-01.out` byte-identical to `expected/<task>.out`
- `final_ok` — the last attempt's `.out` byte-identical
- `rounds` — `attempts − 1`

Results: `../96-level3.tsv`, `../97-level3-rollup.txt`.

## What this measures, and what it does not

**Emitted-code tokens are not context tokens.** The preregistration asks for "total
tokens emitted until the arm passes". What is actually measurable here is the tokens
of the *code* each arm emitted across all its attempts. A sub-agent's true token usage
— reasoning, tool output, the spec it read — is not exposed by the harness. The
to-working figure therefore undercounts, and undercounts the debugging arm more than
the clean one, since debugging spends most of its tokens on things that are not code.
Read it as a lower bound on the effect, not a measurement of it.

**Blindness is enforced by fresh context plus instruction, not by a sandbox.** No arm
could see another arm's context, which is the control that matters. But the other
arm's output directory is reachable by absolute path, and nothing but the instruction
prevented an arm from looking. Per-arm git worktrees were considered and rejected:
they do not close this hole either, since results must land in the real repo by
absolute path regardless.

**The instrument is still `cl100k_base`**, a public proxy, not Claude's tokenizer —
exactly as in Levels 1 and 2. Only relative comparisons hold.

**n=12 per cell.** Better than Level 2's n=5, still not enough to read any individual
task as signal.
