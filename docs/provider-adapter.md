# S7 Provider Adapter Specification v0.1

## Interface
Adapters expose describe_capabilities(), validate_connection(), generate_text(),
stream_text() and normalize_error(). Image/TTS/video are separate capability interfaces;
a text adapter must not pretend to provide them. Implementations must use official
supported APIs. Account connection is shown only when provider-approved OAuth grants
the actual required API scopes; no browser-cookie/password extraction or subscription
to API entitlement conversion. BYOK is the first interoperable path.

Normalized request: request_id, task_type, provider_id, model_id, messages with roles
and typed text/image parts, output_schema_ref, max_output_tokens, deadline_ms,
temperature where supported, budget_reservation_ref, consent_ref and cancellation.
Credentials are passed through a broker context, never serialized into the request.
See shared/examples/provider-request.json. Reject unrecognized/unsupported parameters
rather than silently dropping required structured output or tool calls.

Normalized response: request_id, provider_request_id, provider_id, actual_model_id,
content parts, finish_reason, usage {input_tokens,output_tokens,cached_tokens},
cost {amount,currency,source}, schema_validation and safety flags. Missing usage/cost
is unknown, not zero. Price records are versioned and estimates labeled; do not hardcode
today's model availability, subscription entitlements or free quotas in the contract.

Stream events: message.start, text.delta, tool.call.delta, usage.update, message.end,
error. Every event carries request_id and seq. Buffer tool arguments until complete,
validate against an operation schema and obtain runtime policy admission before action.
Provider text never executes tools itself. A disconnected client can recover workflow
state; it does not automatically replay a partially billed generation.

## Routing
Filter by available credential, user allowed providers/models, region, language,
context capacity, structured-output and tool support. Then rank eligible candidates
by measured reliability, latency and estimated cost. Manual selection wins inside
policy. Persist the actual selected route and model in project provenance. No silent
fallback to a new provider or paid model beyond disclosed consent/budget. Free-only
mode rejects unknown/positive price, rather than trusting a model name containing free.
Fallback before any output may retry compatible providers within consent; after output
starts, record a failed attempt and restart a new explicit generation, never splice
two models' outputs invisibly. Validate JSON/schema before planning continues.

## Connections and secure storage
Connection record: owner, provider_id, auth_mode (byok/oauth/platform), credential_ref,
scopes, status, created_at, revoked_at and consent_revision. Validate connection without
logging the credential and label any API call that can be billable. Credential broker
resolves only approved provider hosts and short-lived job scopes. Block arbitrary
base URLs/private IP ranges by default to prevent SSRF; custom enterprise gateways
require separately configured allowlists and TLS policy.
Keep device-only keys in OS secure storage. Cloud execution needs an explicitly
cloud-enabled key or platform-funded access. Device-only must not silently upload
the key. OAuth refresh tokens stay in the broker and are revocable.

## MVP integrations and completion checks
OpenAI and Gemini text adapters are proposed implementation targets, not implemented
adapters in this package. Anthropic/other compatible endpoints follow after the same
contract tests. Provider-specific documentation and model availability must be checked
at implementation time. Tests cover missing/invalid keys, Unicode, cancellation,
429+Retry-After, timeout after provider acceptance, malformed structured output,
unsupported tools, unknown costs, tenant isolation, credential revocation, streaming
completion and refusal. Mock fixtures do not establish live provider compatibility.
