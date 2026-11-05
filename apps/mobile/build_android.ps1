$ErrorActionPreference = 'Stop'
$app = $PSScriptRoot
$repo = Split-Path (Split-Path $app -Parent) -Parent
$sdk = if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { Join-Path $env:USERPROFILE 'AppData\Local\Android\Sdk' }
$java = 'C:\Program Files\Android\Android Studio\jbr'
if (-not (Test-Path "$sdk\platforms\android-36\android.jar")) { throw "Android SDK platform 36 not found at $sdk" }
$env:JAVA_HOME = $java
$env:PATH = "$java\bin;$env:PATH"
$bt = Join-Path $sdk 'build-tools\36.0.0'
$plat = Join-Path $sdk 'platforms\android-36\android.jar'
$base = Join-Path $app 'app\build\manual'
$null = New-Item -ItemType Directory -Force -Path "$base\classes", "$base\dex", "$base\gen"
Remove-Item "$base\classes\*", "$base\dex\*", "$base\gen\*" -Recurse -Force -ErrorAction SilentlyContinue

& "$java\bin\javac.exe" -source 8 -target 8 -classpath $plat -d "$base\classes" `
  "$app\app\src\main\java\com\creators7\mobile\MainActivity.java" `
  "$app\app\src\main\java\com\creators7\mobile\ShortRenderer.java"
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }

& "$bt\aapt2.exe" compile --dir "$app\app\src\main\res" -o "$base\resources.zip"
if ($LASTEXITCODE -ne 0) { throw 'Android resources compilation failed' }
& "$bt\aapt2.exe" link -I $plat -R "$base\resources.zip" `
  --manifest "$app\app\src\main\AndroidManifest.xml" --java "$base\gen" `
  --auto-add-overlay `
  --min-sdk-version 26 --target-sdk-version 35 --version-code 2 --version-name 0.2.0 `
  --rename-manifest-package com.creators7.mobile -o "$base\app-unsigned.apk"
if ($LASTEXITCODE -ne 0) { throw 'Android packaging failed' }

$classes = Get-ChildItem "$base\classes" -Filter '*.class' -Recurse | ForEach-Object { $_.FullName }
& "$bt\d8.bat" --lib $plat --min-api 26 --output "$base\dex" $classes
if ($LASTEXITCODE -ne 0) { throw 'DEX conversion failed' }
& "$java\bin\jar.exe" uf "$base\app-unsigned.apk" -C "$base\dex" classes.dex
if ($LASTEXITCODE -ne 0) { throw 'DEX packaging failed' }

$keystore = "$base\creator-s7-test.jks"
if (-not (Test-Path $keystore)) {
  & "$java\bin\keytool.exe" -genkeypair -keystore $keystore -storepass android -keypass android `
    -alias s7test -dname 'CN=Creator S7 Test,O=Creator S7,C=US' -keyalg RSA -keysize 2048 -validity 10000
  if ($LASTEXITCODE -ne 0) { throw 'Test signing key creation failed' }
}
& "$bt\zipalign.exe" -f 4 "$base\app-unsigned.apk" "$base\app-aligned.apk"
if ($LASTEXITCODE -ne 0) { throw 'APK alignment failed' }
$out = Join-Path $repo 'CreatorS7-test-v0.2.apk'
& "$bt\apksigner.bat" sign --ks $keystore --ks-pass pass:android --key-pass pass:android --out $out "$base\app-aligned.apk"
if ($LASTEXITCODE -ne 0) { throw 'APK signing failed' }
& "$bt\apksigner.bat" verify --verbose $out
if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed' }
Write-Output "APK ready: $out"
