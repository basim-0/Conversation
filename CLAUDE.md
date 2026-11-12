# Conversation — KMP app (shared logic, native UI per platform)

- Kotlin Multiplatform for everything **below the UI**: domain, data, persistence, networking, DI and MVI ViewModels are shared in `commonMain`. Package root: `org.talkdesk.conversation`.
- **UI is native per platform — never shared:**
  - Android: **Jetpack Compose** (androidx, Compose BOM) in Android-only modules (`<name>/android`, `core:designsystem`, `core:navigation`, `androidApp`). No Compose Multiplatform, no Compose in any KMP module.
  - iOS: **SwiftUI** in `iosApp/iosApp/` (`<Name>/`, `DesignSystem/`, `Navigation/`), consuming the shared ViewModels through the `ConversationKit` framework (SKIE for Flow/sealed/suspend interop).
- Target layout: `core/*` + one top-level folder per feature (`<name>/api|impl|android`, e.g. `auth/`), shared composition root + iOS umbrella framework in `:conversation-kit`, Android composition root in `androidApp`, convention plugins in `build-logic`.
- **Current state:** `build-logic` exists; the app modules are still the wizard template (`sharedLogic` builds the `SharedLogic` framework that iOS imports, plus `sharedUI`). `core/*`, `conversation-kit` and any feature do not exist yet. Never assume a class, task or module exists: look first. If something is missing, say so and propose it; don't silently create it.
- **Single environment**: no STG/QA/PROD, flavors or environment enum. Base URL from build config; logging only in debug builds.
- Presentation is **MVI**: shared `MviViewModel` + data-class `State` + `onIntent(Intent)` + `Effect` flow; each platform renders `State` and sends `Intent`s. **Never use Circuit.**
- Navigation: routes are `@Serializable` `NavKey` data in `<name>:api` (shared). Android: Navigation 3 `NavDisplay`. iOS: SwiftUI `NavigationStack` driven by the same routes.
- DI: Koin. Network: Ktor (`core:network`). Persistence: Room (`core:database`) + DataStore (`core:datastore`, settings only). No SQLDelight.
- Dependency rule: `ui → domain ← data`; domain is pure Kotlin. No `impl → impl` / `android → android` between features.
- **Offline-first**: the local DB is the single source of truth. UI reads only `Flow`s from it; API calls only write into it. No cache policies.
- Errors: `resultCatching`/`dataCatching`, never `runCatching`/`mapCatching`. Typed `AppError`; user text per platform (Android string resources, iOS String Catalog).
- In KMP modules everything lives in `commonMain`; `androidMain`/`iosMain` hold only thin expect/actual and platform glue.
- Versions only in `gradle/libs.versions.toml`. Build config lives in `build-logic` convention plugins (`conversation.*`); module build files hold only plugin aliases + dependencies.
- Verify: `./gradlew check` (guards + tests), `./gradlew :androidApp:assembleDebug`, and for iOS `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build`.

## KMP architecture

### MVI state
- A ViewModel exposes its `State` as one `StateFlow`. `State` holds only data: no Compose or SwiftUI types, colors or string resources.

### What goes in each module
- `<name>/api` (KMP): `commonMain` only. Public interfaces, shared models and `NavKey` routes. Keep it small, with no implementations.
- `<name>/impl` (KMP):
  - `commonMain`: ViewModels, `State`/`Intent`/`Effect`, use cases, repository implementations.
  - `androidMain`: Android-only implementations of interfaces. Never Compose.
  - `iosMain`: only when Kotlin code needs an iOS API. SwiftUI screens live in `iosApp/`, never in `iosMain`.
- `<name>/android` (Android-only): Compose screens that render the feature's ViewModel.
- Only create source set folders that contain code.
- Compiled module names must be unique (many modules are named `api`/`impl`, which clashes in `.kotlin_module` on Android and in the klib `unique_name` on iOS). TODO: the convention plugins give each module a unique Android namespace but don't set the Kotlin module name or klib unique name. Flag this instead of adding a per-module workaround.

### Dependency rules
- `impl` may depend on its own `api`, other features' `api`, and `core` modules.
- `android` may depend on its own `impl`, other features' `api`, and Android `core` modules.
- Only `androidApp`, `:conversation-kit` and a feature's own `android` module depend on an `impl`. `androidApp` and `:conversation-kit` wire implementations to interfaces.

### Where platform code goes
- Used by more than one feature: a `core` module (`androidMain` + `iosMain`).
- Small and dependency-free: expect/actual.
- Needed by only one feature's ViewModel: an interface in that feature's `impl/commonMain`, with implementations in its `androidMain`/`iosMain`. Move it to `core` as soon as a second feature needs it.
- Swift-only or much simpler in Swift (CallKit, PushKit, AVAudioSession, async APIs): declare a Kotlin interface, implement it in Swift inside `iosApp/`, and inject it at app startup. Prefer callbacks over `suspend` in interfaces that Swift implements.
- Needed only by the UI (haptics, share sheet, opening settings): put it directly in Compose or SwiftUI.

### iOS umbrella framework
- iOS consumes exactly ONE Kotlin framework, `ConversationKit`, produced by `:conversation-kit` (`conversation-kit/`). Swift uses `import ConversationKit`. Until it exists, `:sharedLogic` produces the only framework (`SharedLogic`).
- Never produce a framework per feature or per core module. That duplicates the Kotlin runtime and creates incompatible copies of the same types in Swift.
- `export()` only the modules whose classes Swift uses by name: ViewModels, `State`/`Intent`/`Effect`, models, and interfaces Swift implements. Every exported module must be an `api(...)` dependency. Everything else stays `implementation(...)`.
- When adding a feature, also add it to `:conversation-kit`'s dependencies, export it if Swift needs it, and register its ViewModels in the iOS wiring.
- iOS does not clear ViewModels automatically. Every SwiftUI screen must own a ViewModel scope that clears its ViewModelStore on deinit. TODO: no pattern exists yet; propose one in `:conversation-kit` the first time it's needed.

### Build logic
- Use the existing convention plugins (`build-logic/convention`) for every module:
  - KMP library (`core/*`, `:conversation-kit`): `conversation.kmp.library`
  - Feature api: `conversation.kmp.feature.api`
  - Feature impl: `conversation.kmp.feature.impl`
  - Feature android UI: `conversation.android.feature`
  - Android-only library: `conversation.android.library` / `conversation.android.library.compose`
  - Android application: `conversation.android.application.compose` (builds on `conversation.android.application`)
- Don't hand-write `kotlin {}` or `android {}` configuration that a convention plugin already provides.
- Don't create new convention plugins or change existing ones without asking. If no plugin fits, explain what's missing and propose a change.

### Choosing libraries and solutions
Go through these in order before writing a feature or picking a dependency:
1. Official Google/AndroidX/Jetpack libraries (ViewModel, Lifecycle, Room, DataStore, Navigation, Paging, ...) and JetBrains/Kotlin libraries (kotlinx.coroutines, kotlinx.serialization, kotlinx.datetime, Ktor, ...). Check the current docs for KMP support, because it changes often.
2. A mature, widely used community KMP library, only when no official option fits. Say why the official options don't work.
3. A custom implementation, only as a last resort. Explain why options 1 and 2 don't fit.
- Check `libs.versions.toml` for an existing equivalent first.
- Ask before adding any new dependency.

## Skills
Skills under `.claude/skills/`. Load only the one matching the task: `build-logic`.
