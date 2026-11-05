# Faceless Shorts in Creator S7
The complete installed skill source is copied here, not rewritten. S7 adds a manifest,
input schema, permissions, dependency inventory and declarative workflow. No video
was generated and no setup/model download was run during packaging.

## What is included
Original SKILL.md, agents metadata, seven shared script/helper files, six STYLE.md
files, six reference style sheets and complete example folders including JS source,
capture/mix tools, fonts, timing data and Three.js vendor/license files where present.
checksums.json covers the complete development package; provenance.json records hashes
of imported source files separately. Existing license notices remain in place.

## Worker adapter mapping (to implement)
| Operation | Existing skill interface |
|---|---|
| materialize | scripts/new_video.sh STYLE NAME --dir JOB_WORKSPACE |
| voice (AI) | scripts/py scripts/voice.py PROJECT |
| align (AI) | scripts/py scripts/words.py PROJECT |
| align (recording) | scripts/py scripts/words.py PROJECT --audio APPROVED_AUDIO |
| estimate | scripts/py scripts/estimate.py PROJECT |
| animation | planner edits script.json and src/shots.js in isolated project revision |
| capture-check | node tools/capture.mjs check |
| cues | node tools/capture.mjs cues |
| mix | scripts/py tools/mix.py . |
| review | node tools/capture.mjs sheet review/all.jpg 0.5 10; full key frames + audio review |
| encode | node tools/capture.mjs video renders/NAME.mp4 |

Commands document upstream interfaces. Do not interpolate user text into shell
strings. The actual adapter must validate STYLE against the manifest, generate safe
job names and invoke vetted argument arrays/entrypoints in a pinned Linux image.
Read the selected STYLE.md completely before planning. Workflow operation IDs are
contracts, not executable adapter code included in this foundation.

The native skill targets English Kokoro voices and Whisper small.en. Hindi narration
is unsupported in this package contract until a compatible voice/alignment pipeline
is selected and validated. Target duration input is a request: style-specific duration
limits must be checked by the planner before approval. No AI-generated image/video
service or paid generation is required by the original skill; optional text planning
uses a separately authorized provider. Do not silently change these guarantees.

Phone responsibility: topic/style input, approval, status, preview and MP4 save.
Cloud responsibility: private Python runtime, Chrome, fonts, audio models, capture,
mix, encode and machine QA. Phone local Python/Chrome execution is not enabled.
Original visual QA rules remain mandatory; an automated media probe cannot replace
human review. Rendering is complete only after the final MP4 has also been inspected.

## Release blockers
No top-level upstream skill license was found; do not infer redistribution rights
from possession of the files. Review fonts, Three.js and downloaded model licenses.
Resolve dependency locks/digests, implement worker operations, sign the release,
validate sandboxing, render all styles and review artifacts before production install.
