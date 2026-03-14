# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PinDay is a Korean anniversary/D-day tracking app built with Kotlin Multiplatform (KMP) + Compose Multiplatform. Features lunar-solar calendar conversion for Korean lunar birthdays. Package: `com.dino.pinday`.

## Build Commands

```bash
# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Run tests
./gradlew :composeApp:testDebugUnitTest

# Build iOS — open iosApp/ in Xcode and run from there
```

No linting tool is configured. Kotlin code style is set to "official" in gradle.properties.

## Architecture

- **Single module**: `composeApp` — MVVM with Koin DI
- **Shared UI**: Compose Multiplatform renders the same UI on both platforms
- **DB**: SQLDelight with `expect/actual` DriverFactory per platform
- **Navigation**: Type-safe routes with `kotlinx.serialization` + Jetpack Navigation Compose

### Key Layers

```
domain/model/        — Anniversary, Category, CountingType (enums)
domain/usecase/      — LunarSolarConverter, CalculateDDayUseCase, GetMilestonesUseCase
data/db/             — SQLDelight schema (Anniversary.sq), DriverFactory (expect/actual)
data/repository/     — AnniversaryRepository (Flow-based)
data/mapper/         — DB entity ↔ domain model mapping
ui/navigation/       — PinDayNavGraph with routes: Onboarding → Home → AddEdit / Detail
ui/{home,add,detail,onboarding}/ — Screen + ViewModel per feature
di/                  — Koin modules (appModule + platformModule)
```

### DI & Entry Points

- **Android**: `PinDayApplication` starts Koin → `MainActivity` → `App()`
- **iOS**: `initKoin()` called from Swift → `MainViewController()` → `App()`

### iOS Integration

Swift side (`iosApp/`): `iOSApp` → `ContentView` → `ComposeView` (UIViewControllerRepresentable) bridges to Kotlin's `MainViewController`. Must call `initKoin()` before creating the view controller.

## Key Config

- **Version catalog**: `gradle/libs.versions.toml` — all dependency versions managed here
- **Kotlin 2.3.0**, Compose Multiplatform 1.10.0, AGP 8.11.2
- **Android**: minSdk 24, targetSdk 36, compileSdk 36
- **Gradle**: Build caching enabled, configuration cache disabled (SQLDelight compatibility)
- **SQLDelight**: Schema in `commonMain/sqldelight/`, generates `PinDayDatabase`
