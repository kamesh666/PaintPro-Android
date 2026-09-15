# PaintPro Android (MVP)

A native Android companion to PaintPro-Web, built with Kotlin + Jetpack Compose. This is the
**Core MVP**: sign up / sign in, manage sites, manage labour, and mark daily attendance, all
syncing to the same Supabase backend PaintPro-Web already uses.

## Status: not yet opened in Android Studio

Every file in this project was hand-written against researched, current (Sept 2026) library
docs and source - **none of it has been compiled**. The sandbox this was built in has no Android
SDK and can't reach `dl.google.com` or `services.gradle.org`, so there was no way to run a Gradle
sync or build here. The Gradle wrapper itself is genuine (its jar was extracted from a real local
Gradle 8.14.3 install, not fabricated), so the project should sync normally in Android Studio -
but treat the first build as the real first compile of this code, and expect to fix a handful of
small issues (an import path, an API shape that shifted between library versions, etc.) rather
than an app that runs perfectly on the very first try.

## Getting started

1. Open this folder in a recent Android Studio (2024.x "Ladybird" or newer recommended).
2. Let Gradle sync - it will download AGP 8.13.0, Gradle 8.13, and all dependencies listed below.
3. Run the `app` configuration on an emulator or device with **API 26+**.
4. Sign up with any email/password. By default the app talks to a shared demo Supabase project
   (see below) - the tables must exist there, matching PaintPro-Web's schema (see "Backend" below).

If your Android Studio's bundled JDK is older than 17, point Gradle at a JDK 17+ install in
**Settings → Build Tools → Gradle → Gradle JDK**.

## Tech stack & exact versions

| Library | Version | Notes |
|---|---|---|
| Android Gradle Plugin | 8.13.0 | Chosen over AGP 9.x to stay compatible with older Android Studio installs |
| Gradle | 8.13 | |
| Kotlin | 2.4.20 | |
| Compose compiler | (= Kotlin version) | via `org.jetbrains.kotlin.plugin.compose`, not a separate artifact |
| Compose BOM | 2026.08.00 | |
| Navigation Compose | 2.10.1 | |
| Room | 2.8.5 | via KSP, `androidx.room` group (not the `androidx.room3` fork) |
| KSP | 2.3.12 | |
| kotlinx-serialization-json | 1.11.0 | |
| supabase-kt | 3.8.0 (BOM) | `postgrest-kt`, `auth-kt`; package `io.github.jan.supabase` |
| Ktor engine | ktor-client-okhttp 3.5.2 | supabase-kt needs an engine added explicitly |
| compileSdk / targetSdk | 36 | |
| minSdk | 26 | floor required by supabase-kt |

No dependency-injection framework (Hilt/Dagger) is used - `di/AppContainer.kt` is a tiny
hand-rolled service locator, which is simpler and one less thing that can go wrong given no build
verification was possible.

## Architecture

- **UI**: Jetpack Compose, Material 3, one `NavHost` (`ui/navigation/PaintProNavHost.kt`) driving
  everything. Screens read/write through `AppContainer`'s repositories directly (no ViewModels for
  simple list/form screens - `AuthViewModel` is the one screen with enough async state to earn one).
- **Local data**: Room (`data/local/`). Four entities - `ProfileEntity`, `SiteEntity`,
  `LabourEntity`, `AttendanceEntity` - deliberately trimmed to the MVP's needs versus the full
  web-app schema (see comments in each entity file for exactly what was left out and why it's
  safe to leave out).
- **Remote data**: supabase-kt (`data/remote/`). `SupabaseProvider` holds the single
  `SupabaseClient`; `SupabaseConfigStore` implements "bring your own Supabase" (see below).
- **Sync model - the one deliberate improvement over PaintPro-Web**: every write to Site, Labour,
  or Attendance lands in Room *immediately*, tagged `PENDING_UPSERT` (see
  `data/repository/SyncState.kt`). The repository then tries to push it to Supabase in the
  background; on success the row flips to `SYNCED`, on failure (offline, server error) it's simply
  left pending. `syncPending()` retries all pending rows and is called whenever a screen loads.
  This means **nothing typed while offline is ever silently lost** - a real gap in the web app's
  current fire-and-forget dual-write, per its own analysis doc.
- **Auth**: Supabase Auth (email/password) via `AuthRepository`. There's no team-invite flow in
  this v1 - whoever signs up becomes the sole owner of a brand-new org, and `org_id` is simply set
  to that user's own auth id. If PaintPro-Web's actual org/invite model differs from this
  assumption, `AuthRepository.signUp()` is the one place to change.

## "Bring your own Supabase"

Like PaintPro-Web, the app ships with a working default Supabase project baked into
`BuildConfig` (`app/build.gradle.kts`). A user can override it from **Settings** with their own
project's URL + anon/publishable key; the override is stored in plain `SharedPreferences`
(`data/remote/SupabaseConfigStore.kt`) and takes effect after signing out and back in.

## Backend / Supabase schema this app expects

The app reads/writes four tables that must already exist (PaintPro-Web already created them):
`profiles`, `sites`, `labours`, `attendance`, each with an `org_id` column and Row Level Security
scoping rows to that org, matching what PaintPro-Web's own analysis doc describes. The Android
entities only use a subset of each table's columns - see the KDoc comment at the top of each file
in `data/local/entity/` for exactly which newer web-only columns were intentionally left out (they
are nullable on the real table, so Android's inserts don't touch or break them).

## What's not built yet (beyond MVP scope)

Everything else in PaintPro-Web's feature set - warranty tracking, the lead pipeline, public
share links, GPS-tagged attendance, stages, and the 6 features added in the most recent web
roadmap pass - is intentionally out of scope for this first Android build. The data layer was
written so those can be added later without breaking existing rows (see the entity comments).

## Known risk

Because nothing here has compiled, the highest-risk areas if something doesn't build are:
supabase-kt's exact Kotlin API surface (verified via source, but a 3.8.0-specific detail could
still have shifted), and Compose Material3 API names for anything less common than a `Button` or
`TextField` (`SegmentedButton`, `FlowRow`). If Android Studio flags an unresolved reference in one
of those areas, check that library's release notes for the exact version pinned in
`gradle/libs.versions.toml` first.
