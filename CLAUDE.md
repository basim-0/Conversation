# Conversation — KMP app (shared logic, native UI per platform)

- Kotlin Multiplatform for everything **below the UI**: domain, data, persistence, networking, DI and MVI ViewModels are shared in `commonMain`. Package root: `org.talkdesk.conversation`.
- **UI is native per platform — never shared:**
  - Android: **Jetpack Compose** (androidx, Compose BOM) in Android-only modules (`feature/<name>/android`, `core:designsystem`, `core:navigation`, `androidApp`). No Compose Multiplatform, no Compose in any KMP module.
  - iOS: **SwiftUI** in `iosApp/iosApp/` (`Features/<Name>/`, `DesignSystem/`, `Navigation/`), consuming the shared ViewModels through the `Shared` framework (SKIE for Flow/sealed/suspend interop).
- Target layout: `core/*` + `feature/<name>/api|impl|android`, shared composition root + iOS framework in `:shared`, Android composition root in `androidApp`, convention plugins in `build-logic`.
- **Current state:** still the wizard template (`sharedLogic`/`sharedUI`); `build-logic`, `core/*`, `shared` and any feature do not exist yet. Never assume a class/task named in a skill exists — look first, and create what's missing per the matching skill.
- **Single environment**: no STG/QA/PROD, flavors or environment enum. Base URL from build config; logging only in debug builds.
- Presentation is **MVI**: shared `MviViewModel` + data-class `State` + `onIntent(Intent)` + `Effect` flow; each platform renders `State` and sends `Intent`s. **Never use Circuit.**
- Navigation: routes are `@Serializable` `NavKey` data in `feature:*:api` (shared). Android: Navigation 3 `NavDisplay`. iOS: SwiftUI `NavigationStack` driven by the same routes.
- DI: Koin. Network: Ktor (`core:network`). Persistence: Room (`core:database`) + DataStore (`core:datastore`, settings only). No SQLDelight.
- Dependency rule: `ui → domain ← data`; domain is pure Kotlin. No `impl → impl` / `android → android` between features.
- **Offline-first**: the local DB is the single source of truth. UI reads only `Flow`s from it; API calls only write into it. No cache policies.
- Errors: `resultCatching`/`dataCatching`, never `runCatching`/`mapCatching`. Typed `AppError`; user text per platform (Android string resources, iOS String Catalog).
- In KMP modules everything lives in `commonMain`; `androidMain`/`iosMain` hold only thin expect/actual and platform glue.
- Versions only in `gradle/libs.versions.toml`. Build config lives in `build-logic` convention plugins (`conversation.*`); module build files hold only plugin aliases + dependencies.
- Verify: `./gradlew check` (guards + tests), `./gradlew :androidApp:assembleDebug`, and for iOS `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build`.

Skills under `.claude/skills/` (`conversation-*`) — load only the one matching the task:
module-structure · build-logic · domain-layer · data-layer · mvi-presentation · navigation · di-composition-root · persistence · testing · new-feature · compose-screen (Android UI) · swiftui-screen (iOS UI) · design-verification (compare a rendered screen to a design).
