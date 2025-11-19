# CLAUDE.md - AI Assistant Guide for FlightChecks

**Last Updated**: 2025-11-19
**Project**: Taifun Checks - Aviation Checklist App
**Version**: 1.0.00.00 (New versioning system: w.x.yy.zz)
**Type**: Android Native (Kotlin + Jetpack Compose)

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Codebase Structure](#codebase-structure)
3. [Architecture & Patterns](#architecture--patterns)
4. [Key Technologies](#key-technologies)
5. [Development Workflows](#development-workflows)
6. [Code Conventions](#code-conventions)
7. [Build System](#build-system)
8. [Testing Strategy](#testing-strategy)
9. [Common Tasks](#common-tasks)
10. [Critical Paths & Safety](#critical-paths--safety)
11. [Documentation Resources](#documentation-resources)
12. [Troubleshooting Guide](#troubleshooting-guide)

---

## Project Overview

**FlightChecks** (branded as "Taifun Checks") is a professional aviation checklist application designed for aircraft operations. The app provides critical safety functionality for pilots with features including:

- Multi-aircraft YAML checklist management
- Step-by-step and full-list execution modes
- Voice control for hands-free operation (Spanish/English)
- Real-time GPS altitude, QNH calculations, and flight logging
- Dark/light themes optimized for cockpit conditions
- Offline-first architecture (no cloud dependencies)

**Target Users**: Pilots, flight instructors, aviation enthusiasts
**Safety-Critical**: YES - This app is used during actual flight operations

---

## Codebase Structure

### Directory Layout

```
FlightChecks/
├── app/
│   ├── src/main/
│   │   ├── java/com/taifun/checks/
│   │   │   ├── data/              # Data layer (repositories, persistence)
│   │   │   │   ├── ChecklistRepository.kt      (407 lines - multi-file YAML management)
│   │   │   │   ├── SettingsRepository.kt       (89 lines - user preferences)
│   │   │   │   ├── ProgressRepository.kt       (105 lines - checklist progress)
│   │   │   │   ├── SensorDataRepository.kt     (227 lines - GPS, barometer, QNH)
│   │   │   │   ├── LogRepository.kt            (464 lines - flight logging)
│   │   │   │   ├── AerodromeRepository.kt      (144 lines - aerodrome database)
│   │   │   │   └── yaml/
│   │   │   │       └── YamlParser.kt           (YAML parsing with SnakeYAML)
│   │   │   ├── domain/            # Domain models
│   │   │   │   └── models.kt                   (Paso, Checklist, Catalogo)
│   │   │   └── ui/                # Presentation layer
│   │   │       ├── MainActivity.kt             (App entry point)
│   │   │       ├── screens/                    (5,845 lines total)
│   │   │       │   ├── HomeScreen.kt           (428 lines)
│   │   │       │   ├── StepScreen.kt           (1,304 lines - checklist execution)
│   │   │       │   ├── EditChecklistScreen.kt  (892 lines - visual editor)
│   │   │       │   ├── EditorScreen.kt         (303 lines - YAML editor)
│   │   │       │   ├── ChecklistManagerScreen.kt (521 lines)
│   │   │       │   ├── FirstLaunchScreen.kt    (385 lines)
│   │   │       │   ├── SettingsScreen.kt       (312 lines)
│   │   │       │   ├── LogViewerScreen.kt      (557 lines)
│   │   │       │   └── HelpScreen.kt           (1,143 lines)
│   │   │       ├── components/                 (Reusable UI components)
│   │   │       │   └── ColorPickerDialog.kt
│   │   │       ├── navigation/
│   │   │       │   └── AppNavHost.kt           (Navigation setup)
│   │   │       ├── theme/
│   │   │       │   └── TaifunTheme.kt          (Material3 theming)
│   │   │       ├── vm/                         (ViewModels)
│   │   │       │   ├── HomeViewModel.kt
│   │   │       │   └── StepViewModel.kt
│   │   │       ├── HapticUtils.kt
│   │   │       └── IconsRepo.kt                (40+ aviation vector icons)
│   │   ├── assets/
│   │   │   ├── Taifun17E_ES.yaml / Taifun17E_EN.yaml
│   │   │   ├── Grob_G109B_ES.yaml / Grob_G109B_EN.yaml
│   │   │   ├── SF-28A_ES.yaml / SF-28A_EN.yaml
│   │   │   ├── aerodromes.csv                  (491 KB)
│   │   │   └── airports.csv                    (12.4 MB)
│   │   ├── res/
│   │   │   ├── values/strings.xml              (Spanish - default)
│   │   │   ├── values-en/strings.xml           (English)
│   │   │   ├── drawable/                       (Icons, logos)
│   │   │   ├── mipmap-*/                       (Launcher icons, multiple densities)
│   │   │   ├── raw/                            (SVG icons)
│   │   │   └── xml/
│   │   │       ├── file_paths.xml              (FileProvider config)
│   │   │       └── locales_config.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                        (App module build configuration)
│   └── proguard-rules.pro                      (R8/ProGuard rules)
├── .github/workflows/
│   ├── android-build.yml                       (CI/CD for builds)
│   └── release.yml                             (GitHub release automation)
├── build.gradle.kts                            (Root project configuration)
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── README.md                                   (674 lines - comprehensive user guide)
├── YAML_GUIDE.md                               (YAML format documentation)
├── PRIVACY.md                                  (Privacy policy)
└── seed_english.yaml                           (Template checklist)
```

---

## Architecture & Patterns

### MVVM Architecture

**FlightChecks follows a clean MVVM architecture:**

```
┌─────────────────┐
│   UI Layer      │  Jetpack Compose @Composables
│   (Screens)     │  • HomeScreen.kt
│                 │  • StepScreen.kt
│                 │  • EditChecklistScreen.kt
└────────┬────────┘  • etc.
         │
         ↓ observes State
┌─────────────────┐
│   ViewModels    │  State Management
│                 │  • HomeViewModel
└────────┬────────┘  • StepViewModel
         │
         ↓ calls
┌─────────────────┐
│  Repositories   │  Business Logic & Data Access
│   (Data Layer)  │  • ChecklistRepository
│                 │  • SettingsRepository
│                 │  • ProgressRepository
│                 │  • SensorDataRepository
│                 │  • LogRepository
└────────┬────────┘  • AerodromeRepository
         │
         ↓ uses
┌─────────────────┐
│  Domain Models  │  Data Classes
│                 │  • Paso (Step)
│                 │  • Checklist
└─────────────────┘  • Catalogo (Catalog)
```

### Key Principles

1. **Separation of Concerns**: UI, business logic, and data access are clearly separated
2. **Unidirectional Data Flow**: Data flows down via state, events flow up via callbacks
3. **Reactive State**: State changes trigger automatic UI recomposition
4. **Repository Pattern**: Single source of truth for data access
5. **Dependency Injection**: Manual DI via constructor parameters (no Hilt/Dagger)

### Data Persistence Strategy

- **SharedPreferences**: Checklist progress tracking (legacy, being used alongside DataStore)
- **DataStore Preferences**: App settings (language, theme, screen timeout)
- **File System**: YAML checklists stored in `Android/media/[package]/checklists/`
- **CSV Export**: Flight logs exported to `Download/FlightChecks/`
- **Assets**: Default checklists bundled in APK

---

## Key Technologies

### Core Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.0.20 | Primary language |
| **Jetpack Compose** | BOM 2024.08.00 | Modern declarative UI |
| **Material Design 3** | Latest | Design system |
| **Compose Navigation** | 2.8.0 | Screen routing |
| **Android Gradle Plugin** | 8.6.1 | Build system |
| **Gradle** | 8.6.1 | Build automation |

### Android Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **androidx.activity:activity-compose** | 1.9.2 | Compose integration |
| **androidx.lifecycle** | 2.8.4 | Lifecycle management |
| **androidx.datastore:datastore-preferences** | 1.1.1 | Modern settings storage |
| **material-icons-extended** | Latest | Extended icon set |

### Data & Parsing

| Library | Version | Purpose |
|---------|---------|---------|
| **SnakeYAML** | 2.2 | YAML parsing/serialization |
| **kotlinx-coroutines-android** | 1.9.0 | Async operations |

### Platform Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Java Version**: 17 (Temurin distribution)

---

## Development Workflows

### Version Management

**Version Format**: `w.x.yy.zz`

- **w**: Major version (very important breaking changes) - manual increment in `build.gradle.kts`
- **x**: Large new features (manual increment)
- **yy**: Large groups of improvements and fixes (manual increment, 00-99)
- **zz**: Minor bug fixes (auto-increment based on git commit count)

**Version Code Formula**: `w*100000 + x*10000 + yy*100 + zz`

Example: v1.0.00.05 → version code 100005, v1.2.15.03 → version code 121503

**Location**: `/home/user/TaifunChecks/app/build.gradle.kts:22-51`

```kotlin
val majorVersion = 1      // w - versión principal
val minorVersion = 0      // x - grandes funcionalidades
val patchVersion = 0      // yy - grupos de mejoras (00-99)
val buildVersion = try {
    val count = Runtime.getRuntime().exec("git rev-list --count HEAD")
        .inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 0
    if (count <= 1) 0 else count  // Handle shallow clones
} catch (e: Exception) { 0 }
versionCode = majorVersion * 100000 + minorVersion * 10000 + patchVersion * 100 + buildVersion
```

### Git Workflow

**Branches**:
- `main` / `master`: Stable production code
- `claude/**`: AI assistant development branches
- Feature branches: Short-lived, merged via PRs

**CI/CD Triggers**:
- Push to `main`, `master`, or `claude/**` → Build APK + AAB
- Tag push `v*` → Create GitHub release with artifacts
- Pull requests → Validation builds

### Build Process

**Development Build**:
```bash
./gradlew assembleDebug
```

**Release Build** (requires signing):
```bash
export KEYSTORE_FILE=/path/to/release.keystore
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

**Generate AAB for Play Store**:
```bash
./gradlew bundleRelease
```

**Build Outputs**:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

### CI/CD Pipeline

**Workflow**: `.github/workflows/android-build.yml`

**Steps**:
1. Checkout code
2. Setup JDK 17 (Temurin)
3. Decode keystore from GitHub secrets (if available)
4. Build signed release APK
5. Build signed release AAB
6. Upload artifacts (30-day retention)
7. Generate build summary

**Required GitHub Secrets** (for signing):
- `KEYSTORE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

**Release Workflow**: `.github/workflows/release.yml`

Triggered by git tag push (e.g., `git tag v1.0.00.05 && git push origin v1.0.00.05`)

---

## Code Conventions

### Kotlin Style Guide

**Naming**:
- **Classes**: PascalCase (`ChecklistRepository`, `StepScreen`)
- **Functions**: camelCase (`loadCatalogo()`, `saveSettings()`)
- **Variables**: camelCase (`currentStep`, `isDarkTheme`)
- **Constants**: UPPER_SNAKE_CASE (`KEY_LAST_VERSION_CODE`)
- **Private fields**: camelCase with no prefix (`settingsRepo`, not `_settingsRepo`)

**File Organization**:
1. Package declaration
2. Imports (alphabetized, Android first, then Kotlin, then third-party)
3. Class declaration
4. Public properties/functions
5. Private properties/functions
6. Companion object (if needed)

### Compose Conventions

**Composable Functions**:
- PascalCase naming (`@Composable fun HomeScreen() {}`)
- Preview functions: `@Preview @Composable fun PreviewHomeScreen() {}`
- State hoisting: Pass state and callbacks as parameters
- Modifier parameter: Always include `modifier: Modifier = Modifier`

**Example**:
```kotlin
@Composable
fun ChecklistItem(
    paso: Paso,
    onStepClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

**State Management**:
- Use `remember { mutableStateOf() }` for local UI state
- Use `collectAsState()` for Flow observation
- Hoist state to ViewModel when it needs to survive configuration changes

### Repository Pattern

**Repositories** should:
1. Be initialized with `Context` as constructor parameter
2. Use Kotlin coroutines (`suspend` functions) for async operations
3. Use `withContext(Dispatchers.IO)` for file/network operations
4. Return domain models (not DTOs or raw data)
5. Handle errors gracefully and log appropriately

**Example**:
```kotlin
class ChecklistRepository(private val context: Context) {
    suspend fun loadCatalogo(filename: String): Catalogo = withContext(Dispatchers.IO) {
        try {
            YamlIO.load(File(getStorageDir(), filename))
        } catch (e: YamlParseException) {
            Log.e(TAG, "Failed to parse YAML", e)
            throw e
        }
    }
}
```

### Error Handling

**Logging**:
- Use `android.util.Log` with appropriate levels:
  - `Log.e(TAG, message, exception)` for errors
  - `Log.w(TAG, message)` for warnings
  - `Log.i(TAG, message)` for informational messages
  - `Log.d(TAG, message)` for debug (removed in release builds)

**User-Facing Errors**:
- Show user-friendly error messages in UI
- Use `AlertDialog` for critical errors
- Use `Snackbar` for non-critical notifications

### Comments & Documentation

**When to Comment**:
- Complex algorithms or calculations (e.g., QNH formula)
- Non-obvious business logic
- Workarounds for platform bugs
- TODOs for future improvements

**Comment Style**:
```kotlin
/**
 * Calculates QNH (pressure at sea level) using barometric pressure and GPS altitude.
 *
 * Uses ICAO standard atmosphere formula:
 * QNH = P * (1 - (0.0065 * h / (T + 0.0065 * h + 273.15)))^(-5.255)
 *
 * @param pressure Current barometric pressure in hPa
 * @param altitude GPS altitude in meters
 * @return QNH value in hPa
 */
fun calculateQNH(pressure: Float, altitude: Float): Float {
    // Implementation
}
```

### Spanish in Code

**Language Usage**:
- **Code**: English preferred for new code (variables, functions, comments)
- **Legacy**: Some Spanish comments and variable names exist (e.g., `pasos`, `catalogo`)
- **UI Strings**: All strings externalized to `strings.xml` (Spanish/English)
- **Documentation**: Spanish and English both acceptable

---

## Build System

### Gradle Configuration

**Root `build.gradle.kts`**:
- Applies plugins (Android, Kotlin, Compose)
- No dependencies (plugin classpath only)

**App `build.gradle.kts`**:
- Version management (lines 22-52)
- Signing configuration (lines 66-73)
- Build types (lines 75-96)
- App Bundle configuration (lines 99-104)
- Dependencies (lines 140-167)

### ProGuard/R8 Rules

**Critical Rules** (in `proguard-rules.pro`):

1. **SnakeYAML**: Required for YAML parsing
   ```proguard
   -keep class org.yaml.snakeyaml.** { *; }
   ```

2. **Domain Models**: Required for YAML serialization
   ```proguard
   -keep class com.taifun.checks.domain.** { *; }
   ```

3. **Jetpack Compose**: Required for UI rendering
   ```proguard
   -keep class androidx.compose.runtime.** { *; }
   -keepclassmembers class * {
       @androidx.compose.runtime.Composable <methods>;
   }
   ```

4. **String Resources**: Prevents localization stripping
   ```proguard
   -keep class **.R$string { *; }
   ```

5. **Debug Information**: Required for crash reports
   ```proguard
   -keepattributes SourceFile,LineNumberTable
   ```

### Build Optimization

**Release Build Flags**:
- R8 minification: Enabled
- Resource shrinking: Enabled
- Native debug symbols: FULL (for Play Console crash reports)
- Language splitting: Disabled (ensures all languages included)

**NDK Architectures**:
- armeabi-v7a (32-bit ARM)
- arm64-v8a (64-bit ARM)
- x86 (32-bit Intel)
- x86_64 (64-bit Intel)

---

## Testing Strategy

### Current State

**No automated tests** are currently implemented in this project. Testing is performed:

1. **Manual Testing**: Developer validation on physical devices
2. **CI/CD Validation**: Build verification on every push
3. **Dogfooding**: Active use by pilots in real flight operations

### Future Testing Recommendations

If implementing tests, consider:

**Unit Tests** (`app/src/test/`):
- YamlParser validation
- QNH calculation accuracy
- Version code formula verification
- Repository logic (mocked Context)

**Integration Tests** (`app/src/androidTest/`):
- ChecklistRepository file operations
- SettingsRepository DataStore operations
- Navigation flow verification

**UI Tests** (`app/src/androidTest/`):
- Compose UI tests using `ComposeTestRule`
- Screen navigation tests
- User interaction flows

**Instrumentation Tests**:
- GPS/sensor integration
- File provider operations
- Voice recognition accuracy

---

## Common Tasks

### Adding a New Screen

1. **Create Composable** in `ui/screens/`:
   ```kotlin
   @Composable
   fun NewScreen(
       nav: NavController,
       modifier: Modifier = Modifier
   ) {
       // Screen implementation
   }
   ```

2. **Add Route** in `ui/navigation/Routes.kt`:
   ```kotlin
   object Routes {
       const val NEW_SCREEN = "new_screen"
   }
   ```

3. **Register in NavHost** (`ui/navigation/AppNavHost.kt`):
   ```kotlin
   composable(Routes.NEW_SCREEN) {
       NewScreen(nav = nav)
   }
   ```

4. **Add Navigation Call**:
   ```kotlin
   nav.navigate(Routes.NEW_SCREEN)
   ```

### Adding a New Repository

1. **Create Repository Class** in `data/`:
   ```kotlin
   class NewRepository(private val context: Context) {
       suspend fun loadData(): DataType = withContext(Dispatchers.IO) {
           // Implementation
       }
   }
   ```

2. **Initialize in Activity/ViewModel**:
   ```kotlin
   private val newRepo = NewRepository(context)
   ```

3. **Call from ViewModel**:
   ```kotlin
   viewModelScope.launch {
       val data = newRepo.loadData()
       // Update state
   }
   ```

### Adding a New Setting

1. **Add DataStore Key** in `SettingsRepository.kt`:
   ```kotlin
   private val NEW_SETTING = booleanPreferencesKey("new_setting")
   ```

2. **Create Flow** in `SettingsRepository`:
   ```kotlin
   val newSettingFlow: Flow<Boolean> = dataStore.data.map { it[NEW_SETTING] ?: false }
   ```

3. **Create Save Function**:
   ```kotlin
   suspend fun setNewSetting(value: Boolean) {
       dataStore.edit { it[NEW_SETTING] = value }
   }
   ```

4. **Add to SettingsScreen UI** (`ui/screens/SettingsScreen.kt`)

5. **Add String Resource** in `res/values/strings.xml` and `res/values-en/strings.xml`

### Adding a New Icon

1. **Add SVG** to `res/raw/` (if SVG source)

2. **Add Icon Definition** in `IconsRepo.kt`:
   ```kotlin
   val NewIcon: ImageVector
       get() = ImageVector.Builder(...)
           .path { ... }
           .build()
   ```

3. **Use in Composables**:
   ```kotlin
   Icon(imageVector = IconsRepo.NewIcon, contentDescription = "...")
   ```

### Modifying YAML Structure

**CRITICAL**: Changing YAML structure requires backward compatibility!

1. **Update Domain Models** (`domain/models.kt`)
2. **Update YamlParser** (`data/yaml/YamlParser.kt`)
3. **Test with Existing YAML Files**
4. **Update YAML_GUIDE.md** with new schema
5. **Consider Migration Path** for existing user checklists

### Adding a New Localization

1. **Create Values Directory**: `res/values-<lang>/`
2. **Copy `strings.xml`** from `res/values/` or `res/values-en/`
3. **Translate All Strings**
4. **Add Locale Support** in `res/xml/locales_config.xml`:
   ```xml
   <locale android:name="<lang>" />
   ```
5. **Update MainActivity** language selection logic if needed
6. **Test Locale Switching** in Settings screen

---

## Critical Paths & Safety

### Safety-Critical Code

**These components are used during actual flight operations and require extra care:**

1. **StepScreen.kt** (checklist execution)
   - Controls which steps are shown/completed
   - Must be 100% reliable
   - Test thoroughly on multiple devices

2. **YamlParser.kt** (checklist parsing)
   - Malformed YAML could cause crashes during flight
   - Extensive error handling required
   - Validate all edge cases

3. **ProgressRepository.kt** (checklist state)
   - Must persist progress reliably
   - Losing progress mid-flight is unacceptable
   - Test SharedPreferences edge cases

4. **SensorDataRepository.kt** (GPS/barometer)
   - Altitude and QNH calculations used for navigation
   - Sensor errors must be handled gracefully
   - Always show units clearly (meters vs feet)

### Data Loss Prevention

**Never delete user data without confirmation:**
- Checklist progress reset → Show confirmation dialog
- Custom checklist deletion → Show confirmation dialog
- Log entry deletion → Show confirmation dialog
- Settings reset → Show confirmation dialog

### Permission Handling

**Optional Permissions** (graceful degradation):
- RECORD_AUDIO → Voice control disabled if denied
- ACCESS_FINE_LOCATION → Altitude/QNH features disabled if denied
- INTERNET → Link feature disabled if denied

**Never crash if permission denied** - always provide fallback behavior.

### Crash Prevention

**Common Crash Causes**:
1. **YAML Parsing Errors**: Always catch `YamlParseException`
2. **File Not Found**: Check file existence before reading
3. **Null Context**: Ensure Context is valid before repository calls
4. **Compose Recomposition**: Use `remember` for expensive operations
5. **Coroutine Cancellation**: Use `NonCancellable` for critical cleanup

---

## Documentation Resources

### Internal Documentation

- **README.md** (674 lines): Comprehensive user guide with features, installation, user guide, changelog
- **YAML_GUIDE.md**: YAML format specification for creating custom checklists
- **PRIVACY.md**: Privacy policy covering data collection, GPS usage, logging
- **.github/workflows/README.md**: CI/CD workflow documentation
- **.github/RELEASE_SIGNING_SETUP.md**: Release signing configuration guide

### External Resources

**Android Development**:
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

**Dependencies**:
- [SnakeYAML Documentation](https://bitbucket.org/snakeyaml/snakeyaml/wiki/Documentation)
- [DataStore Guide](https://developer.android.com/topic/libraries/architecture/datastore)

**Aviation**:
- ICAO Standard Atmosphere calculations (for QNH)
- OpenAIP aerodrome database format

### Key Files to Read First

If you're new to this codebase:

1. **README.md** - Understand what the app does
2. **This file (CLAUDE.md)** - Understand how the code works
3. **domain/models.kt** - Understand core data structures
4. **MainActivity.kt** - Understand app initialization
5. **AppNavHost.kt** - Understand screen navigation
6. **ChecklistRepository.kt** - Understand data access patterns
7. **StepScreen.kt** - Understand main user workflow

---

## Troubleshooting Guide

### Build Issues

**Problem**: "SDK location not found"
**Solution**: Create `local.properties` with `sdk.dir=/path/to/Android/sdk`

**Problem**: "Git command failed" during version code calculation
**Solution**: Fallback version is used automatically; check git installation if needed

**Problem**: "Keystore not found" during release build
**Solution**: Set environment variables or build debug version instead

**Problem**: R8 obfuscation breaks runtime behavior
**Solution**: Add keep rules to `proguard-rules.pro` for affected classes

### Runtime Issues

**Problem**: YAML fails to parse
**Solution**: Validate YAML syntax using YAML_GUIDE.md; check for proper indentation

**Problem**: Settings not persisting
**Solution**: Check DataStore initialization; ensure no crashes during write operations

**Problem**: GPS altitude showing incorrect values
**Solution**: Verify location permissions granted; check sensor availability

**Problem**: Voice control not working
**Solution**: Verify RECORD_AUDIO permission; check device speech recognition setup

**Problem**: Dark theme not applying
**Solution**: Check SettingsRepository flow collection; verify theme propagation in MainActivity

### Localization Issues

**Problem**: Strings showing in wrong language
**Solution**: Check `MainActivity.attachBaseContext()` logic; verify locale configuration

**Problem**: Language splitting removes translations
**Solution**: Ensure `bundle.language.enableSplit = false` in `build.gradle.kts:102`

**Problem**: New strings not appearing
**Solution**: Run "Clean Project" + "Rebuild Project" to regenerate R class

### CI/CD Issues

**Problem**: Build fails on CI but works locally
**Solution**: Check shallow git clone handling; verify Java version matches (17)

**Problem**: Artifacts not uploaded
**Solution**: Verify GitHub Actions permissions; check artifact path correctness

**Problem**: Release not created from tag
**Solution**: Ensure tag format is `v*` (e.g., v1.0.00.05); check workflow trigger configuration

---

## Migration Notes

### From v1.5.x to v1.0.00.zz (New Versioning System)

**Major Changes**:
- Changed version format from `major.minor.patch` to `w.x.yy.zz`
- Version code formula changed from `major*10000 + minor*100 + patch` to `w*100000 + x*10000 + yy*100 + zz`
- First version with new system: v1.0.00.00
- `zz` component auto-increments with each commit

**Breaking Changes**: None (backward compatible, only versioning scheme changed)

### From v1.4 to v1.5

**Major Changes**:
- Added flight logging with GPS tracking
- Added aerodrome database integration
- Added log viewer screen
- Fixed language selection using SharedPreferences

**Breaking Changes**: None (backward compatible)

### From v1.3 to v1.4

**Major Changes**:
- Added optional step features (altitude, QNH, links, apps, time)
- Migrated from single `checklists.yaml` to multi-file management
- Added checklist manager screen

**Breaking Changes**:
- Old `checklists.yaml` automatically migrated to new structure
- Progress data migrated to new format

---

## Best Practices for AI Assistants

### When Making Code Changes

1. **Read Before Writing**: Always read the full file before editing
2. **Preserve Indentation**: Match existing code style (4 spaces, no tabs)
3. **Test Impact**: Consider how changes affect safety-critical functionality
4. **Backward Compatibility**: Ensure YAML changes don't break existing checklists
5. **Localization**: Add strings to both Spanish and English resource files
6. **Logging**: Add appropriate log statements for debugging

### When Adding Features

1. **Check Existing Patterns**: Follow established architectural patterns
2. **Update Documentation**: Update README.md and this file as needed
3. **Consider Offline Use**: Ensure feature works without internet
4. **Test on Multiple Devices**: UI must work on different screen sizes
5. **Think About Pilots**: Consider use in cockpit environment (gloves, sunlight, turbulence)

### When Fixing Bugs

1. **Reproduce First**: Understand the exact failure scenario
2. **Check Logs**: Review Android Logcat for stack traces
3. **Test Edge Cases**: Consider null values, empty lists, missing files
4. **Verify Fix**: Test on both debug and release builds
5. **Add Safeguards**: Prevent similar bugs in related code

### Communication

When explaining code to developers:
- Reference file paths with line numbers (e.g., `StepScreen.kt:542`)
- Link to relevant documentation sections
- Explain "why" not just "what"
- Highlight safety implications if relevant
- Suggest testing approaches

---

## Revision History

| Date | Version | Changes |
|------|---------|---------|
| 2025-11-19 | 1.1 | Updated versioning system to w.x.yy.zz format |
| 2025-11-17 | 1.0 | Initial CLAUDE.md creation |

---

**For questions or clarifications about this codebase, refer to:**
- **README.md** for user-facing documentation
- **YAML_GUIDE.md** for checklist format specification
- **GitHub Issues** for known bugs and feature requests
- **Git History** for understanding past changes

**Remember**: This is safety-critical aviation software. When in doubt, ask for clarification and test thoroughly.
