# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PinDay is a Kotlin Multiplatform (KMP) project targeting Android and iOS with shared Compose Multiplatform UI. Package: `com.dino.pinday`.

## Build Commands

```bash
# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Run tests
./gradlew :composeApp:test

# Build iOS — open iosApp/ in Xcode and run from there
```

No linting tool is configured. Kotlin code style is set to "official" in gradle.properties.

## Architecture

- **Single module**: `composeApp` contains all shared and platform-specific code
- **Shared UI**: Compose Multiplatform renders the same UI on both platforms
- **Platform abstraction**: Uses Kotlin `expect/actual` pattern (see `Platform.kt`)

### Source Sets

- `commonMain/` — Shared Kotlin + Compose UI code (App.kt is the root composable)
- `androidMain/` — Android entry point (`MainActivity`), platform actuals
- `iosMain/` — iOS entry point (`MainViewController`), platform actuals
- `commonTest/` — Shared tests

### iOS Integration

Swift side (`iosApp/`): `iOSApp` → `ContentView` → `ComposeView` (UIViewControllerRepresentable) bridges to Kotlin's `MainViewController`.

## Key Config

- **Version catalog**: `gradle/libs.versions.toml` — all dependency versions managed here
- **Kotlin 2.3.0**, Compose Multiplatform 1.10.0, AGP 8.11.2
- **Android**: minSdk 24, targetSdk 36, compileSdk 36
- **Gradle**: Configuration cache and build caching enabled
