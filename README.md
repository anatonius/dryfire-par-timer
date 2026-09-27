# Dry-Fire Par Timer

A par-time timer for dry-fire practice. Configurable par time, random start delay,
configurable preparation time between reps, series mode with decreasing par times,
shot-timer style buzzer, spoken "Stand by!" cue, and per-drill screens with
editable drills persisted in Room.

## Features

- 6 built-in drills (Trigger Control at Speed, Fast Draw, Emergency Reload,
  Target Transition, Strong-Hand Only, El Presidente) — fully editable,
  deletable, with reset-to-defaults
- Per-drill screen: title, description, timer settings (all text boxes)
- Random start delay (configurable min/max), preparation time between reps
- Series mode: set number of steps, start/end time, reps per step —
  the app extrapolates intermediate par times
- Timer settings are saved per drill when a drill starts
- Shot-timer style buzzer synthesized with AudioTrack (configurable tone,
  length, volume); double-buzz on drill end
- Spoken "Stand by!" (male-urgent TTS voice) at each rep, with warning-buzz
  fallback when TTS is unavailable
- Dark theme with amber accents

## Tech

Kotlin, Jetpack Compose (Material 3), MVVM, Room, DataStore, Navigation Compose,
Coroutines. Min SDK 26, target/compile SDK 34, Java 17.

## Build

Toolchain lives outside the repo (user-space install):

```bash
export JAVA_HOME=~/Android/jdk17
export PATH=~/Android/jdk17/bin:~/Android/gradle/gradle-8.7/bin:$PATH
export ANDROID_HOME=~/Android/Sdk ANDROID_SDK_ROOT=~/Android/Sdk
```

Project needs `local.properties` with `sdk.dir` (already present for this machine).

```bash
gradle :app:assembleDebug          # APK -> app/build/outputs/apk/debug/app-debug.apk
gradle :app:testDebugUnitTest      # unit tests
```

## Install on a phone

```bash
export PATH=~/Android/Sdk/platform-tools:$PATH
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Enable USB debugging on the phone first (tap Build number 7x, then allow the
RSA prompt when plugging in).

## Project layout

```
app/src/main/java/com/dryfire/partimer/
  MainActivity.kt        # nav + drill pager, timer setup, run, beep settings
  timer/
    TimerConfig.kt       # TimerConfig, BeepConfig, SeriesStep, buildSeries()
    ParTimerEngine.kt    # rep loop: standby -> start buzz -> par -> stop -> prep
    BeepPlayer.kt        # AudioTrack shot-timer buzzer
    StandbySpeaker.kt    # TTS "Stand by!" with buzz fallback
  drills/
    Drill.kt / DrillEntity.kt / DrillDao.kt / DrillDatabase.kt
    DrillRepository.kt / DrillViewModel.kt / DefaultDrills.kt
  settings/SettingsStore.kt  # DataStore beep persistence
  ui/Theme.kt            # dark + amber design system
app/src/test/.../TimerConfigTest.kt
```

## Notes

- Room DB uses `fallbackToDestructiveMigration` (currently v3); schema changes
  wipe and re-seed drills — acceptable while unreleased.
- UI strings use sentence case (Standby!, Go!, Preparation…, Done!).
