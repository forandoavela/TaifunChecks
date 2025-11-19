# Política de Privacidad / Privacy Policy

**Última actualización / Last updated**: 2025-11-19
**Versión / Version**: 1.0.00.00

---

## Español (English below)

### 1. Introducción

Taifun Checks es una aplicación de checklists de aviación desarrollada por Jorge Mármol. Esta política de privacidad explica cómo la aplicación recopila, utiliza y protege su información personal.

### 2. Información que Recopilamos

#### 2.1 Datos de Ubicación (GPS)

La aplicación utiliza el GPS de dos formas distintas y opcionales:

##### A) Uso Temporal para Altitud y QNH (NO SE GUARDA)

**Propósito:**
- Mostrar la altitud actual en tiempo real durante los procedimientos de checklist
- Calcular el QNH (ajuste de presión atmosférica) usando la altitud GPS y el barómetro del dispositivo según la fórmula estándar ICAO
- Ayudar en la calibración del altímetro durante las fases de rodaje y pre-vuelo

**Privacidad:**
- **Solo se utiliza la altitud**, no las coordenadas geográficas (latitud/longitud)
- Los datos de altitud GPS se procesan **en tiempo real** y **NO se guardan**
- **No se almacena** ningún dato en el dispositivo
- **No se transmite** ningún dato a servidores externos
- Este uso es **completamente opcional**: la aplicación funciona plenamente sin activar esta función

##### B) Flight Logging - Registro de Vuelo (SE GUARDA LOCALMENTE, OPCIONAL)

**Propósito:**
- Permitir al usuario crear registros de actividades de vuelo cuando lo desee explícitamente
- Documentar eventos importantes: despegues, aterrizajes, paradas de combustible, waypoints, etc.

**Cómo Funciona:**
- **Requiere acción explícita del usuario**: Solo se registran datos cuando el usuario toca el botón "Log" en pasos específicos del checklist
- **Totalmente opcional**: El usuario decide qué pasos tienen capacidad de logging y cuándo activarlo
- **Control total del usuario**: El usuario puede ver, editar y eliminar cualquier entrada del log en cualquier momento

**Datos Guardados (SOLO cuando el usuario crea una entrada de log):**
- Coordenadas GPS (latitud y longitud) en el momento de tocar el botón
- Altitud en metros
- Timestamp (fecha y hora)
- Texto personalizado definido por el usuario
- Código ICAO del aeródromo más cercano (si está disponible en la base de datos local)

**Almacenamiento:**
- **Todos los datos se almacenan localmente** en el dispositivo del usuario
- Archivos CSV guardados en `Download/FlightChecks/` (accesible por el usuario)
- **No hay transmisión automática** a servidores externos
- **No hay sincronización en la nube**
- El usuario puede eliminar todos los logs en cualquier momento

**Exportar y Compartir (OPCIONAL):**
- El usuario puede **opcionalmente** exportar sus logs a CSV
- El usuario puede **opcionalmente** compartir sus logs por email, mensajería, etc.
- Estas acciones son **explícitas y controladas por el usuario**

**Permisos solicitados:**
- `ACCESS_FINE_LOCATION`: Para obtener lecturas precisas de altitud GPS y coordenadas para logging
- `ACCESS_COARSE_LOCATION`: Como respaldo para lecturas de GPS

#### 2.2 Datos del Barómetro

- La aplicación accede al sensor de presión barométrica del dispositivo (si está disponible)
- Se usa junto con la altitud GPS para calcular el QNH
- Los datos del barómetro se procesan **localmente** en el dispositivo
- **No se almacenan ni transmiten** fuera del dispositivo

#### 2.3 Datos de Audio

- Si activa el control por voz, la aplicación accede al micrófono
- El reconocimiento de voz se procesa mediante la API de Android del sistema
- **No se graban, almacenan ni transmiten conversaciones**
- Solo se procesan comandos de navegación específicos ("Anterior", "Siguiente", etc.)

#### 2.4 Archivos de Checklist

- Los checklists creados o modificados se almacenan **localmente** en su dispositivo
- La aplicación **no tiene acceso a internet** para transmitir datos
- Solo usted controla el contenido de sus checklists

### 3. Cómo Utilizamos la Información

#### 3.1 Uso de Datos de Ubicación

##### A) Uso Temporal (Altitud/QNH)

Los datos de altitud GPS se utilizan **exclusivamente** para:
- Mostrar la altitud actual en tiempo real en pantalla
- Calcular el QNH usando la fórmula barométrica estándar ICAO
- Proporcionar información de referencia para la calibración del altímetro

Estos datos **NO se guardan** en ningún momento.

##### B) Flight Logging (Cuando el Usuario lo Activa Explícitamente)

Los datos de GPS se utilizan **solo cuando el usuario toca el botón "Log"** para:
- Crear registros de actividades de vuelo elegidas por el usuario
- Documentar eventos importantes con coordenadas y altitud
- Mantener un historial de vuelo local para referencia personal
- Permitir al usuario exportar sus propios datos si lo desea

**Lo que NO hacemos con sus datos de ubicación:**
- ❌ No rastreamos su ubicación en segundo plano
- ❌ No creamos perfiles de ubicación o patrones de vuelo sin su conocimiento
- ❌ No compartimos datos de ubicación con terceros
- ❌ No enviamos datos a servidores externos
- ❌ No utilizamos datos de ubicación para publicidad
- ❌ No accedemos a sus logs sin su acción explícita

**Control del Usuario:**
- ✅ Usted decide cuándo crear una entrada de log
- ✅ Usted puede ver todos sus logs en cualquier momento
- ✅ Usted puede editar o eliminar cualquier entrada
- ✅ Usted puede borrar todos los logs cuando desee
- ✅ Usted controla si exporta o comparte sus datos

### 4. Almacenamiento de Datos

- **Todos los datos se almacenan localmente** en su dispositivo
- No hay sincronización en la nube
- No hay servidores remotos
- No hay transmisión automática de datos a internet

**Datos almacenados localmente:**
- Archivos YAML de checklist
- Preferencias de la aplicación (idioma, tema, checklist activo)
- Estado del primer inicio
- **Flight logs** (SOLO cuando el usuario crea entradas explícitamente):
  - Archivos CSV en `Download/FlightChecks/`
  - Coordenadas GPS, altitud, timestamps elegidos por el usuario
  - Completamente bajo control del usuario (puede ver, editar, eliminar en cualquier momento)

**Datos NO almacenados (Uso temporal de GPS):**
- Datos de ubicación GPS para altitud/QNH (solo procesados en tiempo real)
- Lecturas de altitud temporal
- Lecturas de presión barométrica temporal
- Audio del micrófono
- Historial de navegación en la app

### 5. Compartir Información

**No compartimos ninguna información con terceros automáticamente.**

La aplicación funciona de forma **completamente offline** y:
- No requiere conexión a internet para su funcionamiento básico
- No transmite datos a servidores externos de forma automática
- No incluye servicios de análisis de terceros
- No incluye publicidad
- No se conecta a servicios en la nube

**Excepciones (SOLO con acción explícita del usuario):**

1. **Enlaces en pasos del checklist**: Si utiliza la función de "Abrir enlace" en un paso del checklist, su navegador predeterminado abrirá la URL especificada, sujeta a la política de privacidad de ese sitio web.

2. **Exportar/Compartir Flight Logs**: Si **usted elige** exportar o compartir sus flight logs:
   - **Es completamente opcional**: La aplicación NUNCA comparte logs automáticamente
   - **Control total del usuario**: Usted decide qué compartir, cuándo y con quién
   - **Acción explícita requerida**: Debe tocar el botón de "Compartir" o "Exportar"
   - **Sin acceso de terceros**: La app no tiene acceso a dónde envía usted sus logs
   - Los logs compartidos quedan sujetos a las políticas de privacidad del servicio que usted elija (email, mensajería, etc.)

### 6. Seguridad de los Datos

- Todos los datos se almacenan en el espacio privado de la aplicación
- Los archivos de checklist son accesibles solo por la aplicación
- Los datos de ubicación GPS temporal (altitud/QNH) se procesan en memoria y no se persisten
- Los flight logs se almacenan en `Download/FlightChecks/` para que el usuario tenga acceso directo
- No hay transmisión automática de datos a internet
- No hay riesgo de compartición no autorizada ya que toda exportación requiere acción explícita del usuario

### 7. Permisos de Android

La aplicación solicita los siguientes permisos:

| Permiso | Uso | Obligatorio |
|---------|-----|-------------|
| `ACCESS_FINE_LOCATION` | (A) Obtener altitud GPS para calibración de altímetro (temporal, no se guarda)<br>(B) Obtener coordenadas GPS para flight logging (solo cuando el usuario lo activa) | No |
| `ACCESS_COARSE_LOCATION` | Respaldo para GPS | No |
| `RECORD_AUDIO` | Control por voz (comandos de navegación) | No |
| `INTERNET` | Abrir enlaces en navegador (función opcional de pasos) | No |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Guardar logs CSV en Download/FlightChecks/ e importar logs existentes | No |

**Todos los permisos son opcionales** - la aplicación funciona plenamente sin otorgarlos.

**Nota sobre Flight Logging:**
- El permiso de ubicación se usa para flight logging **solo cuando usted toca el botón "Log"**
- Si no usa la función de logging, no se recopilan coordenadas GPS
- Si usa logging, **usted controla** qué se registra y cuándo

### 8. Privacidad de Menores

Esta aplicación no está dirigida a menores de 13 años. No recopilamos intencionalmente información personal de menores.

### 9. Sus Derechos

Como todos sus datos están almacenados localmente en su dispositivo:
- Puede eliminar todos los datos desinstalando la aplicación
- Puede revocar permisos en cualquier momento desde Ajustes de Android
- Tiene control total sobre sus archivos de checklist
- **Tiene control total sobre sus flight logs**:
  - Ver todos los logs en la app
  - Editar cualquier entrada
  - Eliminar entradas individuales o borrar todo el log
  - Exportar cuando desee
  - Los archivos CSV en Download/FlightChecks/ son accesibles y eliminables desde su gestor de archivos

### 10. Cambios a esta Política

Notificaremos cualquier cambio mediante la actualización de la fecha "Última actualización" en este documento. Los cambios importantes se comunicarán en las notas de la versión de la aplicación.

### 11. Contacto

Para preguntas sobre esta política de privacidad:
- **GitHub Issues**: [https://github.com/jorgemarmor/FlightChecks/issues](https://github.com/jorgemarmor/FlightChecks/issues)
- **Desarrollador**: Jorge Mármol

---

## English

### 1. Introduction

Taifun Checks is an aviation checklist application developed by Jorge Mármol. This privacy policy explains how the application collects, uses, and protects your personal information.

### 2. Information We Collect

#### 2.1 Location Data (GPS)

The application uses GPS in two distinct and optional ways:

##### A) Temporary Use for Altitude and QNH (NOT SAVED)

**Purpose:**
- Display current altitude in real-time during checklist procedures
- Calculate QNH (atmospheric pressure setting) using GPS altitude and device barometer according to ICAO standard formula
- Assist in altimeter calibration during taxiing and pre-flight phases

**Privacy:**
- **Only altitude is used**, not geographic coordinates (latitude/longitude)
- GPS altitude data is processed **in real-time** and **NOT saved**
- **No data is stored** on the device
- **No data is transmitted** to external servers
- This use is **completely optional**: the application works fully without activating this feature

##### B) Flight Logging - Flight Recording (SAVED LOCALLY, OPTIONAL)

**Purpose:**
- Allow the user to create flight activity records when explicitly desired
- Document important events: takeoffs, landings, fuel stops, waypoints, etc.

**How It Works:**
- **Requires explicit user action**: Data is only recorded when the user taps the "Log" button on specific checklist steps
- **Totally optional**: The user decides which steps have logging capability and when to activate it
- **Full user control**: The user can view, edit, and delete any log entry at any time

**Data Saved (ONLY when the user creates a log entry):**
- GPS coordinates (latitude and longitude) at the moment of tapping the button
- Altitude in meters
- Timestamp (date and time)
- Custom text defined by the user
- ICAO code of nearest aerodrome (if available in local database)

**Storage:**
- **All data is stored locally** on the user's device
- CSV files saved in `Download/FlightChecks/` (accessible by the user)
- **No automatic transmission** to external servers
- **No cloud synchronization**
- User can delete all logs at any time

**Export and Share (OPTIONAL):**
- User can **optionally** export their logs to CSV
- User can **optionally** share their logs via email, messaging, etc.
- These actions are **explicit and user-controlled**

**Permissions requested:**
- `ACCESS_FINE_LOCATION`: For accurate GPS altitude readings and coordinates for logging
- `ACCESS_COARSE_LOCATION`: As backup for GPS readings

#### 2.2 Barometer Data

- The application accesses the device's barometric pressure sensor (if available)
- Used together with GPS altitude to calculate QNH
- Barometer data is processed **locally** on the device
- **Not stored or transmitted** outside the device

#### 2.3 Audio Data

- If you activate voice control, the application accesses the microphone
- Voice recognition is processed through the Android system API
- **Conversations are NOT recorded, stored, or transmitted**
- Only specific navigation commands are processed ("Previous", "Next", etc.)

#### 2.4 Checklist Files

- Created or modified checklists are stored **locally** on your device
- The application **has no internet access** to transmit data
- Only you control the content of your checklists

### 3. How We Use Information

#### 3.1 Location Data Use

##### A) Temporary Use (Altitude/QNH)

GPS altitude data is used **exclusively** for:
- Display current altitude in real-time on screen
- Calculate QNH using the ICAO standard barometric formula
- Provide reference information for altimeter calibration

This data is **NOT saved** at any time.

##### B) Flight Logging (When User Explicitly Activates It)

GPS data is used **only when the user taps the "Log" button** to:
- Create flight activity records chosen by the user
- Document important events with coordinates and altitude
- Maintain a local flight history for personal reference
- Allow the user to export their own data if desired

**What we DON'T do with your location data:**
- ❌ We do not track your location in the background
- ❌ We do not create location profiles or flight patterns without your knowledge
- ❌ We do not share location data with third parties
- ❌ We do not send data to external servers
- ❌ We do not use location data for advertising
- ❌ We do not access your logs without your explicit action

**User Control:**
- ✅ You decide when to create a log entry
- ✅ You can view all your logs at any time
- ✅ You can edit or delete any entry
- ✅ You can clear all logs whenever you want
- ✅ You control whether to export or share your data

### 4. Data Storage

- **All data is stored locally** on your device
- No cloud synchronization
- No remote servers
- No automatic data transmission to internet

**Locally stored data:**
- YAML checklist files
- Application preferences (language, theme, active checklist)
- First launch status
- **Flight logs** (ONLY when user explicitly creates entries):
  - CSV files in `Download/FlightChecks/`
  - GPS coordinates, altitude, timestamps chosen by the user
  - Completely under user control (can view, edit, delete at any time)

**Data NOT stored (Temporary GPS use):**
- GPS location data for altitude/QNH (only processed in real-time)
- Temporary altitude readings
- Temporary barometric pressure readings
- Microphone audio
- App navigation history

### 5. Sharing Information

**We do not automatically share any information with third parties.**

The application works **completely offline** and:
- Does not require internet connection for basic operation
- Does not automatically transmit data to external servers
- Does not include third-party analytics services
- Does not include advertising
- Does not connect to cloud services

**Exceptions (ONLY with explicit user action):**

1. **Checklist step links**: If you use the "Open link" feature in a checklist step, your default browser will open the specified URL, subject to that website's privacy policy.

2. **Export/Share Flight Logs**: If **you choose** to export or share your flight logs:
   - **It is completely optional**: The app NEVER shares logs automatically
   - **Full user control**: You decide what to share, when, and with whom
   - **Explicit action required**: You must tap the "Share" or "Export" button
   - **No third-party access**: The app has no access to where you send your logs
   - Shared logs are subject to the privacy policies of the service you choose (email, messaging, etc.)

### 6. Data Security

- All data is stored in the application's private space
- Checklist files are accessible only by the application
- Temporary GPS location data (altitude/QNH) is processed in memory and not persisted
- Flight logs are stored in `Download/FlightChecks/` so the user has direct access
- No automatic data transmission to internet
- No risk of unauthorized sharing as all export requires explicit user action

### 7. Android Permissions

The application requests the following permissions:

| Permission | Use | Required |
|------------|-----|----------|
| `ACCESS_FINE_LOCATION` | (A) Obtain GPS altitude for altimeter calibration (temporary, not saved)<br>(B) Obtain GPS coordinates for flight logging (only when user activates it) | No |
| `ACCESS_COARSE_LOCATION` | Backup for GPS | No |
| `RECORD_AUDIO` | Voice control (navigation commands) | No |
| `INTERNET` | Open links in browser (optional step feature) | No |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Save CSV logs to Download/FlightChecks/ and import existing logs | No |

**All permissions are optional** - the application works fully without granting them.

**Note about Flight Logging:**
- Location permission is used for flight logging **only when you tap the "Log" button**
- If you don't use the logging feature, no GPS coordinates are collected
- If you use logging, **you control** what is recorded and when

### 8. Children's Privacy

This application is not directed to children under 13. We do not intentionally collect personal information from minors.

### 9. Your Rights

Since all your data is stored locally on your device:
- You can delete all data by uninstalling the application
- You can revoke permissions at any time from Android Settings
- You have full control over your checklist files
- **You have full control over your flight logs**:
  - View all logs in the app
  - Edit any entry
  - Delete individual entries or clear entire log
  - Export whenever you want
  - CSV files in Download/FlightChecks/ are accessible and deletable from your file manager

### 10. Changes to This Policy

We will notify any changes by updating the "Last updated" date in this document. Significant changes will be communicated in the application's version release notes.

### 11. Contact

For questions about this privacy policy:
- **GitHub Issues**: [https://github.com/jorgemarmor/FlightChecks/issues](https://github.com/jorgemarmor/FlightChecks/issues)
- **Developer**: Jorge Mármol

---

## Resumen / Summary

**🇪🇸 Español:**
- ✅ GPS tiene DOS usos opcionales:
  - **A) Altitud temporal** (no se guarda): Para calibrar altímetro
  - **B) Flight Logging** (se guarda localmente): Solo cuando TÚ tocas el botón "Log"
- ❌ NO rastreamos ubicación en segundo plano
- ❌ NO compartimos datos automáticamente
- ❌ NO enviamos datos a servidores externos
- ✅ TODO bajo tu control: tú decides qué se registra y cuándo
- ✅ Puedes ver, editar y eliminar tus logs en cualquier momento
- ✅ Todo funciona offline y localmente

**🇬🇧 English:**
- ✅ GPS has TWO optional uses:
  - **A) Temporary altitude** (not saved): To calibrate altimeter
  - **B) Flight Logging** (saved locally): Only when YOU tap the "Log" button
- ❌ We do NOT track location in the background
- ❌ We do NOT automatically share data
- ❌ We do NOT send data to external servers
- ✅ EVERYTHING under your control: you decide what is recorded and when
- ✅ You can view, edit, and delete your logs at any time
- ✅ Everything works offline and locally
