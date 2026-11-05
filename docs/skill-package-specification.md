# S7 Skill Package Specification v0.1

## Contract
A skill is a versioned archive containing instructions, declarative metadata,
styles, examples and optional trusted-worker source. Package version and protocol
version are separate. v0.1 is a development contract; incompatible schema changes
increment the schema major version before a stable release.

Required paths: manifest.json, SKILL.md, dependencies.json, permissions.json,
workflow.json, checksums.json. Styles and examples are referenced by relative paths.
The manifest is validated by shared/schemas/skill-manifest.schema.json (2020-12).
All manifest-listed paths must exist inside the package root. Use POSIX paths.

## Import and trust
1. Enforce compressed and expanded size limits before extraction (initial defaults
   100 MiB compressed, 250 MiB expanded, 10,000 entries; configurable by deployment).
2. Reject absolute paths, dot/dot-dot components, drive prefixes, backslashes, duplicate
   normalized paths, case-fold collisions, symlinks, hard links and device files.
3. Validate JSON and references without executing imports or package scripts.
4. checksums.json maps each payload path to lowercase SHA-256 of exact bytes. It
   excludes itself and signature.json. Extra/missing payload files are rejected.
5. Development imports are unsigned and cannot enter the production trusted registry.
   Production signature.json contains algorithm=Ed25519, key_id and base64 signature
   over the exact UTF-8 checksums.json bytes. The trusted registry resolves key_id.
   Sorted checksum output and UTF-8 LF are packaging requirements; do not reserialize
   signed bytes. Manifest, permissions and workflow are covered by payload hashes.
6. Verify signer authorization, key revocation, engine compatibility and explicit
   permission consent before activation. A valid signature establishes provenance,
   not safety of the code or ownership of third-party assets.

No signing key or valid release signature is included in this foundation.

## Planner boundary
SKILL.md supplies instructions to an AI planner; it is never interpreted as an
executable workflow. workflow.json defines approved operation IDs and dependencies.
Generated scripts are project-revision artifacts with hashes. The worker executes
only operations admitted by the runtime policy, in its sandbox. A skill cannot
grant itself network, credential, publication or filesystem privileges.

## Permissions and dependencies
The manifest's permission IDs must exactly match permissions.json. Each permission
describes resource scope, purpose and approval class. The broker intersects declared
permissions, user grants and deployment policy; unknown permissions deny execution.
Dependency metadata is descriptive during import. Actual release worker images
resolve and lock dependencies by digest with provenance/license review; no pip/npm
installation runs merely because a skill was imported. No provider secret enters
the archive. Native/local runtime flags require implementation and device evidence.

## Lifecycle
Download -> quarantine -> validate -> trust decision -> consent -> install immutable
version. Projects pin both skill version and package digest. Updates stage beside
the current version and require renewed permission consent when scope increases.
Active jobs keep their pinned version. Rollback changes the default for new jobs;
it cannot erase published outputs or reuse an incompatible checkpoint. Block revoked
versions from new execution and explicitly decide whether in-flight jobs cancel.

## Faceless Shorts package
Source retained unchanged under scripts/, styles/, agents/ and SKILL.md. S7 files
are additive. All six styles are included. Cloud=true; Android/iOS local execution=false
for this initial package. Current voice/alignment contract is English only.
Development snapshot version 0.1.0 is an S7 wrapper version, not an upstream release
claim. Redistribution remains unverified because no top-level license was found.
See skills/faceless-shorts/S7_INTEGRATION.md for actual pipeline and acceptance gates.
