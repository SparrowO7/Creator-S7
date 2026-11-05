# Creator S7
Create anything. From your phone.

Mobile-first orchestration of AI providers, versioned skills and controlled execution.
Foundation v0.1 — 2026-11-05. Status: engineering contracts and source package;
there is no implemented Flutter application, API server or production renderer yet.

## Start here
1. Read `docs/S7_Architecture.md` for boundaries and decisions.
2. Read `docs/skill-package-specification.md` and `shared/schemas/skill-manifest.schema.json`.
3. Read `docs/runtime-protocol.md` and `docs/provider-adapter.md` before implementing workers.
4. Follow `docs/MVP_Roadmap.md`, beginning with the rendering feasibility experiment.
5. `skills/faceless-shorts/` contains the actual installed skill source, all six styles,
   examples, scripts and vendor files. `S7_INTEGRATION.md` explains the adapter work required.

## Repository map
`apps/mobile` Flutter client; `apps/admin` deferred administration;
`backend` control plane; `runtime` execution adapters; `skills` source packages;
`shared/schemas` machine-readable contracts; `infrastructure` deployment design.
Folders with README files describe intended ownership, not working services.

## Local contract checks
Python 3.11+ and `python -m pip install -r tools/requirements.txt`, then:
`python tools/validate_foundation.py`.
This validates schemas, examples and source hashes; it does not render a video.

## Release status
The imported skill is an unsigned development snapshot. The source has no top-level
license declaration. Preserve all vendor notices. Resolve redistribution and model/font
licenses before distributing packages or publishing the repository. No account,
credential, external repository, cloud resource or paid service was created.
