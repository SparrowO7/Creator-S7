# S7 Runtime Protocol v0.1

## Scope and transport
The API owns workflow state. Workers use authenticated internal HTTPS, not a public
shell endpoint. Mobile clients submit to /api/v1/workflows and subscribe to job events.
All external APIs require session auth and tenant checks. protocol_version is 0.1;
an unknown major is rejected. Payload limits: control messages 256 KiB, media travels
through scoped object-store URLs. References never contain filesystem paths from users.

## Submission
POST /api/v1/workflows accepts project_id, project_revision, skill_id, skill_version,
skill_digest and input (validated against the skill input schema). Idempotency-Key
is required and scoped to the authenticated user for 24 hours; same key and same
body returns the same workflow, a changed body returns 409. Respond 202 with
workflow_id, job_id and events_url. Local editing can work offline; cloud submission
requires connectivity and never silently substitutes local execution.

## State machine
CREATED -> VALIDATING -> PLANNING -> WAITING_FOR_APPROVAL -> QUEUED -> EXECUTING
-> QUALITY_CHECK -> COMPLETED.
VALIDATING/PLANNING/QUEUED/EXECUTING/QUALITY_CHECK may enter FAILED or CANCEL_REQUESTED.
WAITING_FOR_APPROVAL may become QUEUED after approval, CANCEL_REQUESTED on decline,
or EXPIRED at expiry. CANCEL_REQUESTED -> CANCELLED only after worker termination
or proof no worker owns the job. FAILED -> QUEUED creates a new attempt only when
retryable, authorized and within retry/budget limits. Terminal states COMPLETED,
CANCELLED and EXPIRED do not restart; create a linked new workflow instead.
QUALITY_CHECK -> WAITING_FOR_APPROVAL is allowed for required human visual review;
approval completes the reviewed output rather than re-enqueueing execution. Store
approval purpose (plan vs output) to make these transitions unambiguous.

## Execution envelope
See shared/examples/runtime-job.json. Required: protocol_version, job_id, workflow_id,
attempt, fencing_token, skill digest/version, project revision, operation, input
artifact refs/digests, runtime class, limits, approval_ref, credential lease refs
and trace_id. Values are server-issued after policy admission. No raw key, arbitrary
shell command, interpolated command line or arbitrary URL belongs in the envelope.
Worker adapter constructs argv arrays for pinned operation implementations.

## Approvals
Bind approval to user/project, immutable plan digest, generated code digest,
source artifacts, upload destinations, provider/model, maximum currency/cost and
expiry. Editing script, destination, code or budget invalidates the approval.
The initial plan approval covers explicitly listed zero/within-ceiling operations;
publishing, paid generation outside ceiling, destructive actions and new uploads
require additional grants. An approval_ref cannot itself create permissions.
Retries remain inside the original consent and reserved budget or pause for renewal.

## Leases, events and recovery
Lease default 60 seconds; heartbeat every 15 seconds. Expired leases may be requeued
with a greater fencing token. Late workers cannot write state or commit outputs.
Each event has event_id, job_id, monotonic seq, timestamp, attempt, state, step_id,
phase and optional progress {completed,total,unit}. Commit state/event in one transaction.
GET /api/v1/jobs/{id}/events?after_seq=N replays durable events; WebSocket/SSE reconnect
uses the same cursor. At-least-once events require client deduplication by job/seq.
GET /api/v1/jobs/{id} returns the canonical snapshot and latest seq.

Checkpoint key = skill digest + worker image digest + step ID + input artifact digests
+ provider/model parameters. Resume reuses only validated immutable outputs with an
exact key match. Retry a transient timeout/429/worker loss with jitter, max 3 attempts;
honor Retry-After within deadline. Authentication, validation, policy, copyright/licensing
gate, unsupported language and budget failures are not automatic retries. Provider
timeouts may still incur costs; ambiguous billable requests need idempotency support
or user review before retry. Reserve estimates before dispatch; reconcile actual usage
and release unused reservations atomically. Concurrent jobs share a user's ceiling.

## Cancellation and output
POST /api/v1/jobs/{id}/cancel is idempotent. Workers kill the process group and upload
no new final artifacts after the cancellation fence. Cancellation does not undo an
external request already accepted. Only QUALITY_CHECK can commit final artifact refs.
Check digest, codec, dimensions, frame count/duration tolerance, audio stream,
caption bounds and container integrity. Human contact-sheet/audio review remains
mandatory for Faceless Shorts; machine checks alone cannot assert visual correctness.

Errors: {code,message,retryable,step_id,request_id,details}; sanitize details.
Stable codes: INVALID_INPUT, SKILL_UNTRUSTED, PERMISSION_DENIED, APPROVAL_REQUIRED,
PROVIDER_AUTH, RATE_LIMITED, BUDGET_EXCEEDED, DEADLINE_EXCEEDED, WORKER_LOST,
OUTPUT_INVALID, UNSUPPORTED_LANGUAGE, CANCELLED. HTTP: 400 input, 401 auth,
403 policy/ownership, 404 inaccessible object, 409 revision/state, 429 quota,
503 unavailable. Never put a credential or signed artifact URL into an error.
