# Taifun Checks - Aviation Checklist App

![Version](https://img.shields.io/badge/version-1.5.8-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Language](https://img.shields.io/badge/language-Kotlin-purple.svg)

A professional aviation checklist application designed specifically for Taifun 17E aircraft operations. Built with modern Android technologies and focused on safety, usability, and accessibility in aviation environments.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [User Guide](#user-guide)
  - [First Launch Setup](#first-launch-setup)
  - [Home Screen](#home-screen)
  - [Checklist Execution](#checklist-execution)
  - [Voice Control](#voice-control)
  - [Color Customization](#color-customization)
  - [Checklist Manager](#checklist-manager)
  - [YAML Editor](#yaml-editor)
  - [Settings](#settings)
- [YAML Guide](#yaml-guide)
- [Building from Source](#building-from-source)
- [Technical Details](#technical-details)
- [Contributing](#contributing)
- [Privacy & Data Protection](#privacy--data-protection)
- [License](#license)

## Features

### Core Functionality
- **Multiple Display Modes**: Switch between step-by-step and full-list views
- **Multi-Language Voice Control**: Hands-free navigation with Spanish and English voice commands
- **Multi-Checklist Manager**: Manage, switch between, and organize multiple YAML files
- **Color Customization**: Assign custom colors to checklists for easy visual identification
- **Custom Checklists**: Create and edit checklists using YAML format or visual editor
- **Import/Export**: Share checklists with other users
- **Search**: Quickly find checklists by name or category
- **First Launch Setup**: Welcome screen with language and checklist selection

### Accessibility & UX
- **Haptic Feedback**: Tactile confirmation of button presses (configurable)
- **Dark Theme**: Reduce eye strain in low-light conditions
- **High Contrast Mode**: Maximum readability in difficult conditions
- **Large Touch Targets**: Easy to use with gloves
- **Screen Always On**: Prevent screen timeout during critical operations
- **Multi-language**: English and Spanish UI support

### Aviation-Specific
- **Custom Icons**: 40+ aviation-specific vector icons
- **Category Organization**: Group checklists by procedure type
- **Progress Tracking**: Visual indication of completion status
- **Reset Confirmation**: Prevent accidental checklist resets

### Optional Step Features (NEW in v1.4)
- **Altitude Display**: Show real-time GPS altitude in meters or feet
- **QNH Calculation**: Calculate QNH using barometer + GPS (ICAO formula)
- **Link Opening**: Tap steps to open URLs (weather, charts, etc.)
- **App Launching**: Tap steps to launch apps (ForeFlight, Garmin, etc.)
- **Time Display**: Show local or UTC time in real-time

### Flight Logging Features (NEW in v1.5.7-1.5.8)
- **GPS Tracking & Logging**: Automatically log GPS coordinates, altitude, and custom notes during procedures
- **CSV Export/Import**: Export flight logs to CSV format for analysis or record-keeping
- **Log Viewer**: Dedicated screen to view, edit, and manage flight log entries
- **Automatic Engine Logging**: For gliders - automatically log engine start/stop events
- **Aerodrome Database**: Integration with OpenAIP aerodrome data for enhanced logging
- **Persistent Storage**: Logs automatically saved to Download/FlightChecks/ folder

## Requirements

- **Android Version**: 7.0 (API 24) or higher
- **Permissions**:
  - Microphone (optional, for voice control)
  - Storage (for import/export)
  - Location (optional, for altitude/QNH features)
  - Internet (optional, for link feature)
- **Screen**: Minimum 5" recommended for optimal experience
- **Sensors**: Barometer (optional, for QNH calculation)
- **Internet**: Not required (fully offline capable)

## Installation

### From APK
1. Download the latest APK from the [Releases](../../releases) page
2. Enable "Install from Unknown Sources" in your Android settings
3. Open the APK file and follow installation prompts
4. Launch "Taifun Checks" from your app drawer

### From Source
See [Building from Source](#building-from-source) section below.

## User Guide

### First Launch Setup

On first launch, the app presents a welcome screen to configure your preferences.

**Setup Options:**
- **Language Selection**:
  - Auto (System): Uses device language
  - Spanish (Español): Force Spanish interface
  - English: Force English interface
- **Active Checklist**: Choose which YAML file to use as default

**After Setup:**
- Language applies immediately
- Selected checklist becomes active
- Setup screen only shows once (can be changed later in Settings)

### Home Screen

The home screen displays all available checklists organized by category.

**Features:**
- **Search Bar**: Type to filter checklists by name
- **Category Groups**: Checklists grouped by type (Normal Procedures, Emergency Procedures, etc.)
- **Color Indicators**: Visual color bars on the left side of each checklist for easy identification
- **Colored Buttons**: Checklist buttons use custom colors when assigned
- **Quick Access**: Tap any checklist to start execution
- **Checklist Manager Button**: Manage multiple YAML files
- **Settings Button**: Access app configuration
- **Help Button**: Access this documentation

### Checklist Execution

#### Step-by-Step Mode
Perfect for detailed procedures that require careful attention to each item.

- **Large Text**: Current step displayed in large, easy-to-read font
- **Step Counter**: Shows current position (e.g., "Step 3 of 15")
- **Navigation Buttons**:
  - **Previous**: Go back one step (or return to home if on first step)
  - **Next**: Advance to next step
- **Custom Icons**: Visual representation of each step
- **Reset Button**: Restart checklist from the beginning (with confirmation)

#### Full-List Mode
Ideal for quick reviews or familiar procedures.

- **Checkbox List**: All steps visible with checkboxes
- **Pagination**: ~10 items per page for better readability
- **Auto-Advance**: Automatically moves to next page when all items checked
- **Page Counter**: Shows current page (e.g., "Page 2 / 3")
- **Manual Navigation**: Previous/Next buttons for page control
- **Auto-Complete**: Returns to home screen when all steps completed

#### Switching Modes
- Tap the **List/CheckCircle icon** in the top bar to toggle between modes
- Your preference is remembered for the current session
- Each checklist can have a default mode set in YAML (but always toggleable)

### Voice Control

Hands-free navigation using multi-language voice commands. The app automatically adapts to your selected language.

**Activation:**
1. Tap the microphone icon in the top bar
2. Grant microphone permission when prompted
3. Icon turns blue when listening

**Commands:**

**Spanish:**
- **"Anterior"** - Go to previous step/page
- **"Siguiente"** - Go to next step/page

**English:**
- **"Previous"** - Go to previous step/page
- **"Next"** - Go to next step/page

**Language Adaptation:**
- Automatically uses your selected language (from Settings)
- Falls back to system language in Auto mode
- Voice recognition optimized for Spanish (es-ES) and English (en-US)

**Tips:**
- Speak clearly and wait for the listening indicator
- Works in both step-by-step and full-list modes
- Voice control button is hidden when active to prevent accidental deactivation
- Tap the blue microphone icon to disable voice control

### Color Customization

Assign custom colors to checklists for quick visual identification.

**Features:**
- **Predefined Palette**: 8 ready-to-use colors:
  - Red, Green, Blue, Yellow
  - Orange, Purple, Pink, Cyan
- **Custom RGB**: Define any color using hex codes (#RRGGBB)
- **Color Validation**: Real-time validation of hex color codes
- **Visual Feedback**: Colors appear as:
  - Sidebar indicator on checklist cards
  - Button background color
  - Icon tint in step screen

**How to Set Colors:**
1. Edit a checklist (tap pencil icon)
2. Tap "Select Color" or current color preview
3. Choose from predefined colors or enter custom hex
4. Tap "Accept" to save
5. Color applies immediately throughout the app

**Removing Colors:**
- Select "None" from the color picker to use default theme colors

### Checklist Manager

Manage multiple YAML checklist files in one place.

**Access:**
- Tap the **List icon** (☰) in the home screen top bar

**Features:**
- **Multiple Files**: Import, manage, and switch between different YAML files
- **Active Checklist**: One checklist file is active at a time (shown with checkmark)
- **File Operations**:
  - **Select**: Make a checklist file active
  - **Edit**: Open YAML editor for specific file
  - **Export**: Share individual checklist file
  - **Delete**: Remove checklist file (with confirmation)
  - **Import**: Add new YAML files from device storage

**Workflow:**
1. Open Checklist Manager from home screen
2. View all available YAML files
3. Select a file to make it active
4. Tap "Edit" button to modify that file in YAML editor
5. Import new files using the "Import" button
6. Export or delete files as needed

**Use Cases:**
- Maintain separate checklists for different aircraft
- Keep emergency procedures in separate files
- Share specific checklist sets with other pilots
- Organize by flight phase or season

### YAML Editor

Edit checklists directly within the app using YAML syntax. The editor works with the currently active checklist file.

**Features:**
- **Real-time Validation**: Errors shown as you type (500ms delay)
- **Active File Indicator**: Shows which file you're editing
- **Syntax Highlighting**: Color-coded YAML structure
- **Status Indicators**:
  - ✓ "Valid YAML" - No errors detected
  - ✗ "Validation Error" - Syntax issues found
- **Save**: Apply changes to active checklist file
- **Import**: Load YAML files from device storage (replaces active file content)
- **Export**: Share active checklist file with other users

**Access Methods:**
1. From Checklist Manager: Tap "Edit" button on any file (sets it as active, then opens editor)
2. Editor always modifies the active checklist file shown in the status bar

**Workflow:**
1. Open YAML Editor (from Checklist Manager)
2. Edit the YAML content for the active file
3. Watch for validation status at the bottom
4. Tap "Save" to apply changes to the active file
5. Return to home screen to see updated checklists

See [YAML_GUIDE.md](YAML_GUIDE.md) for detailed YAML format documentation.

### Optional Step Features

Add dynamic, real-time data to individual steps in your checklists.

**Available Features:**

1. **Altitude Display**
   - Shows real-time GPS altitude
   - Format: `altitud: m` (meters) or `altitud: ft` (feet)
   - Displayed with thousands separator, no decimals
   - Example: "2,850 ft" or "850 m"
   - Requires location permission

2. **QNH Calculation**
   - Calculates QNH using barometer + GPS altitude
   - Format: `qnh: hPa` or `qnh: inHg`
   - Uses ICAO standard barometric formula
   - hPa: whole numbers (e.g., "1,013 hPa")
   - inHg: 2 decimals (e.g., "29.92 inHg")
   - Requires location permission and barometer sensor

3. **Link Opening**
   - Opens URL when tapping step area (outside navigation buttons)
   - Format: `link: https://example.com`
   - Useful for weather sites, charts, documentation
   - Opens in default browser

4. **App Launching**
   - Opens specified app when tapping step area
   - Format: `app: com.package.name`
   - **Use package name, NOT the display name**
   - **Common Examples:**
     - Google Maps: `com.google.android.apps.maps`
     - Chrome: `com.android.chrome`
     - WhatsApp: `com.whatsapp`
     - Gmail: `com.google.android.gm`
     - ForeFlight: `com.foreflight.mobile`
   - **Finding package names:**
     - Settings → Apps → App Info (look for package field)
     - Use "App Inspector" app from Play Store
     - Check Play Store URL: `play.google.com/store/apps/details?id=PACKAGE_NAME`
   - Silent fail if app not installed

5. **Time Display**
   - Shows current time in real-time
   - `localtime: true` → Local time (HH:mm:ss)
   - `utctime: true` → UTC time (HH:mm:ss UTC)
   - Updates continuously

**Using Multiple Features:**
You can combine multiple features in a single step!

**Example YAML:**
```yaml
- id: altitude_check
  texto: "Verify cruise altitude"
  icono: "altitud"
  altitud: ft
  qnh: inHg
  localtime: true
  link: https://aviationweather.gov
```

**Adding via Editor:**
1. Edit any step in the visual editor
2. Scroll to "Optional Features" section
3. Fill in desired fields
4. Leave blank to disable a feature

### Flight Logging & Log Viewer

Record your flight activities with GPS-tracked log entries.

**Using the Log Feature:**

1. **Creating Log Entries**
   - Add `log: "Description"` field to any step in YAML
   - Example: `log: "Takeoff runway 32L"`
   - When executing that step, a "Log" button appears
   - Tap the button to create a log entry with:
     - Your custom text
     - Current GPS coordinates (latitude/longitude)
     - Current altitude
     - Timestamp
     - Nearest aerodrome ICAO code (if available)

2. **Automatic Engine Logging (Gliders)**
   - For glider operations, engine start/stop events are logged automatically
   - No manual intervention required
   - Tracks engine usage for maintenance and record-keeping

3. **Accessing the Log Viewer**
   - Open from home screen menu
   - View all log entries in chronological order
   - See total entry count
   - Each entry shows: timestamp, text, coordinates, altitude, ICAO code

4. **Managing Log Entries**
   - **Edit**: Tap pencil icon to modify entry details
   - **Delete**: Tap trash icon to remove individual entries
   - **Clear All**: Use menu option to clear entire log (with confirmation)

5. **Exporting Logs**
   - **Auto-save**: Logs automatically saved to `Download/FlightChecks/` folder as CSV
   - **Manual Export**: Tap "Save CSV" to export with custom location
   - **Share**: Tap share icon to send log via email, messaging, etc.
   - CSV format compatible with Excel, Google Sheets, and flight logging software

6. **Importing Logs**
   - Tap "Import CSV" to load existing log files
   - Note: Import will overwrite current log (confirmation required)
   - Useful for merging logs or restoring backups

**Use Cases:**
- Track takeoff and landing times
- Record fuel stops and quantities
- Log maintenance events
- Document flight route waypoints
- Track engine usage (especially for gliders)
- Maintain flight records for logbook
- Share flight data with instructors or clubs

**YAML Example with Log:**
```yaml
- id: takeoff_log
  texto: "Record takeoff"
  icono: "vuelo"
  log: "Takeoff runway 32L"
  altitud: ft
```

### Settings

Configure the app to your preferences.

#### Theme
- **Dark Theme**: Toggle dark/light color scheme (default: dark)
- **High Contrast**: Maximum readability for bright sunlight or low visibility
  - Increases contrast ratios
  - Bolder text
  - Enhanced borders

#### Screen
- **Keep Screen On**: Prevents screen timeout during checklist execution
  - Useful for long procedures
  - Battery consideration: increases power consumption

#### Language
- **Auto (System)**: Uses device language setting
  - Falls back to Spanish if system language is not English
- **Spanish**: Force Spanish UI
- **English**: Force English UI
- **Note**: YAML content language is independent of UI language

#### Haptics
- **Haptic Feedback**: Enable/disable vibrations on button presses
  - Provides tactile confirmation
  - Useful in noisy environments
  - Disable to conserve battery

## YAML Guide

See the detailed [YAML_GUIDE.md](YAML_GUIDE.md) for complete documentation on:
- YAML file structure
- Creating checklists
- Adding custom icons
- Configuring display modes
- Best practices

Quick example:
```yaml
version: 0.1
checklists:
  - id: pre_flight
    titulo: "Pre-Flight Check"
    categoria: "Normal Procedures"
    full-list: false  # Default to step-by-step mode
    color: "#4CAF50"  # Optional: custom color (hex format)
    pasos:
      - id: step1
        texto: "Documents verified"
        icono: "documento"
      - id: step2
        texto: "Fuel quantity checked"
        icono: "combustible"
```

**New in v1.3:**
- `color`: Optional hex color code (#RRGGBB) for visual identification
- Multi-file support: Save different YAMLs for different purposes
- Multi-language voice commands: Spanish and English support

## Building from Source

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 11 or newer
- Android SDK with API 24-35
- Gradle 8.6.1 (included via wrapper)

### Build Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/jorgemarmor/FlightChecks.git
   cd FlightChecks
   ```

2. **Open in Android Studio**
   - File → Open → Select the FlightChecks directory
   - Wait for Gradle sync to complete

3. **Install required SDK components**
   - API 35 (Android 15)
   - Build-Tools 35.x
   - Android Studio will prompt to install missing components

4. **Build the app**

   Using Gradle (command line):
   ```bash
   ./gradlew assembleDebug
   ```

   Using Android Studio:
   - Click the "Run" button (green triangle)
   - Or Build → Build Bundle(s) / APK(s) → Build APK(s)

5. **Install on device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build (requires signing)

### Troubleshooting Build Issues
- **Gradle sync failed**: Check internet connection, Gradle downloads dependencies
- **SDK not found**: Install required SDK versions via SDK Manager
- **Build tools not found**: Update Build Tools in SDK Manager
- **Kotlin compiler error**: Ensure Kotlin plugin is up to date

## Technical Details

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose (declarative UI)
- **Language**: Kotlin 2.0.20
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Gradle**: 8.6.1

### Key Technologies
- **Jetpack Compose**: Modern declarative UI framework
- **DataStore Preferences**: Settings persistence
- **SnakeYAML 2.2**: YAML parsing and serialization
- **Kotlin Coroutines**: Asynchronous operations
- **Navigation Compose**: Screen routing
- **Material Design 3**: Modern UI components
- **Speech Recognition**: Android SpeechRecognizer API

### Data Storage
- **Default Checklists**: Bundled with app in `app/src/main/assets/`
  - `Taifun17E_ES.yaml` (Spanish version)
  - `Taifun17E_EN.yaml` (English version)
- **User Checklists**: `context.filesDir/*.yaml` (supports multiple files)
  - Default files: `Taifun17E_ES.yaml`, `Taifun17E_EN.yaml`
  - Additional files: `checklist_<timestamp>.yaml`
- **Settings**: DataStore Preferences (`settings_prefs`)
  - Active checklist file
  - Language preference
  - Theme settings
  - First launch status
- **All data stored locally** - no cloud sync or internet required

### Permissions
- **RECORD_AUDIO**: Required only for voice control feature (optional)
  - Requested at runtime when user activates voice control
  - App functions fully without this permission
- **ACCESS_FINE_LOCATION** / **ACCESS_COARSE_LOCATION**: Required for altitude/QNH features (optional)
  - Requested at runtime when opening checklist with these features
  - App functions fully without these permissions
- **INTERNET**: Required for link opening feature (optional)
  - No runtime request needed
  - App functions fully offline

## Project Structure

```
FlightChecks/
├── app/
│   ├── src/main/
│   │   ├── java/com/taifun/checks/
│   │   │   ├── data/              # Repositories and data handling
│   │   │   │   ├── ChecklistRepository.kt
│   │   │   │   ├── SettingsRepository.kt
│   │   │   │   └── yaml/YamlParser.kt
│   │   │   ├── domain/            # Data models
│   │   │   │   └── models.kt
│   │   │   └── ui/                # Screens, components, theme
│   │   │       ├── screens/       # HomeScreen, StepScreen, etc.
│   │   │       ├── theme/         # TaifunTheme.kt
│   │   │       ├── navigation/    # AppNavHost.kt
│   │   │       ├── HapticUtils.kt
│   │   │       ├── IconsRepo.kt   # Custom aviation icons
│   │   │       └── MainActivity.kt
│   │   ├── assets/                # Default YAML checklists
│   │   │   ├── Taifun17E_ES.yaml  # Spanish checklists
│   │   │   └── Taifun17E_EN.yaml  # English checklists
│   │   └── res/                   # Android resources
│   │       ├── values/            # Spanish strings
│   │       └── values-en/         # English strings
│   └── build.gradle.kts
├── seed_english.yaml              # English checklist template (not in package)
├── YAML_GUIDE.md                 # YAML format documentation
└── README.md                     # This file
```

### Key Files
- **MainActivity.kt**: App entry point, handles first launch detection and settings
- **AppNavHost.kt**: Navigation routing between screens
- **FirstLaunchScreen.kt**: Welcome screen with language and checklist selection
- **HomeScreen.kt**: Main checklist list with search, categories, and color indicators
- **ChecklistManagerScreen.kt**: Multi-file YAML management interface
- **StepScreen.kt**: Checklist execution with step-by-step and full-list modes
- **EditorScreen.kt**: YAML editor with validation (works with active checklist)
- **EditChecklistScreen.kt**: Visual editor for individual checklists
- **SettingsScreen.kt**: App configuration
- **ColorPickerDialog.kt**: Color selection component with predefined palette and custom RGB
- **YamlParser.kt**: YAML parsing and serialization logic (supports color field)
- **models.kt**: Data classes (Paso, Checklist with color field, Catalogo)
- **ChecklistRepository.kt**: Multi-file YAML file operations
- **SettingsRepository.kt**: Persists active checklist, language, and first launch status

## Official References

This project uses the following Android technologies:
- [Jetpack Compose](https://developer.android.com/jetpack/compose/documentation) - Modern UI toolkit
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) - Navigation handling
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Settings storage
- [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider) - File import/export
- [Accessibility](https://developer.android.com/guide/topics/ui/accessibility) - Accessibility features
- [Screen Densities](https://developer.android.com/training/multiscreen/screendensities) - Touch target sizing
- [Keep Screen On](https://developer.android.com/reference/android/view/View#setKeepScreenOn(boolean)) - Screen timeout control

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Test on multiple Android versions
- Ensure accessibility features work correctly

## Safety Notice

**⚠️ IMPORTANT**: This app is a tool to assist with aviation procedures but should never be the sole reference for flight operations. Always:
- Follow official aircraft manuals and procedures
- Verify all checklist items against official documentation
- Maintain current pilot certification and training
- Use in accordance with aviation regulations

The developers assume no liability for the use of this application in actual flight operations.

## Privacy & Data Protection

**Your privacy is important to us.** Taifun Checks is designed with privacy by default:

- **GPS Used Only for Altitude**: Location permission is requested solely to obtain altitude for altimeter calibration. Geographic location (lat/lon) is NOT collected, stored, or transmitted.
- **No Data Tracking**: We do not track your location, flight patterns, or usage behavior.
- **Fully Offline**: All data stored locally on your device. No cloud sync, no servers, no data transmission.
- **Optional Permissions**: All permissions (location, microphone, internet) are optional - the app works fully without them.
- **No Third Parties**: No analytics, no ads, no external services.

For complete details, see [PRIVACY.md](PRIVACY.md).

## Support

- **Issues**: Report bugs or request features via [GitHub Issues](../../issues)
- **Documentation**: See [YAML_GUIDE.md](YAML_GUIDE.md) for YAML editing help
- **In-App Help**: Tap the Help button in the home screen
- **Updates**: Check [Releases](../../releases) for latest versions

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Designed for Taifun 17E aircraft operations
- Built with modern Android Jetpack libraries
- Custom aviation icons designed specifically for this application

---

**Version**: 1.5.8
**Last Updated**: 2025-11-12
**Maintainer**: Jorge Mármol

## Changelog

### v1.5.8 (2025-11-10)
- **GPS Tracking & CSV Logging**: Track and log GPS coordinates, altitude, and custom notes
- **Log Viewer Screen**: Dedicated interface to view, edit, and manage flight log entries
- **Aerodrome Database**: Integration with OpenAIP data for enhanced logging with ICAO codes
- **Automatic Engine Logging**: For gliders - automatic logging of engine start/stop events
- **CSV Export/Import**: Export logs to CSV format, import existing logs
- **Improved File Storage**: Logs automatically saved to Download/FlightChecks/ folder for easy access
- **Enhanced GPS Tracking**: More reliable GPS data collection and formatting

### v1.5.7 (2025-11-08)
- **Log Field for Steps**: Added `log` field to checklist steps for GPS-tracked logging
- **Log Button**: Steps with log field show a "Log" button to create entries
- **Model Updates**: Extended Paso model to support log field and GPS data

### v1.5.6 (2025-11-06)
- **Generic Branding**: Removed Taifun 17E specific references for broader aircraft compatibility
- **Build Optimization**: Removed debug builds from automated workflow

### v1.5.5 (2025-11-05)
- **Optional Info Display**: Show dynamic data in full-list mode
- **Clickable Links/Apps**: Tap steps to open URLs or launch apps in full-list mode
- **Enhanced UX**: Better interaction with optional features across display modes

### v1.5.4 (2025-11-04)
- **Auto-detect YAML Files**: Automatically include all YAML files from assets folder
- **Simplified Setup**: No need to manually configure checklist file lists

### v1.5.3 (2025-11-03)
- **New Aircraft Support**: Added YAML checklists for SF28
- **Configuration Updates**: Build and deployment improvements

### v1.5.2 (2025-11-03)
- **Google Play Optimization**: Configuration fixes for better Play Store compatibility
- **Debug Symbols**: Enhanced crash reporting configuration

### v1.5.1 (2025-11-03)
- **Grob 109 Support**: Added YAML checklists for Grob 109 aircraft
- **Multi-Aircraft**: Enhanced multi-aircraft support

### v1.5.0 (2025-11-03)
- **Build Optimization**: Added native debug symbols for Google Play
- **Resource Management**: Improved resource handling and artifact retention

### v1.4.0 (2025-11-03)
- **Optional Step Features**: Add dynamic data to individual steps
  - **Altitude Display**: Real-time GPS altitude in meters or feet
  - **QNH Calculation**: ICAO-standard QNH using barometer + GPS
  - **Link Opening**: Tap steps to open URLs
  - **App Launching**: Tap steps to open other apps
  - **Time Display**: Show local or UTC time in real-time
- **Enhanced Editor**: Visual editor now supports all optional features
- **Build Optimization**: Enabled R8/ProGuard for 20-40% smaller APK
- **Debug Symbols**: Added native debug symbols for better crash analysis
- **Complete English Translation**: All UI screens fully translated
- **Permissions**: Added location permissions for altitude/QNH features

### v1.3.0 (2025-11-01)
- **Multi-Language Voice Control**: Added English voice commands alongside Spanish
- **Color Customization**: Assign custom colors to checklists for visual identification
- **Checklist Manager**: Manage multiple YAML files, switch between them, import/export individual files
- **First Launch Setup**: Welcome screen with language and checklist selection
- **Enhanced YAML Editor**: Now works with active checklist file
- **UI Improvements**: Color indicators on checklist buttons and sidebar

### v1.2.0
- Visual editor for checklists and steps
- Icon selector with visual preview
- Category editing

### v1.1.0
- Stability improvements
- Bug fixes

### v1.0.0
- Initial release
- Step-by-step and full-list modes
- Voice control (Spanish)
- YAML editor
- Custom icons
