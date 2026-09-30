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
- Dark theme with amber accents, logo splash screen (4s), logo launcher icon
- Activity tracker: GitHub-style 90-day heatmap (weeks Sun–Sat, month labels),
  color scales with daily reps (10+ to show, 200+ full amber), tap-a-day detail,
  scrolling summary of active days only with per-drill rep counts

## Built-in drill defaults

| # | Drill | Par (s) | Reps | Prep (s) | Delay (s) |
|---|-------|---------|------|----------|-----------|
| 1 | Trigger Control at Speed | 0.4 | 10 | 2 | 2–4 |
| 2 | Fast Draw | 1.5 | 10 | 4 | 2–4 |
| 3 | Emergency Reload | 1.5 | 10 | 6 | 2–4 |
| 4 | Target Transition | 3.5 | 10 | 4 | 2–4 |
| 5 | Strong-Hand Only | 1.5 | 10 | 4 | 2–4 |
| 6 | El Presidente (Dry) | 8 | 10 | 5 | 2–4 |
| + | New drill | 2 | 10 | 4 | 2–4 |

Drill descriptions are capped at 180 characters (`MAX_DESCRIPTION_LENGTH`)
so they fit the 4-row box on the drill screen; enforced in the add/edit
dialog with a character counter.

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
  MainActivity.kt        # nav + splash, drill pager, timer setup, run, beep settings
  timer/
    TimerConfig.kt       # TimerConfig, BeepConfig, SeriesStep, buildSeries()
    ParTimerEngine.kt    # rep loop: standby -> start buzz -> par -> stop -> prep
    BeepPlayer.kt        # AudioTrack shot-timer buzzer
    StandbySpeaker.kt    # TTS "Stand by!" with buzz fallback
  drills/
    Drill.kt / DrillEntity.kt / DrillDao.kt / DrillDatabase.kt
    DrillRepository.kt / DrillViewModel.kt / DefaultDrills.kt
  activity/ActivityLog.kt  # run log entity, DAO, level thresholds
  settings/SettingsStore.kt  # DataStore beep persistence
  ui/Theme.kt            # dark + amber design system
  ui/ActivityScreen.kt   # 90-day heatmap + daily summaries
app/src/test/.../TimerConfigTest.kt / ActivityLevelsTest.kt / DefaultDrillsTest.kt
```

## Notes

- Room DB uses `fallbackToDestructiveMigration` (currently v4); schema changes
  wipe drills and activity history — acceptable while unreleased.
- Changing built-in defaults does NOT touch existing installs (seeding only
  runs on empty DB); users pick them up via Menu → Reset defaults.
- UI strings use sentence case (Standby!, Go!, Preparation…, Done!).
