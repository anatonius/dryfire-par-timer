# AGENTS.md — working on Dry-Fire Par Timer

## Environment

No system Java/Android SDK. Toolchain is user-space on this machine:

```bash
export JAVA_HOME=~/Android/jdk17
export PATH=~/Android/jdk17/bin:~/Android/gradle/gradle-8.7/bin:~/Android/Sdk/cmdline-tools/latest/bin:~/Android/Sdk/platform-tools:$PATH
export ANDROID_HOME=~/Android/Sdk ANDROID_SDK_ROOT=~/Android/Sdk
```

There is no Gradle wrapper in the repo — invoke `gradle` (8.7) directly.
`local.properties` (`sdk.dir`) is machine-specific and untracked-friendly;
do not hardcode SDK paths in Gradle files.

## Build / test

```bash
gradle :app:assembleDebug :app:testDebugUnitTest --console=plain
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Unit tests must stay green
(`TimerConfigTest`). There is no emulator system image by default; prefer
`adb install -r` on a physical phone for smoke tests.

## Conventions

- Kotlin + Compose Material 3, MVVM. Keep screens in `MainActivity.kt` unless
  a screen outgrows it — then split by screen, not by layer.
- Theme: everything visual goes through `ui/Theme.kt` (`DryFireTheme`,
  `DryFireColors`). Dark surfaces + amber accent; rounded cards/pills.
- UI strings: sentence case only (Standby!, Go!, Preparation…, Done!).
  Never all-caps except the top-bar title style.
- Timer settings are text boxes (no sliders) and persist per drill via
  `DrillRepository.saveTimer()` on START.
- Room is on `fallbackToDestructiveMigration`; bump `version` in
  `DrillDatabase.kt` on entity changes. Keep `sortOrder` seeding in plan
  order (see `DefaultDrills.all`).
- `ParTimerEngine` stays Android-free (default clock is
  `System.currentTimeMillis`) so logic remains unit-testable. Audio
  (`BeepPlayer`, `StandbySpeaker`) stays in the UI layer.
- Verify with a real `gradle` build + tests before claiming done.
