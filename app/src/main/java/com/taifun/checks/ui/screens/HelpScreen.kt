package com.taifun.checks.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taifun.checks.R
import com.taifun.checks.ui.rememberHapticFeedback
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val haptic = rememberHapticFeedback()
    val scrollState = rememberScrollState()
    val ctx = LocalContext.current

    // Detectar idioma actual
    val isEnglish = remember {
        val locale = ctx.resources.configuration.locales[0]
        locale.language == "en"
    }

    // Obtener versión dinámica
    val versionName = remember {
        try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back)
                        )
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título principal
            Text(
                text = if (isEnglish) "Taifun Checks - Complete Manual" else "Taifun Checks - Manual Completo",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            // 1. Quick Start
            HelpCard(
                title = if (isEnglish) "1. Quick Start" else "1. Inicio Rápido",
                content = if (isEnglish) """
**Starting a Checklist:**
1. Select a checklist from the home screen
2. Follow each step carefully
3. Use Previous/Next buttons to navigate
4. Tap the toggle button to switch between modes

**Two Display Modes:**
• Step-by-Step: One item at a time (detailed)
• Full-List: All items with checkboxes (quick review)
                """.trimIndent() else """
**Iniciar un Checklist:**
1. Selecciona un checklist desde la pantalla principal
2. Sigue cada paso cuidadosamente
3. Usa los botones Anterior/Siguiente para navegar
4. Toca el botón de cambio para alternar entre modos

**Dos Modos de Visualización:**
• Paso a Paso: Un elemento a la vez (detallado)
• Lista Completa: Todos los elementos con casillas (revisión rápida)
                """.trimIndent()
            )

            // 2. Color Customization
            HelpCard(
                title = if (isEnglish) "2. Color Customization" else "2. Personalización de Colores",
                content = if (isEnglish) """
**Assign custom colors to checklists for quick visual identification.**

**Features:**
• 8 predefined colors: Red, Green, Blue, Yellow, Orange, Purple, Pink, Cyan
• Custom RGB: Enter any hex color (#RRGGBB)
• Real-time validation of custom colors
• Colors appear on:
  - Sidebar indicator (left edge of checklist card)
  - Button background in home screen
  - Icons during checklist execution

**How to Set Colors:**
1. Edit a checklist (tap ✏️ icon)
2. Tap "Select Color" button
3. Choose predefined color or enter custom hex
4. Tap "Accept" to save
5. Color applies immediately

**Remove Color:**
Select "None" to use default theme colors.
                """.trimIndent() else """
**Asigna colores personalizados a checklists para identificación visual rápida.**

**Características:**
• 8 colores predefinidos: Rojo, Verde, Azul, Amarillo, Naranja, Morado, Rosa, Cian
• RGB Personalizado: Ingresa cualquier color hex (#RRGGBB)
• Validación en tiempo real de colores personalizados
• Los colores aparecen en:
  - Indicador lateral (borde izquierdo de tarjeta)
  - Fondo del botón en pantalla principal
  - Iconos durante ejecución del checklist

**Cómo Establecer Colores:**
1. Edita un checklist (toca icono ✏️)
2. Toca botón "Seleccionar Color"
3. Elige color predefinido o ingresa hex personalizado
4. Toca "Aceptar" para guardar
5. El color se aplica inmediatamente

**Eliminar Color:**
Selecciona "Ninguno" para usar colores del tema por defecto.
                """.trimIndent()
            )

            // 3. Checklist Manager
            HelpCard(
                title = if (isEnglish) "3. Checklist Manager - Multiple Files" else "3. Gestor de Checklists - Archivos Múltiples",
                content = if (isEnglish) """
**Manage multiple YAML checklist files.**

**Access:**
Tap the ☰ (List) icon in home screen top bar

**Features:**
• Manage multiple YAML files
• Switch between different checklist sets
• One file is active at a time (shown with ✓)
• Import, export, edit, and delete files

**File Operations:**
• Select: Make a file active (tap "Select" button)
• Edit: Open YAML editor for that file (tap ✏️)
• Export: Share file (tap 🔗 Share icon)
• Delete: Remove file (tap 🗑️ with confirmation)
• Import: Add new YAML files (tap "Import" button)

**Use Cases:**
• Separate files for different aircraft
• Different procedures in different files
• Share specific checklist sets
• Organize by season or flight phase

**Active File:**
The active file is used for:
- Home screen display
- YAML editor modifications
- All checklist operations
                """.trimIndent() else """
**Gestiona múltiples archivos YAML de checklists.**

**Acceso:**
Toca el icono ☰ (Lista) en barra superior de pantalla principal

**Características:**
• Gestiona múltiples archivos YAML
• Cambia entre diferentes conjuntos de checklists
• Un archivo está activo a la vez (mostrado con ✓)
• Importa, exporta, edita y elimina archivos

**Operaciones de Archivo:**
• Seleccionar: Hacer un archivo activo (toca botón "Seleccionar")
• Editar: Abrir editor YAML para ese archivo (toca ✏️)
• Exportar: Compartir archivo (toca icono 🔗 Compartir)
• Eliminar: Borrar archivo (toca 🗑️ con confirmación)
• Importar: Añadir nuevos archivos YAML (toca botón "Importar")

**Casos de Uso:**
• Archivos separados para diferentes aeronaves
• Diferentes procedimientos en archivos distintos
• Compartir conjuntos específicos de checklists
• Organizar por temporada o fase de vuelo

**Archivo Activo:**
El archivo activo se usa para:
- Visualización en pantalla principal
- Modificaciones en editor YAML
- Todas las operaciones de checklist
                """.trimIndent()
            )

            // 4. Visual Editor
            HelpCard(
                title = if (isEnglish) "4. Visual Editor - Edit Without YAML" else "4. Editor Visual - Edita Sin YAML",
                content = if (isEnglish) """
**Edit checklists and categories directly in the app!**

**Edit a Checklist:**
1. Tap the ✏️ (edit) icon next to any checklist
2. Or tap ✏️ in the checklist toolbar while running it
3. Modify:
   • Title
   • Category
   • Default mode (Step-by-Step or Full-List)
   • Steps: add, edit, delete, or reorder
4. Tap ✓ (checkmark) to save changes

**Edit Steps:**
• Tap any step to edit its text and icon
• Tap + (floating button) to add a new step
• Use ↑↓ arrows to reorder steps
• Tap 🗑️ (trash) to delete a step

**Edit a Category:**
1. Tap the ✏️ (edit) icon next to any category name
2. Change the category name
3. All checklists in that category update automatically

**Navigation:**
• Back arrow (←) returns to previous screen
• Changes save to YAML automatically
                """.trimIndent() else """
**¡Edita checklists y categorías directamente en la app!**

**Editar un Checklist:**
1. Toca el icono ✏️ (editar) junto a cualquier checklist
2. O toca ✏️ en la barra mientras ejecutas el checklist
3. Modifica:
   • Título
   • Categoría
   • Modo por defecto (Paso a Paso o Lista Completa)
   • Pasos: añadir, editar, eliminar o reordenar
4. Toca ✓ (check) para guardar cambios

**Editar Pasos:**
• Toca cualquier paso para editar su texto e icono
• Toca + (botón flotante) para añadir un paso nuevo
• Usa flechas ↑↓ para reordenar pasos
• Toca 🗑️ (papelera) para eliminar un paso

**Editar una Categoría:**
1. Toca el icono ✏️ (editar) junto al nombre de categoría
2. Cambia el nombre de la categoría
3. Todos los checklists de esa categoría se actualizan automáticamente

**Navegación:**
• Flecha atrás (←) vuelve a la pantalla anterior
• Los cambios se guardan en YAML automáticamente
                """.trimIndent()
            )

            // 5. Display Modes
            HelpCard(
                title = if (isEnglish) "5. Display Modes" else "5. Modos de Visualización",
                content = if (isEnglish) """
**Step-by-Step Mode:**
• Shows one step at a time
• Large, easy-to-read text
• Perfect for complex procedures
• Icon for each step
• Step counter (e.g., "Step 3 of 15")
• Previous/Next navigation buttons

**Full-List Mode:**
• Shows ~10 steps per page
• Checkbox for each item
• Great for quick reviews
• Auto-advances when page complete
• Page counter (e.g., "Page 2 / 3")
• Returns to home when all complete

**Switching Modes:**
Tap the List/CheckCircle icon in the top bar anytime.
                """.trimIndent() else """
**Modo Paso a Paso:**
• Muestra un paso a la vez
• Texto grande y fácil de leer
• Perfecto para procedimientos complejos
• Icono para cada paso
• Contador de pasos (ej. "Paso 3 de 15")
• Botones de navegación Anterior/Siguiente

**Modo Lista Completa:**
• Muestra ~10 pasos por página
• Casilla para cada elemento
• Ideal para revisiones rápidas
• Avanza automáticamente al completar página
• Contador de páginas (ej. "Página 2 / 3")
• Vuelve al inicio al completar todo

**Cambiar de Modo:**
Toca el ícono de Lista/CheckCircle en la barra superior.
                """.trimIndent()
            )

            // 6. Voice Control (Multi-Language)
            HelpCard(
                title = if (isEnglish) "6. Voice Control - Multi-Language" else "6. Control por Voz - Multiidioma",
                content = if (isEnglish) """
**Hands-Free Navigation:**
Control the app with voice commands in Spanish or English.

**Spanish Commands:**
• "Anterior" → Previous step/page
• "Siguiente" → Next step/page

**English Commands:**
• "Previous" → Previous step/page
• "Next" → Next step/page

**How to Use:**
1. Tap the microphone icon (top bar)
2. Grant microphone permission when asked
3. Icon turns blue when listening
4. Speak command clearly in your selected language
5. Tap blue icon to disable

**Language Detection:**
• Uses your selected language from Settings
• Auto mode uses system language
• Works in both display modes!
                """.trimIndent() else """
**Navegación Manos Libres:**
Controla la app con comandos de voz en español o inglés.

**Comandos en Español:**
• "Anterior" → Paso/página anterior
• "Siguiente" → Paso/página siguiente

**Comandos en Inglés:**
• "Previous" → Paso/página anterior
• "Next" → Paso/página siguiente

**Cómo Usar:**
1. Toca el ícono del micrófono (barra superior)
2. Permite acceso al micrófono cuando se solicite
3. El ícono se pone azul al escuchar
4. Di el comando claramente en tu idioma seleccionado
5. Toca el ícono azul para desactivar

**Detección de Idioma:**
• Usa tu idioma seleccionado en Ajustes
• Modo Auto usa idioma del sistema
• ¡Funciona en ambos modos de visualización!
                """.trimIndent()
            )

            // 6a. Optional Step Features
            HelpCard(
                title = if (isEnglish) "6a. Optional Step Features" else "6a. Funciones Opcionales de Pasos",
                content = if (isEnglish) """
**Add dynamic data to steps:**

**1. Altitude Display**
• Show GPS altitude in real-time
• Format: `altitud: m` (meters) or `altitud: ft` (feet)
• Displayed with thousands separator, no decimals
• Requires location permission
• Example: `altitud: ft` → Shows "2,850 ft"

**2. QNH Calculation**
• Calculate QNH using barometer + GPS
• Format: `qnh: hPa` or `qnh: inHg`
• hPa: whole numbers with separator (e.g., "1,013 hPa")
• inHg: 2 decimals (e.g., "29.92 inHg")
• Requires location permission and barometer sensor
• Uses ICAO standard barometric formula

**3. Link Opening**
• Open URL when tapping step area (outside buttons)
• Format: `link: https://example.com`
• Opens in default browser
• Silent fail if URL invalid

**4. App Launching**
• Open app when tapping step area (outside buttons)
• Format: `app: com.package.name`
• **Use package name, NOT app display name**
• Silent fail if app not installed

**Common Examples:**
• Google Maps: `com.google.android.apps.maps`
• Chrome: `com.android.chrome`
• WhatsApp: `com.whatsapp`
• Gmail: `com.google.android.gm`

**How to find package name:**
  - Settings → Apps → App Info (look for package)
  - Or use "App Inspector" app from Play Store
  - Or check Play Store URL: play.google.com/store/apps/details?id=PACKAGE_NAME

**5. Time Display**
• Show current time in step
• `localtime: true` → Local time (HH:mm:ss)
• `utctime: true` → UTC time (HH:mm:ss UTC)
• Updates in real-time

**Multiple Features:**
You can combine multiple features in one step!

**Example in YAML:**
```yaml
- id: altitude_check
  texto: "Verify altitude"
  icono: "altitud"
  altitud: ft
  qnh: inHg
  localtime: true
  link: https://weather.com
```

**Adding via Editor:**
Edit any step → Scroll to "Optional Features" section
                """.trimIndent() else """
**Añade datos dinámicos a los pasos:**

**1. Visualización de Altitud**
• Muestra altitud GPS en tiempo real
• Formato: `altitud: m` (metros) o `altitud: ft` (pies)
• Se muestra con separador de miles, sin decimales
• Requiere permiso de ubicación
• Ejemplo: `altitud: ft` → Muestra "2.850 ft"

**2. Cálculo de QNH**
• Calcula QNH usando barómetro + GPS
• Formato: `qnh: hPa` o `qnh: inHg`
• hPa: números enteros con separador (ej: "1.013 hPa")
• inHg: 2 decimales (ej: "29,92 inHg")
• Requiere permiso de ubicación y sensor barómetro
• Usa fórmula barométrica estándar OACI

**3. Abrir Enlace**
• Abre URL al tocar área del paso (fuera de botones)
• Formato: `link: https://ejemplo.com`
• Abre en navegador predeterminado
• Fallo silencioso si URL inválida

**4. Lanzar Aplicación**
• Abre app al tocar área del paso (fuera de botones)
• Formato: `app: com.paquete.nombre`
• **Usar package name, NO el nombre visible**
• Fallo silencioso si app no instalada

**Ejemplos Comunes:**
• Google Maps: `com.google.android.apps.maps`
• Chrome: `com.android.chrome`
• WhatsApp: `com.whatsapp`
• Gmail: `com.google.android.gm`

**Cómo encontrar el package name:**
  - Ajustes → Aplicaciones → Info de App (buscar paquete)
  - O usar app "App Inspector" de Play Store
  - O ver URL de Play Store: play.google.com/store/apps/details?id=NOMBRE_PAQUETE

**5. Mostrar Hora**
• Muestra hora actual en el paso
• `localtime: true` → Hora local (HH:mm:ss)
• `utctime: true` → Hora UTC (HH:mm:ss UTC)
• Se actualiza en tiempo real

**Múltiples Funciones:**
¡Puedes combinar varias funciones en un paso!

**Ejemplo en YAML:**
```yaml
- id: chequeo_altitud
  texto: "Verificar altitud"
  icono: "altitud"
  altitud: ft
  qnh: inHg
  localtime: true
  link: https://tiempo.com
```

**Añadir mediante Editor:**
Edita cualquier paso → Desplázate a "Funciones Opcionales"
                """.trimIndent()
            )

            // 6b. GPS Logging & Log Viewer
            HelpCard(
                title = if (isEnglish) "6b. GPS Logging & Log Viewer" else "6b. Logging GPS y Visor de Log",
                content = if (isEnglish) """
**Record your flight activities with GPS-tracked log entries.**

**Using the Log Feature:**
1. Add `log: "Description"` to any step in YAML
2. Example: `log: "Takeoff runway 32L"`
3. A "Log" button appears on that step during execution
4. Tap the button to create a log entry with:
   • Your custom text
   • Current GPS coordinates (lat/lon)
   • Current altitude
   • Timestamp
   • Nearest aerodrome ICAO code (if available)

**Creating Custom Log Entries:**
• Tap the ✏️ floating button in the Log Viewer screen
• Enter custom text for your log entry
• Current GPS data, altitude, and UTC time are automatically recorded
• ICAO code included if near an aerodrome (within 2km and speed < 40 km/h)
• Useful for logging events not tied to specific checklist steps
• Examples: flight start, fuel stops, incidents, observations

**ICAO Code Detection:**
• Automatically detects when at an aerodrome using:
  - Horizontal distance < 2 km from aerodrome
  - Altitude difference < 50 m from aerodrome elevation
• Database: 85,333+ aerodromes worldwide with elevation
• More reliable than speed-based detection
• Works in hangars, parking areas, without active GPS speed
• Won't detect aerodromes when flying over them

**Automatic Engine Logging (Gliders):**
• Engine start/stop events logged automatically
• No manual intervention required
• Tracks engine usage for maintenance records

**Accessing Log Viewer:**
• Open from home screen menu
• View all entries in chronological order
• See total entry count
• Each entry shows: timestamp, text, coordinates, altitude, ICAO

**Managing Log Entries:**
• Edit: Tap ✏️ to modify entry details
• Delete: Tap 🗑️ to remove individual entries
• Clear All: Menu option to clear entire log (with confirmation)

**Exporting Logs:**
• Auto-save: Logs saved to Download/FlightChecks/ as CSV
• Manual Export: Tap "Save CSV" for custom location
• Share: Send via email, messaging, etc.
• CSV format: Compatible with Excel, Google Sheets, flight logging software

**Importing Logs:**
• Tap "Import CSV" to load existing log files
• Import overwrites current log (confirmation required)
• Useful for merging logs or restoring backups

**Use Cases:**
• Track takeoff/landing times
• Record fuel stops and quantities
• Log maintenance events
• Document flight route waypoints
• Track engine usage (gliders)
• Maintain flight records for logbook
• Share flight data with instructors/clubs

**YAML Example:**
```yaml
- id: takeoff_log
  texto: "Record takeoff"
  icono: "vuelo"
  log: "Takeoff runway 32L"
  altitud: ft
```

**CSV Format:**
Columns: Timestamp, Text, Latitude, Longitude, Altitude (m), ICAO Code
                """.trimIndent() else """
**Registra tus actividades de vuelo con entradas de log rastreadas por GPS.**

**Usar la Función de Log:**
1. Añade `log: "Descripción"` a cualquier paso en YAML
2. Ejemplo: `log: "Despegue pista 32L"`
3. Aparece un botón "Log" en ese paso durante la ejecución
4. Toca el botón para crear una entrada con:
   • Tu texto personalizado
   • Coordenadas GPS actuales (lat/lon)
   • Altitud actual
   • Marca de tiempo
   • Código ICAO del aeródromo más cercano (si disponible)

**Crear Entradas de Log Personalizadas:**
• Toca el botón flotante ✏️ en la pantalla del Visor de Log
• Introduce el texto personalizado para tu entrada de log
• Los datos GPS actuales, altitud y hora UTC se registran automáticamente
• Código ICAO incluido si estás cerca de un aeródromo (2km y velocidad < 40 km/h)
• Útil para registrar eventos no vinculados a pasos específicos de checklist
• Ejemplos: inicio de vuelo, paradas de combustible, incidentes, observaciones

**Detección de Código ICAO:**
• Detecta automáticamente cuando estás en un aeródromo usando:
  - Distancia horizontal < 2 km del aeródromo
  - Diferencia de altitud < 50 m de la elevación del aeródromo
• Base de datos: 85,333+ aeródromos mundiales con elevación
• Más fiable que la detección basada en velocidad
• Funciona en hangares, áreas de parking, sin velocidad GPS activa
• No detectará aeródromos al sobrevolar

**Logging Automático de Motor (Veleros):**
• Eventos de encendido/apagado del motor se registran automáticamente
• Sin intervención manual requerida
• Rastrea uso del motor para registros de mantenimiento

**Acceder al Visor de Log:**
• Abre desde el menú de pantalla principal
• Ver todas las entradas en orden cronológico
• Ver contador total de entradas
• Cada entrada muestra: timestamp, texto, coordenadas, altitud, ICAO

**Gestionar Entradas de Log:**
• Editar: Toca ✏️ para modificar detalles de entrada
• Eliminar: Toca 🗑️ para borrar entradas individuales
• Borrar Todo: Opción de menú para limpiar log completo (con confirmación)

**Exportar Logs:**
• Auto-guardado: Logs guardados en Download/FlightChecks/ como CSV
• Exportar Manual: Toca "Guardar CSV" para ubicación personalizada
• Compartir: Envía por email, mensajería, etc.
• Formato CSV: Compatible con Excel, Google Sheets, software de logging de vuelos

**Importar Logs:**
• Toca "Importar CSV" para cargar archivos de log existentes
• Importar sobrescribe log actual (confirmación requerida)
• Útil para fusionar logs o restaurar respaldos

**Casos de Uso:**
• Rastrear tiempos de despegue/aterrizaje
• Registrar paradas de combustible y cantidades
• Log de eventos de mantenimiento
• Documentar waypoints de ruta de vuelo
• Rastrear uso de motor (veleros)
• Mantener registros de vuelo para bitácora
• Compartir datos de vuelo con instructores/clubes

**Ejemplo YAML:**
```yaml
- id: log_despegue
  texto: "Registrar despegue"
  icono: "vuelo"
  log: "Despegue pista 32L"
  altitud: ft
```

**Formato CSV:**
Columnas: Timestamp, Texto, Latitud, Longitud, Altitud (m), Código ICAO
                """.trimIndent()
            )

            // 6c. GPS Accuracy & Checklist Completion
            HelpCard(
                title = if (isEnglish) "6c. GPS Accuracy & Completion" else "6c. Precisión GPS y Finalización",
                content = if (isEnglish) """
**GPS Accuracy for Logging**
When creating log entries, the app validates GPS data quality:
• **Accuracy**: ≤50 meters (internal GPS) or any value (Bluetooth/NMEA)
• **Altitude validity**: Must be actually measured, not default 0.0
• **Fix freshness**: Data must be less than 60 seconds old
• **Progress indicator** shown while waiting for valid data

**Timeout Options** (after 30 seconds):
• **Cancel**: Abort log entry
• **Keep searching**: Continue waiting for valid GPS
• **Save anyway**: Save with current data (even if invalid)

This ensures your flight logs have accurate position data, especially useful for:
• Tracking takeoff/landing locations
• Recording waypoints during flight
• Maintaining accurate flight records

**Checklist Completion Screen**
When you finish a checklist, a full-screen completion view appears:
• Shows "Checklist Completed" message with check icon
• Large buttons optimized for glove use
• **Exit button (right)**: Returns to home AND resets progress
• **Back button (left)**: Returns to previous step to review
• **Voice control**: Say "siguiente/next" to exit, "anterior/previous" to go back

This applies to both modes:
• Step-by-step: When pressing Next on last step
• Full-list: When all checkboxes are checked on last page

**Benefits:**
• Prevents accidental exits before finishing
• Large buttons work well with gloves
• Voice commands for hands-free operation
• Quick reset for repeated use
                """.trimIndent() else """
**Precisión GPS para Logging**
Al crear entradas de log, la app valida la calidad de los datos GPS:
• **Precisión**: ≤50 metros (GPS interno) o cualquier valor (Bluetooth/NMEA)
• **Validez de altitud**: Debe ser realmente medida, no el valor por defecto 0.0
• **Frescura del fix**: Datos deben tener menos de 60 segundos de antigüedad
• **Indicador de progreso** mientras espera datos válidos

**Opciones tras Timeout** (después de 30 segundos):
• **Cancelar**: Abortar entrada de log
• **Seguir buscando**: Continuar esperando GPS válido
• **Guardar igualmente**: Guardar con datos actuales (aunque sean inválidos)

Esto asegura que tus logs de vuelo tengan datos de posición precisos, útil para:
• Rastrear ubicaciones de despegue/aterrizaje
• Registrar waypoints durante el vuelo
• Mantener registros de vuelo precisos

**Pantalla de Finalización de Checklist**
Al terminar un checklist, aparece una pantalla completa de finalización:
• Muestra mensaje "Checklist Completado" con icono de check
• Botones grandes optimizados para uso con guantes
• **Botón Salir (derecha)**: Regresa al inicio Y reinicia progreso
• **Botón Volver (izquierda)**: Regresa al paso anterior para revisar
• **Control por voz**: Di "siguiente/next" para salir, "anterior/previous" para volver

Aplica a ambos modos:
• Paso a paso: Al presionar Siguiente en el último paso
• Lista completa: Cuando todas las casillas están marcadas en la última página

**Beneficios:**
• Evita salidas accidentales antes de terminar
• Botones grandes funcionan bien con guantes
• Comandos de voz para operación manos libres
• Reinicio rápido para uso repetido
                """.trimIndent()
            )

            // 7. YAML Editor
            HelpCard(
                title = if (isEnglish) "8. YAML Editor - Advanced Editing" else "8. Editor YAML - Edición Avanzada",
                content = if (isEnglish) """
**Edit checklists in-app with real-time validation.**

**Basic Structure:**
```yaml
version: 0.1
checklists:
  - id: pre_flight
    titulo: "Pre-Flight Check"
    categoria: "Normal Procedures"
    full-list: false
    pasos:
      - id: step1
        texto: "Documents verified"
        icono: "documento"
      - id: step2
        texto: "Fuel checked"
        icono: "combustible"
```

**Required Fields:**
• id: Unique identifier (no spaces)
• titulo: Display name
• categoria: Category/group name
• pasos: List of steps
  • id: Step identifier
  • texto: Step description
  • icono: Icon name (optional)

**Optional Fields:**
• full-list: true/false (default mode)
• color: Hex color code (e.g., "#4CAF50")

**Features:**
• Edits active checklist file (shown in status)
• Real-time validation (shows errors)
• Import YAML files (replaces active file content)
• Export active file
• Save to apply changes

**Access:**
From Checklist Manager, tap ✏️ on any file to edit it
                """.trimIndent() else """
**Edita checklists en la app con validación en tiempo real.**

**Estructura Básica:**
```yaml
version: 0.1
checklists:
  - id: pre_vuelo
    titulo: "Chequeo Pre-Vuelo"
    categoria: "Procedimientos Normales"
    full-list: false
    pasos:
      - id: paso1
        texto: "Documentos verificados"
        icono: "documento"
      - id: paso2
        texto: "Combustible chequeado"
        icono: "combustible"
```

**Campos Requeridos:**
• id: Identificador único (sin espacios)
• titulo: Nombre a mostrar
• categoria: Nombre de categoría/grupo
• pasos: Lista de pasos
  • id: Identificador del paso
  • texto: Descripción del paso
  • icono: Nombre del icono (opcional)

**Campos Opcionales:**
• full-list: true/false (modo por defecto)
• color: Código de color hex (ej., "#4CAF50")

**Funciones:**
• Edita archivo de checklist activo (mostrado en estado)
• Validación en tiempo real (muestra errores)
• Importar archivos YAML (reemplaza contenido del archivo activo)
• Exportar archivo activo
• Guardar para aplicar cambios

**Acceso:**
Desde Gestor de Checklists, toca ✏️ en cualquier archivo para editarlo
                """.trimIndent()
            )

            // 8. Available Icons
            HelpCard(
                title = if (isEnglish) "9. Available Icons (40+)" else "9. Iconos Disponibles (40+)",
                content = if (isEnglish) """
**General:**
check, inspeccion, documento, carga, balanza, seguro, salida

**Aircraft:**
cabina, alas, aleron, flaps, aerofreno, timon, profundidad, trim, tren

**Engine:**
motor, helice, combustible, aceite, bomba, refrigeracion, ignicion, gases, estrangulador

**Electrical:**
bateria, interruptor, generador, luz, antena, radio, transponder, brujula, instrumentos, altimetro, anemometro, pitot, puerto

**Controls:**
control, palanca, freno, cinturon, llave

**Flight:**
vuelo, viento, paracaidas, calefaccion

**Other:**
boton

**Usage:**
```yaml
- id: fuel
  texto: "Fuel checked"
  icono: "combustible"
```
                """.trimIndent() else """
**Generales:**
check, inspeccion, documento, carga, balanza, seguro, salida

**Aeronave:**
cabina, alas, aleron, flaps, aerofreno, timon, profundidad, trim, tren

**Motor:**
motor, helice, combustible, aceite, bomba, refrigeracion, ignicion, gases, estrangulador

**Eléctricos:**
bateria, interruptor, generador, luz, antena, radio, transponder, brujula, instrumentos, altimetro, anemometro, pitot, puerto

**Controles:**
control, palanca, freno, cinturon, llave

**Vuelo:**
vuelo, viento, paracaidas, calefaccion

**Otros:**
boton

**Uso:**
```yaml
- id: combustible
  texto: "Combustible chequeado"
  icono: "combustible"
```
                """.trimIndent()
            )

            // 9. YAML Tips
            HelpCard(
                title = if (isEnglish) "10. YAML Editing Tips" else "10. Consejos para Editar YAML",
                content = if (isEnglish) """
**Indentation:**
• Use 2 spaces (NOT tabs!)
• Must be consistent
• Most common error source

**Strings:**
• Use quotes for special characters: : ? - [ ] { }
• Example: "Question: Is fuel OK?"

**Compact Format:**
```yaml
- { id: s1, texto: "Step 1", icono: "check" }
```

**Extended Format:**
```yaml
- id: s1
  texto: "Step 1"
  icono: "check"
```

**Both formats work the same!**

**Common Errors:**
• Wrong indentation → validation error
• Missing colon → validation error
• Unclosed quotes → validation error

**Testing:**
1. Edit YAML in editor
2. Watch validation status
3. Save when "Valid YAML" shows
4. Check home screen for changes
                """.trimIndent() else """
**Indentación:**
• Usa 2 espacios (¡NO tabuladores!)
• Debe ser consistente
• Fuente más común de errores

**Cadenas:**
• Usa comillas para caracteres especiales: : ? - [ ] { }
• Ejemplo: "Pregunta: ¿Combustible OK?"

**Formato Compacto:**
```yaml
- { id: p1, texto: "Paso 1", icono: "check" }
```

**Formato Extendido:**
```yaml
- id: p1
  texto: "Paso 1"
  icono: "check"
```

**¡Ambos formatos funcionan igual!**

**Errores Comunes:**
• Indentación incorrecta → error de validación
• Falta dos puntos → error de validación
• Comillas sin cerrar → error de validación

**Pruebas:**
1. Edita YAML en el editor
2. Observa el estado de validación
3. Guarda cuando muestre "YAML válido"
4. Revisa la pantalla principal para ver cambios
                """.trimIndent()
            )

            // 10. Settings
            HelpCard(
                title = if (isEnglish) "11. Settings & Customization" else "11. Ajustes y Personalización",
                content = if (isEnglish) """
**Theme:**
• Dark Theme: On/Off (reduces eye strain)
• High Contrast: Maximum readability in bright conditions

**Screen:**
• Keep Screen On: Prevents timeout during use (uses more battery)

**Language:**
• Auto (System): Uses device language
• Spanish: Force Spanish interface
• English: Force English interface
Note: Changes require app restart

**Haptics:**
• Haptic Feedback: Vibrations on button press
• Useful in noisy environments
• Disable to save battery

**All settings saved automatically.**
                """.trimIndent() else """
**Tema:**
• Tema Oscuro: Activar/Desactivar (reduce fatiga visual)
• Alto Contraste: Máxima legibilidad en condiciones brillantes

**Pantalla:**
• Pantalla Siempre Encendida: Evita apagado durante uso (usa más batería)

**Idioma:**
• Auto (Sistema): Usa idioma del dispositivo
• Español: Forzar interfaz en español
• Inglés: Forzar interfaz en inglés
Nota: Los cambios requieren reiniciar la app

**Hápticos:**
• Respuesta Háptica: Vibraciones al tocar botones
• Útil en ambientes ruidosos
• Desactivar para ahorrar batería

**Todos los ajustes se guardan automáticamente.**
                """.trimIndent()
            )

            // 11. Import/Export
            HelpCard(
                title = if (isEnglish) "12. Import/Export Checklists" else "12. Importar/Exportar Checklists",
                content = if (isEnglish) """
**Exporting from Manager:**
1. Open Checklist Manager
2. Tap 🔗 (Share) icon on any file
3. Choose save location
4. Share file with others

**Exporting from Editor:**
1. Open YAML Editor (edits active file)
2. Tap "Export" button
3. Choose save location

**Importing:**
1. Open Checklist Manager
2. Tap "Import" button at bottom
3. Select YAML file from device
4. New file added to manager
5. Select it to make it active

**Use Cases:**
• Share with other pilots
• Backup your checklists
• Edit on computer, import to app
• Maintain multiple checklist sets for different aircraft
• Organize procedures by season or flight type

**File Format:** Standard YAML (.yaml or .yml)
                """.trimIndent() else """
**Exportar desde Gestor:**
1. Abre Gestor de Checklists
2. Toca icono 🔗 (Compartir) en cualquier archivo
3. Elige ubicación para guardar
4. Comparte archivo con otros

**Exportar desde Editor:**
1. Abre Editor YAML (edita archivo activo)
2. Toca botón "Exportar"
3. Elige ubicación para guardar

**Importar:**
1. Abre Gestor de Checklists
2. Toca botón "Importar" al final
3. Selecciona archivo YAML del dispositivo
4. Nuevo archivo se añade al gestor
5. Selecciónalo para hacerlo activo

**Casos de Uso:**
• Compartir con otros pilotos
• Respaldar tus checklists
• Editar en computadora, importar a app
• Mantener múltiples conjuntos de checklists para diferentes aeronaves
• Organizar procedimientos por temporada o tipo de vuelo

**Formato de Archivo:** YAML estándar (.yaml o .yml)
                """.trimIndent()
            )

            // 12. Complete Example
            HelpCard(
                title = if (isEnglish) "13. Complete Example" else "13. Ejemplo Completo",
                content = if (isEnglish) """
**Full Working Example:**

```yaml
version: 0.1
checklists:
  # Normal procedure
  - id: engine_start
    titulo: "Engine Start"
    categoria: "Normal Procedures"
    full-list: false
    color: "#4CAF50"
    pasos:
      - id: es1
        texto: "Throttle IDLE"
        icono: "gases"
      - id: es2
        texto: "Fuel valve OPEN"
        icono: "combustible"
      - id: es3
        texto: "Master switch ON"
        icono: "interruptor"
      - id: es4
        texto: "Ignition ON"
        icono: "ignicion"
      - id: es5
        texto: "Starter PRESS"
        icono: "boton"

  # Emergency procedure
  - id: engine_fire
    titulo: "Engine Fire"
    categoria: "Emergency"
    full-list: false
    pasos:
      - id: ef1
        texto: "Throttle IDLE"
        icono: "gases"
      - id: ef2
        texto: "Fuel OFF"
        icono: "combustible"
      - id: ef3
        texto: "Master OFF"
        icono: "interruptor"
```

**Copy this and edit in YAML Editor!**
                """.trimIndent() else """
**Ejemplo Completo Funcional:**

```yaml
version: 0.1
checklists:
  # Procedimiento normal
  - id: arranque_motor
    titulo: "Arranque Motor"
    categoria: "Procedimientos Normales"
    full-list: false
    color: "#4CAF50"
    pasos:
      - id: am1
        texto: "Gases RALENTÍ"
        icono: "gases"
      - id: am2
        texto: "Paso gasolina ABIERTO"
        icono: "combustible"
      - id: am3
        texto: "Interruptor principal ON"
        icono: "interruptor"
      - id: am4
        texto: "Ignición ON"
        icono: "ignicion"
      - id: am5
        texto: "Arranque PRESIONAR"
        icono: "boton"

  # Procedimiento emergencia
  - id: incendio_motor
    titulo: "Incendio Motor"
    categoria: "Emergencias"
    full-list: false
    pasos:
      - id: im1
        texto: "Gases RALENTÍ"
        icono: "gases"
      - id: im2
        texto: "Combustible OFF"
        icono: "combustible"
      - id: im3
        texto: "Interruptor principal OFF"
        icono: "interruptor"
```

**¡Copia esto y edítalo en el Editor YAML!**
                """.trimIndent()
            )

            // Safety Warning
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEnglish) "⚠️ SAFETY WARNING" else "⚠️ AVISO DE SEGURIDAD",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isEnglish) """
This app assists with aviation procedures but should NEVER be the sole reference for flight operations.

Always:
• Follow official aircraft manuals
• Maintain proper pilot certification
• Use in accordance with aviation regulations
• Verify all items against official documentation

The developers assume NO liability for use in actual flight operations.
                        """.trimIndent() else """
Esta app asiste con procedimientos de aviación pero NUNCA debe ser la única referencia para operaciones de vuelo.

Siempre:
• Sigue los manuales oficiales de la aeronave
• Mantén la certificación de piloto apropiada
• Usa de acuerdo con regulaciones de aviación
• Verifica todos los elementos contra documentación oficial

Los desarrolladores NO asumen responsabilidad por uso en operaciones de vuelo reales.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Version Info
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isEnglish) "Version $versionName" else "Versión $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isEnglish) "© 2025 Taifun Checks" else "© 2025 Taifun Checks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HelpCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = if (content.contains("```")) FontFamily.Monospace else FontFamily.Default
            )
        }
    }
}
