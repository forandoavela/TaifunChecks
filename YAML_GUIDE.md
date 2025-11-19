# YAML Checklist Guide for Taifun Checks

This guide explains how to create and edit checklists for the Taifun Checks app using YAML format.

## Table of Contents

- [YAML Basics](#yaml-basics)
- [File Structure](#file-structure)
- [Creating a Checklist](#creating-a-checklist)
- [Available Icons](#available-icons)
- [Display Modes](#display-modes)
- [Best Practices](#best-practices)
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)

## YAML Basics

YAML (YAML Ain't Markup Language) is a human-readable data format. Here are the key rules:

### Indentation
- Use **2 spaces** for indentation (not tabs!)
- Consistent indentation is crucial - incorrect indentation will cause errors

### Data Types
```yaml
# String (text)
titulo: "Pre-Flight Check"
titulo: 'Pre-Flight Check'  # Single quotes also work
titulo: Pre-Flight Check     # Quotes optional for simple text

# Boolean (true/false)
full-list: true
full-list: false

# Lists
pasos:
  - item one
  - item two
```

### Special Characters
- Use quotes around text containing: `: ? - [ ] { } # | > @ & *`
- Example: `"Question: Is fuel sufficient?"`

## File Structure

Every checklist file must start with a version and contain a list of checklists:

```yaml
version: 0.1
checklists:
  - id: checklist_one
    # ... checklist details
  - id: checklist_two
    # ... checklist details
```

### Required Fields

#### Checklist Level
```yaml
- id: unique_checklist_id      # Unique identifier (no spaces)
  titulo: "Checklist Title"    # Display name
  categoria: "Category Name"   # Group name
  pasos:                       # List of steps (required)
    - # step 1
    - # step 2
```

#### Step Level (pasos)
```yaml
- id: step_id                  # Unique identifier for this step
  texto: "Step description"    # The actual checklist item text
  icono: "icon_name"          # Icon name (see Available Icons)
```

### Optional Fields

```yaml
- id: checklist_id
  titulo: "Title"
  categoria: "Category"
  full-list: true             # Optional: default display mode
  color: "#4CAF50"            # Optional: custom color
  pasos:
    - id: step1
      texto: "Step text"
      icono: "icon_name"      # Optional: step icon
      altitud: "ft"           # Optional: show altitude (m or ft)
      qnh: "hPa"              # Optional: show QNH (hPa or inHg)
      link: "https://url"     # Optional: open URL when tapped
      app: "com.package.name" # Optional: launch app when tapped
      localtime: true         # Optional: show local time
      utctime: true           # Optional: show UTC time
      log: "Description"      # Optional: enable GPS logging with custom text
```

## Creating a Checklist

### Step 1: Plan Your Checklist

Before writing YAML, organize your checklist:
1. **Title**: What is this checklist called?
2. **Category**: Which procedure group? (e.g., "Normal Procedures", "Emergency Procedures")
3. **Steps**: List each item in order
4. **Icons**: Choose appropriate icons for visual recognition

### Step 2: Write the YAML

#### Simple Example
```yaml
version: 0.1
checklists:
  - id: pre_flight
    titulo: "Pre-Flight Check"
    categoria: "Normal Procedures"
    pasos:
      - id: pf1
        texto: "Documents on board"
        icono: "documento"
      - id: pf2
        texto: "Fuel quantity checked"
        icono: "combustible"
      - id: pf3
        texto: "Control surfaces free"
        icono: "control"
```

#### Compact Format (Advanced)
You can write steps in a more compact format:
```yaml
pasos:
  - { id: pf1, texto: "Documents on board", icono: "documento" }
  - { id: pf2, texto: "Fuel quantity checked", icono: "combustible" }
  - { id: pf3, texto: "Control surfaces free", icono: "control" }
```

Both formats are equivalent and produce the same result.

### Step 3: Validate

1. Open YAML Editor in the app
2. Paste or type your YAML
3. Watch for validation status at the bottom:
   - ✓ **"Valid YAML"** - Ready to save!
   - ✗ **"Validation Error"** - Check for typos or formatting issues

### Step 4: Save

1. Tap "Save" button
2. Return to home screen
3. Your new checklist appears in the appropriate category

## Available Icons

The app includes 40+ aviation-specific icons. Use the `icono` field with these names:

### General
- `check` - Checkmark
- `inspeccion` - Inspection/magnifying glass
- `documento` - Document/paperwork
- `carga` - Cargo/load
- `balanza` - Balance/weight scale
- `seguro` - Security/lock
- `salida` - Exit

### Aircraft Components
- `cabina` - Cockpit/canopy
- `alas` - Wings
- `aleron` - Aileron
- `flaps` - Flaps
- `aerofreno` - Airbrake/spoiler
- `timon` - Rudder
- `profundidad` - Elevator
- `trim` - Trim tab
- `tren` - Landing gear

### Engine & Propulsion
- `motor` - Engine
- `helice` - Propeller
- `combustible` - Fuel
- `aceite` - Oil/lubricant
- `bomba` - Fuel pump
- `refrigeracion` - Cooling
- `ignicion` - Ignition
- `gases` - Throttle
- `estrangulador` - Choke

### Electrical & Instruments
- `bateria` - Battery
- `interruptor` - Switch/master
- `generador` - Generator
- `luz` - Lights
- `antena` - Antenna
- `radio` - Radio/avionics
- `transponder` - Transponder
- `brujula` - Compass
- `instrumentos` - Instruments panel
- `altimetro` - Altimeter
- `anemometro` - Airspeed indicator
- `pitot` - Pitot tube
- `puerto` - Static port

### Controls
- `control` - Control stick/yoke
- `palanca` - Control lever
- `freno` - Brake
- `cinturon` - Seatbelt
- `llave` - Key

### Flight & Environment
- `vuelo` - Flight/aircraft
- `viento` - Wind
- `paracaidas` - Parachute
- `calefaccion` - Heating

### Other
- `boton` - Button

### Using Icons
```yaml
# Specify icon name in the icono field
- id: fuel_check
  texto: "Fuel quantity sufficient"
  icono: "combustible"

# Icons are optional - omit if no suitable icon exists
- id: generic_step
  texto: "Complete general check"
  # no icono field
```

## Display Modes

Checklists can default to either **step-by-step** or **full-list** mode:

### Step-by-Step Mode (Default)
Best for detailed procedures requiring careful attention.

```yaml
- id: engine_start
  titulo: "Engine Start"
  categoria: "Normal Procedures"
  full-list: false  # Or omit this line (false is default)
  pasos:
    # steps here
```

**Characteristics:**
- Shows one step at a time
- Large text for easy reading
- Good for complex procedures
- Prevents skipping steps

### Full-List Mode
Best for quick reviews and familiar procedures.

```yaml
- id: daily_inspection
  titulo: "Daily Inspection"
  categoria: "Normal Procedures"
  full-list: true  # Enable full-list mode
  pasos:
    # steps here
```

**Characteristics:**
- Shows all steps on one screen (paginated)
- Checkboxes for each item
- Quick overview
- Good for inspections

### Important Note
Users can **always** toggle between modes using the button in the top bar, regardless of the default setting. The `full-list` field only sets the initial display mode.

## GPS Logging Feature

Enable GPS-tracked logging for specific steps to record flight activities.

### What is GPS Logging?

The `log` field allows you to create log entries with GPS coordinates, altitude, timestamp, and custom text. When a step has a log field, a "Log" button appears during execution.

### Basic Usage

```yaml
- id: takeoff
  texto: "Takeoff checklist complete"
  icono: "vuelo"
  log: "Takeoff runway 32L"
```

When the user taps the "Log" button, an entry is created with:
- The text from the `log` field
- Current GPS coordinates (latitude/longitude)
- Current altitude in meters
- Timestamp (date and time)
- Nearest aerodrome ICAO code (if available from database)

### Use Cases

**Flight Operations:**
```yaml
- id: takeoff_log
  texto: "Ready for takeoff"
  icono: "vuelo"
  log: "Takeoff"
  altitud: ft

- id: landing_log
  texto: "Landing complete"
  icono: "tren"
  log: "Landing"
  altitud: ft
```

**Fuel Stops:**
```yaml
- id: fuel_log
  texto: "Refueling complete"
  icono: "combustible"
  log: "Fuel stop - 20 gallons added"
```

**Waypoints:**
```yaml
- id: waypoint_log
  texto: "Waypoint reached"
  icono: "vuelo"
  log: "Waypoint Alpha"
  altitud: ft
```

**Engine Events (Gliders):**
```yaml
- id: engine_start
  texto: "Engine started"
  icono: "motor"
  log: "Engine start"

- id: engine_stop
  texto: "Engine stopped"
  icono: "motor"
  log: "Engine stop"
```

### Combining with Other Features

You can combine the `log` field with other optional features:

```yaml
- id: cruise_log
  texto: "Cruise checkpoint"
  icono: "vuelo"
  log: "Cruise checkpoint - all systems normal"
  altitud: ft
  qnh: hPa
  localtime: true
  link: https://aviationweather.gov
```

### Viewing and Managing Logs

- **Log Viewer**: Access from home screen to view all entries
- **Export**: Logs automatically saved to `Download/FlightChecks/` as CSV
- **Share**: Send logs via email or messaging
- **Edit**: Modify log entry details after creation
- **Import**: Load existing CSV log files

### CSV Format

Exported logs use standard CSV format with columns:
- Timestamp
- Text/Description
- Latitude
- Longitude
- Altitude (meters)
- ICAO Code (if available)

This format is compatible with Excel, Google Sheets, and flight logging software.

## Best Practices

### 1. Use Clear, Actionable Text
✓ Good:
```yaml
texto: "Fuel valve OPEN"
texto: "Check oil pressure >50 PSI"
```

✗ Avoid:
```yaml
texto: "Fuel"
texto: "Check various things"
```

### 2. Keep IDs Unique and Meaningful
✓ Good:
```yaml
- id: preflight_documents
- id: preflight_fuel
- id: preflight_controls
```

✗ Avoid:
```yaml
- id: step1
- id: step2
- id: item_a
```

### 3. Group Related Checklists
Use consistent category names:
```yaml
# Normal operations
categoria: "Normal Procedures"

# Urgent situations
categoria: "Emergency Procedures"

# Soaring operations
categoria: "Soaring Procedures"
```

### 4. Use Appropriate Display Modes
- **full-list: true** for:
  - Pre-flight inspections
  - Daily checks
  - Quick reviews

- **full-list: false** for:
  - Engine start sequences
  - Emergency procedures
  - Complex multi-step operations

### 5. Choose Icons Wisely
- Use icons that match the step content
- Icons aid quick visual scanning
- Consistent icon use improves familiarity

### 6. Test Your Checklists
1. Create the YAML
2. Save in the app
3. Execute the checklist
4. Verify:
   - All steps are clear
   - Icons are appropriate
   - Order makes sense
   - No typos or errors

## Examples

### Example 1: Emergency Procedure
```yaml
version: 0.1
checklists:
  - id: engine_fire
    titulo: "Emergency - Engine Fire"
    categoria: "Emergency Procedures"
    full-list: false  # Step-by-step for emergencies
    pasos:
      - id: ef1
        texto: "Throttle IDLE"
        icono: "gases"
      - id: ef2
        texto: "Fuel valve CLOSED"
        icono: "combustible"
      - id: ef3
        texto: "Ignition OFF"
        icono: "ignicion"
      - id: ef4
        texto: "Master switch OFF"
        icono: "interruptor"
```

### Example 2: Daily Inspection
```yaml
version: 0.1
checklists:
  - id: daily_walk_around
    titulo: "Daily Walk-Around"
    categoria: "Normal Procedures"
    full-list: true  # Full-list for inspections
    pasos:
      - { id: dw1, texto: "Canopy clean and operational", icono: "cabina" }
      - { id: dw2, texto: "Wings: check for damage", icono: "alas" }
      - { id: dw3, texto: "Flaps operational", icono: "flaps" }
      - { id: dw4, texto: "Landing gear: tires and struts", icono: "tren" }
      - { id: dw5, texto: "Propeller: no damage", icono: "helice" }
      - { id: dw6, texto: "Fuel quantity checked", icono: "combustible" }
```

### Example 3: Multiple Checklists
```yaml
version: 0.1
checklists:
  - id: taxi
    titulo: "Taxi"
    categoria: "Normal Procedures"
    pasos:
      - { id: tx1, texto: "Brakes checked", icono: "freno" }
      - { id: tx2, texto: "Flight instruments set", icono: "instrumentos" }
      - { id: tx3, texto: "Controls free and correct", icono: "control" }

  - id: takeoff
    titulo: "Takeoff"
    categoria: "Normal Procedures"
    pasos:
      - { id: to1, texto: "Flaps 15°", icono: "flaps" }
      - { id: to2, texto: "Trim neutral", icono: "trim" }
      - { id: to3, texto: "Full throttle", icono: "gases" }
      - { id: to4, texto: "Rotate at 80 km/h", icono: "anemometro" }

  - id: spin_recovery
    titulo: "Emergency - Spin Recovery"
    categoria: "Emergency Procedures"
    pasos:
      - { id: sr1, texto: "Ailerons NEUTRAL", icono: "aleron" }
      - { id: sr2, texto: "Rudder FULL OPPOSITE", icono: "timon" }
      - { id: sr3, texto: "Stick FORWARD", icono: "palanca" }
```

## Troubleshooting

### Common Errors

#### "YAML validation error"
**Cause**: Syntax error in YAML format

**Solutions:**
1. Check indentation (must use 2 spaces, not tabs)
2. Ensure all quotes are closed: `"text"`
3. Check for missing colons after field names
4. Verify list items start with `- ` (dash + space)

#### "No checklists loaded"
**Cause**: All checklists have errors or file is empty

**Solutions:**
1. Verify `version: 0.1` is at the top
2. Ensure `checklists:` list exists
3. Check that at least one valid checklist is present
4. Try the examples from this guide

#### Checklist doesn't appear
**Cause**: Required fields missing

**Solutions:**
Verify each checklist has:
- `id` field (unique)
- `titulo` field
- `categoria` field
- `pasos` list with at least one step

#### Icons don't show
**Cause**: Icon name misspelled or doesn't exist

**Solutions:**
1. Check icon name against [Available Icons](#available-icons) list
2. Icon names are case-sensitive: use lowercase
3. If icon doesn't exist, omit the `icono` field

### Validation Tips

1. **Use the in-app editor**: Real-time validation catches errors immediately
2. **Start small**: Test with one checklist before adding more
3. **Copy working examples**: Modify existing checklists rather than starting from scratch
4. **Check indentation**: This is the #1 cause of YAML errors

### Testing Checklist

Before considering a YAML file complete:
- [ ] File validates without errors
- [ ] All checklists appear in home screen
- [ ] All checklists are in correct categories
- [ ] Each checklist opens and displays correctly
- [ ] Step text is clear and readable
- [ ] Icons are appropriate and display correctly
- [ ] Display mode (step-by-step vs full-list) is appropriate

## Advanced Tips

### Multi-line Text
For longer step descriptions:
```yaml
- id: step1
  texto: "Check fuel quantity: minimum 10 gallons for pattern work,
         20 gallons for cross-country flight"
  icono: "combustible"
```

### Comments
Add comments to your YAML (not shown in app):
```yaml
checklists:
  # This is a comment
  - id: startup
    titulo: "Startup"
    # Comments can explain your choices
    full-list: false  # Use step-by-step for safety
    pasos:
      - id: s1
        texto: "Master ON"
        icono: "interruptor"
```

### Organizing Large Files
Group related checklists together:
```yaml
version: 0.1
checklists:
  # === NORMAL PROCEDURES ===
  - id: pre_flight
    # ...
  - id: startup
    # ...

  # === EMERGENCY PROCEDURES ===
  - id: engine_fire
    # ...
  - id: engine_failure
    # ...
```

## Need More Help?

- **In-App Help**: Tap the Help button on the home screen
- **Examples**: Check `seed_english.yaml` in the repository root for a complete example
- **GitHub Issues**: Report problems or ask questions
- **README**: See README.md for app usage guide

---

**Last Updated**: 2025-11-12
**App Version**: 1.5.8
