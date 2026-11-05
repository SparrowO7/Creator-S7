# Implementation handoff
Start with S7-001 through S7-003 in MVP_Roadmap.md. The mobile UI, API, provider
adapters and workflow executor are not implemented. Do not treat directory READMEs
or operation IDs as working code. The actual Faceless Shorts source is available
under skills/faceless-shorts and must remain intact during the first worker spike.

First pull request: pin a worker toolchain; wrap the existing Inside Out fixture;
stage licensed audio dependencies; run caption-only capture and then narration;
deliver MP4 + contact sheet + redacted benchmark record. Introduce no mobile dynamic
code execution, production signing keys or billable provider defaults.

Before declaring an end-to-end product: implement session/tenant checks, artifact
lineage, approvals, worker fencing, budget reservations, provider adapters and
Android UI/reconnect/save. Run the acceptance journey on a physical device. Review
the finished encoded video after the contact-sheet/audio approval and before final
artifact commit. Every approval must bind the exact artifacts being approved.

Do not publish this source snapshot until licensing is resolved. No external GitHub
repository, deployed backend, Android APK, iOS build or rendered demo is delivered
by this foundation package.
