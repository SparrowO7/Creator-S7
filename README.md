# Creator S7
Create anything. From your phone.

Mobile-first orchestration of AI providers, versioned skills and controlled execution.
Foundation v0.1 — 2026-11-05. Status: engineering contracts, source package,
native Android preview APK and a Colab demo notebook; there is no production API
server or managed render service yet.

## Try the original render pipeline in Colab
Open the [Creator S7 Colab notebook](https://colab.research.google.com/github/SparrowO7/Creator-S7/blob/main/notebooks/CreatorS7-Colab-Render.ipynb), choose a T4 GPU if Colab offers one, connect, and follow the notebook cells. The first setup downloads about 0.9 GB of models plus Python packages, so use Wi-Fi. Free Colab GPU availability and runtime duration are not guaranteed.

The notebook renders the included Inside Out gum demo with the original Chrome,
Kokoro, Whisper and FFmpeg pipeline. It saves the MP4 in Google Drive. The other
styles can be tried by changing the style in the notebook after the sample works.

## Start here
1. Read `docs/S7_Architecture.md` for boundaries and decisions.
2. Read `docs/skill-package-specification.md` and `shared/schemas/skill-manifest.schema.json`.
3. Read `docs/runtime-protocol.md` and `docs/provider-adapter.md` before implementing workers.
4. Follow `docs/MVP_Roadmap.md`, beginning with the rendering feasibility experiment.
5. `skills/faceless-shorts/` contains the actual installed skill source, all six styles,
   examples, scripts and vendor files. `S7_INTEGRATION.md` explains the adapter work required.

## Repository map
`apps/mobile` Android preview client (not yet Flutter); `apps/admin` deferred administration;
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
