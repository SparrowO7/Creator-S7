# Creator S7 Android video smoke-test app

This installable Android slice lets you enter a topic, choose one of the six
Faceless Shorts visual directions, render illustrated scenes with spoken
Android text-to-speech, mux them into a portrait MP4, and save it in Movies.
Keep Creator S7 open during the render. The phone needs an English TTS voice
installed; synthesis is performed by the phone's existing TTS engine.

## Build

Use the no-download build script from PowerShell (requires Android Studio's
bundled Java and Android SDK):

```powershell
./build_android.ps1
```

APK: `../../CreatorS7-test-v0.2.apk`

This is still a mobile preview renderer: 720x1280 H.264 with AAC narration and
native vector scenes guided by the skill's palettes/storyboards. It does not yet
run the original Node/Chrome/ffmpeg/Kokoro/Whisper workflow or true 3D scenes,
and it does not write researched, topic-specific scripts. The complete original
skill source and examples remain under `skills/faceless-shorts/`. The APK is
test-signed for sideloading, not a Play Store release.
