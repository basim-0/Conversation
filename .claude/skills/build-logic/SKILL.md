---
name: build-logic
description: Rules for changing any Gradle build in the Conversation KMP app — module build.gradle.kts files, build-logic convention plugins (conversation.*), gradle/libs.versions.toml, new dependencies, SDK/JVM/compiler/compose/packaging/buildType config. Use this skill BEFORE editing or creating any build.gradle.kts, adding or bumping a dependency or plugin, creating a new module, or touching settings.gradle.kts, even for a "small" one-line change, because the default answer here is that shared configuration belongs in a convention plugin, not in the module.
---

# build-logic: where does this Gradle change belong?

This project keeps build configuration in convention plugins (`build-logic/convention`, ids `conversation.*`) so every module of the same kind is configured identically. Module `build.gradle.kts` files are meant to be nearly empty: plugin aliases plus the dependencies that are unique to that module. When config leaks into a module it gets copy-pasted, drifts, and has to be fixed in N places. So the question to ask for every change is: **would every other module that uses this plugin want this too?**

## Step 1 — Read before you edit

1. Read `build-logic/convention/src/main/kotlin/` (plugin classes + `org/talkdesk/conversation/buildlogic/` helpers) and `gradle/libs.versions.toml`. Don't rely on memory of what the plugins do; they change.
2. Identify which plugin(s) the target module applies. Current set: `kmp.library`, `kmp.feature.api`, `kmp.feature.impl`, `android.library`, `android.library.compose`, `android.feature`, `android.application`, `android.application.compose`.

## Step 2 — Classify the change

| The change is... | It goes in... |
|---|---|
| Needed by every module using plugin X (SDK levels, JVM target, compiler flags, compose, packaging excludes, build types, test deps, namespace/applicationId defaults) | The plugin (or a helper in `buildlogic/` if several plugins share it) |
| Needed by one *category* of module but no plugin covers it | A new convention plugin — propose it, don't inline it |
| Genuinely specific to one module (its own `applicationId` override, its framework `baseName`, a dependency only it uses, a `project(":...")` dependency) | That module's `build.gradle.kts` |
| A version, library or plugin coordinate | `gradle/libs.versions.toml`, always — never a literal in a build file or plugin |

Default to the plugin when in doubt, and say why in one sentence.

## Step 3 — Ask before changing shared config

Changing a plugin affects every module that applies it, and adding a dependency to a plugin adds it to all of them. So when the right home is a plugin, **stop and ask the user** before editing it. Keep the question short and concrete:

> "`foo` would be useful in every `conversation.kmp.feature.impl` module, so I'd add it to that plugin instead of this module. OK to update the plugin?"

If the change is clearly module-specific, don't ask — just put it in the module and say it is module-specific. For a new dependency, ask the same question: is it needed by all modules of a kind (plugin), or only this one (module)? Either way the coordinate itself goes in the catalog first.

## Step 4 — Make the change

- Catalog first (version, library, plugin alias), then the plugin or module.
- Plugin dependencies are injected via `libs.findLibrary("alias-name")`; the alias uses dashes in the lookup (`androidx-compose-ui`).
- Android UI deps stay androidx + Compose BOM (no Compose Multiplatform). KMP modules hold no UI.
- Don't add project dependencies (`project(":core:...")`) to plugins; modules own their wiring.
- A new plugin needs: the class in `build-logic/convention/src/main/kotlin/`, a `register(...)` entry in `convention/build.gradle.kts`, and an alias in the catalog `[plugins]` (parent aliases that have children need `.asProvider()` in the register block).
- Before a module overrides something a plugin sets, consider whether the plugin default is wrong instead.

## Step 5 — Verify and report

Run the narrowest tasks that exercise the change (e.g. `./gradlew :<module>:assembleDebug`, `:<module>:compileKotlinIosSimulatorArm64`, `:<module>:testAndroidHostTest`, then `./gradlew check` for plugin changes since they touch many modules). Report what moved into the plugin, what stayed in the module and why, and which modules are affected.

## Red flags

- A module build file with `android { ... }`, `compileOptions`, `kotlin { compilerOptions }`, `buildFeatures`, `packaging`, or repeated `listOf(iosArm64(), ...)` blocks.
- A dependency version written inline, or a library added to a module that three sibling modules will also need.
- `catalog` aliases duplicating an existing one under another name (Gradle also rejects alias names that normalize to the same accessor).
