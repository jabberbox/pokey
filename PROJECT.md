# Project: GLP-1 Shot Tracker (Light Phone III Tool)

## What this is

A personal tool for the Light Phone III, built on the Light SDK (this repo). It tracks weekly GLP-1 injections. Single user, fully local, no accounts, no network, no sync. Health data never leaves the device.

## Environment notes

- Developer is on Windows 11, using PowerShell. Adapt any shell commands accordingly.
- JAVA_HOME must point to JDK 17 (Temurin, at `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`). Android Studio's bundled JBR is too new for the plugin toolchain.
- `local.properties` already contains working `gpr.user` / `gpr.key` GitHub Packages credentials. `.\gradlew.bat tasks` completes with BUILD SUCCESSFUL.
- The developer is new to mobile development. Explain what you're doing as you go, one step at a time. Keep changes small and verify each one builds before moving on.

## SDK conventions (follow these)

- Read the repo README and any docs before writing code.
- Kotlin, Jetpack Compose, Coroutines, MVVM, matching the scaffold's existing patterns.
- New screens are Screen/ViewModel pairs extending LightScreen / LightScreenViewModel, following whatever HomeScreen / HomeScreenViewModel demonstrate.
- Stay within androidx / first-party libraries. Light restricts allowed APIs and third-party libraries; when unsure whether an API is permitted, flag it rather than assuming.
- Match the Light ethos: minimal, quiet, monochrome-friendly UI. Use the SDK's UX/UI library where it exists. No decorative color, no animation flourishes.
- `git pull` this repo frequently; the SDK is days old and changing fast. Keep the tool's code cleanly separated so SDK updates merge without conflict.

## Features

### 1. Shot logging (build first)

- Log a shot: date (default today, editable) and injection site chosen from a fixed rotation list (left abdomen, right abdomen, left thigh, right thigh, back of left arm, back of right arm).
- Home screen shows: date of last shot, days since, days until next shot (7-day cycle), and the last injection site used (to support site rotation).
- History screen: reverse-chronological list of all shots.

### 2. Weight logging (build second)

- Log a weight entry: value in lbs (one decimal), date (default today, editable).
- Weight entries are independent of shot entries but shown together in history.
- Home screen shows most recent weight.

### 3. 7-day reminder (build third)

- After each logged shot, schedule a local notification for 7 days later ("Shot day"). Schedule follows the actual last shot, not a fixed weekday.
- If a shot is logged early or late, the next reminder reschedules from the new shot date.
- Use standard Android scheduling (WorkManager or AlarmManager) unless SDK docs specify a Light-provided mechanism; prefer the Light mechanism if one exists.
- Note: notifications require the LightOS emulator installed as a system app (see prerequisites below) or real hardware. Build and unit-test the scheduling logic first; verify delivery on the system-app emulator.

### 4. Progress photos (build last, may be cut)

- Optionally attach a photo to a shot log entry, viewable from history.
- Schema should include a nullable photo path field from day one even though the feature ships last.
- Camera/media access may be restricted by Light's API allowlist. Investigate what the SDK permits before building. If blocked, ship without it; do not hack around restrictions.

## Data layer

- Room (androidx) with local SQLite storage.
- Entities: ShotEntry (id, timestamp, site, photoPath nullable), WeightEntry (id, timestamp, weightLbs).
- No cloud, no analytics, no exports for v1. (A local export could be a future addition.)

## Build order and checkpoints

0. **Prerequisites** (do these before feature work):
   - Verify the gradle build is green.
   - Confirm an AVD exists: API 34, AOSP image (NOT Play Store), x86_64, 1080x1240 3.92" display. Create via command line or walk the developer through Device Manager if missing.
   - Install the LightOS emulator as a system app per the repo's emulator doc (platform test key signing, priv-app push, verify uid=1000). Adapt all commands for Windows PowerShell. curl and keytool are available; openssl is bundled with Git for Windows if not on PATH.
   - Create a `launch-emulator.ps1` script that always boots the AVD with `-writable-system`.
   - Build and install the scaffold's sample tool on the emulator to prove the full pipeline before any custom code.
1. Data layer + shot logging + home screen. Checkpoint: log a shot in the emulator, see it reflected on home screen after app restart (persistence proven).
2. Weight logging + history screen. Checkpoint: mixed history displays correctly.
3. Reminder scheduling. Checkpoint: notification fires on the system-app emulator (use a short test interval like 2 minutes during development, then set to 7 days).
4. Photos, only after confirming API permissions.

## Out of scope for v1

Dose amounts/titration tracking, charts or trends, multiple medications, data export, any networking.
