# AGENTS.md

Welcome! This is the entry point for all agentic development in this codebase. It defines the coding standards, environment guidelines, build/test commands, and indexes all project-specific modules.

---

## 1. Core Behavioral Conventions

These guidelines reduce common LLM coding mistakes and ensure maintainable development.

### Think Before Coding
* **Don't assume. Don't hide confusion. Surface tradeoffs.**
* Before implementing:
  * State your assumptions explicitly. If uncertain, ask.
  * If multiple interpretations exist, present them — do not pick silently.
  * If a simpler approach exists, say so. Push back when warranted.
  * If something is unclear, stop. Name what's confusing and ask.

### Simplicity First
* **Minimum code that solves the problem. Nothing speculative.**
* No features beyond what was asked.
* No abstractions for single-use code.
* No "flexibility" or "configurability" that wasn't requested.
* No error handling for impossible scenarios.
* If you write 200 lines and it could be 50, rewrite it.

### Surgical Changes
* **Touch only what you must. Clean up only your own mess.**
* When editing existing code:
  * Don't "improve" adjacent code, comments, or formatting.
  * Don't refactor things that aren't broken.
  * Match existing style, even if you'd write it differently.

### Match Style & Architecture
* Match coding patterns, variable names, and file organization of the surrounding code.
* Adhere to the established hexagonal architecture boundaries (Domain, Ports, Adapters).

---

## 2. Technical Specifications & Constraints

* **Architecture:** Hexagonal (Ports and Adapters) mapping.
  * **Domain Layer:** Pure Kotlin. No framework dependencies (no Android SDK, no Room, no Google API Client library).
  * **Ports (Repository Interfaces):** Define the database cache and Google Drive access interface contracts.
  * **Adapters (Infrastructure):** Concrete implementations (Room database, Google Drive SDK, Jetpack Compose UI).
* **Language/Formatting:** Everything in English. Clean Compose structure.
* **No Monolithic Files:** ~400 lines max per file (excluding generated/config files). Keep components and classes focused.
* **Unit Tests:** Must test at least the domain/core layer, runnable locally on JVM without an emulator or special hardware. The test suite must pass before any commit.
* **Pinned Runtimes:** OpenJDK 21, Gradle 8.7, Android Gradle Plugin 8.3.2, Kotlin 1.9.22.
* **Security & Git:** `.gitignore` must be strictly respected. Never commit build outputs, IDE config files, or secrets.
* **Environment AAPT2 Override:** On native `aarch64` Termux PRoot environments, Gradle is configured via `gradle.properties` to override `aapt2` with the native binary:
  `android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2`

---

## 3. Build, Test, & Lint Commands

Always run these commands inside the root directory `/data/data/com.termux/files/home/Projects/AndroidApp/GoogleDriveAnalyser`.

| Action | Command | Description |
| :--- | :--- | :--- |
| **Build Debug APK** | `./gradlew assembleDebug` | Compiles and builds the debug installable APK. |
| **Run Unit Tests** | `./gradlew test` | Executes unit tests under `app/src/test`. |
| **Run Lint** | `./gradlew lintDebug` | Runs Android Lint check on debug source files. |
| **Clean Project** | `./gradlew clean` | Cleans build directory and cache. |

---

## 4. Documentation Maintenance Rule

* **You must keep all documentation up to date in the same change as any code modification.**
* Touching or modifying a module requires updating its corresponding detailed markdown file in the `AGENTS/` directory.
* Updating, adding, or removing an MD file requires updating the index in `AGENTS/INDEX.md`.

---

## 5. Module Documentation Directory

All detailed documentation for specific modules, libraries, or architectural layers is indexed in `AGENTS/INDEX.md`. 
For example:
* Documentation for the Domain architecture is in [domain.md](file:///data/data/com.termux/files/home/Projects/AndroidApp/GoogleDriveAnalyser/AGENTS/domain.md).
* Documentation for the Database cache architecture is in [database.md](file:///data/data/com.termux/files/home/Projects/AndroidApp/GoogleDriveAnalyser/AGENTS/database.md).

For the complete list, read [AGENTS/INDEX.md](file:///data/data/com.termux/files/home/Projects/AndroidApp/GoogleDriveAnalyser/AGENTS/INDEX.md).
