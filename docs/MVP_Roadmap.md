# Creator S7 MVP roadmap
No calendar commitment until the render spike and Android device measurements finish.

| Gate | Deliverables | Evidence required to advance |
|---|---|---|
| 0 — Foundation (this package) | contracts, manifest schema, actual six-style source snapshot | JSON/schema/hash checks; no app execution claimed |
| 1 — Render spike | pinned cloud worker, Inside Out 10-second fixture, then narrated English short | contact sheet + listened audio + playable MP4; CPU/RAM/time/bytes/cost recorded |
| 2 — Durable backend | FastAPI, PostgreSQL migrations, auth/ownership, outbox/queue, private storage, job state | duplicate delivery, worker death, cancellation and tenant tests |
| 3 — Android vertical slice | Flutter input/style/approval screens, upload consent, status/reconnect, preview/save | physical Android creates and saves a real MP4 after app restart |
| 4 — Text providers | BYOK broker, one working adapter, second adapter, budget reservation | real provider connection plus contract fixtures; no secrets in telemetry |
| 5 — Skill lifecycle | quarantine/import/trust/version pinning/update/rollback | tampered archive rejected; old jobs retain old package; permission increase gated |
| 6 — Six styles and release | complete style qualification, performance, deletion, licenses, store review | each style's QA artifacts; retention deletion; signed packages and threat review |
| 7 — iOS and selective local ops | same Flutter flows, bundled media ops, cloud render | real iOS device evidence; review-policy decision; no arbitrary downloaded-code promise |

## First feasibility experiment
Use the existing skill without rewriting its renderer. Start with Inside Out to
separate pipeline viability from 3D/GPU availability. Run setup/selftest only in a
controlled worker development environment, after reviewing dependencies and licenses.
Download weights once to an explicitly budgeted cache; never bake user assets/keys
into the image. Pin all binaries/dependencies and image digest before qualification.
Render a deterministic caption-only 10s clip, then a 25–40s English narrated short.
Execute the skill's capture check/cues, mix, sheet/frame visual review and video export.
Measure real wall time, output duration, peak RSS, CPU/GPU class, storage and egress.
Draft target output: 1080x1920 H.264/AAC, 30fps for 2D unless the style specifies otherwise;
3D software fallback may use 24fps/reduced resolution and must be disclosed.
Do not call the spike successful based only on an HTTP health response or file existence.

Gate 1 passes when the worker renders with network disabled after dependencies are
staged, rejection/timeout behavior works, captions fit phone safe areas, audio is
clear and the final file is playable on a physical target device. This foundation
does not include a rendered video or a completed spike.

## Initial backlog
S7-001 establish toolchains and lockfiles; S7-002 worker sandbox/image; S7-003 render
spike; S7-004 SQL migrations/ownership; S7-005 auth; S7-006 jobs/outbox/fencing;
S7-007 approvals/budget ledger; S7-008 artifact storage; S7-009 Flutter project shell;
S7-010 Android end-to-end flow; S7-011 provider adapters; S7-012 package signatures;
S7-013 resume/cancel QA; S7-014 style qualification; S7-015 licensing/release.
Cloud worker feasibility precedes committing to a large UI build.

## MVP acceptance journey
Android user creates project -> configures supported BYOK -> installs trusted skill
version -> submits English topic/style -> approves script, storyboard, data transfers
and cost ceiling -> receives durable job progress -> closes/reopens app -> reviews
sheet/audio/video -> approves final output -> saves MP4 to phone. A failed worker can
resume valid checkpoints; a signed skill update installs without mutating old projects.
Ads, premium billing, marketplaces, arbitrary community execution and publishing
automation are outside this first acceptance journey.
