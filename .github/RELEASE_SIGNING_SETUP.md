# 🔐 Configuración de Firma de Release APK

Esta guía te ayudará a generar un keystore y configurar GitHub Actions para firmar automáticamente los APKs de release.

## 📋 Requisitos Previos

- Java JDK instalado (viene con Android Studio)
- Acceso a tu repositorio de GitHub con permisos de administrador

## 🔑 Paso 1: Generar el Keystore

### Opción A: Desde Android Studio (Recomendado)

1. En Android Studio, ve a **Build → Generate Signed Bundle / APK**
2. Selecciona **APK** y haz clic en **Next**
3. Haz clic en **Create new...**
4. Rellena el formulario:
   - **Key store path**: Elige dónde guardar el archivo (ej: `taifun-release-key.jks`)
   - **Password**: Contraseña del keystore (guárdala de forma segura)
   - **Alias**: Nombre del alias (ej: `taifun-key`)
   - **Password**: Contraseña de la clave (puede ser la misma que la del keystore)
   - **Validity**: 25 años (por defecto)
   - Rellena **Certificate**:
     - First and Last Name: Tu nombre o el de tu organización
     - Organizational Unit: (Opcional) Ej: Desarrollo
     - Organization: Tu empresa o nombre
     - City or Locality: Tu ciudad
     - State or Province: Tu provincia
     - Country Code: ES (para España)
5. Haz clic en **OK**
6. **IMPORTANTE**: Guarda el archivo `.jks` o `.keystore` en un lugar seguro

### Opción B: Desde Línea de Comandos

Abre una terminal y ejecuta:

```bash
keytool -genkey -v -keystore taifun-release-key.jks \
  -alias taifun-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass TU_CONTRASEÑA_KEYSTORE \
  -keypass TU_CONTRASEÑA_CLAVE
```

Responde las preguntas:
- **What is your first and last name?**: Tu nombre
- **What is the name of your organizational unit?**: (Opcional)
- **What is the name of your organization?**: Tu organización
- **What is the name of your City or Locality?**: Tu ciudad
- **What is the name of your State or Province?**: Tu provincia
- **What is the two-letter country code for this unit?**: ES

**Guarda estos datos:**
- Archivo del keystore: `taifun-release-key.jks`
- Contraseña del keystore: (la que pusiste en -storepass)
- Alias de la clave: `taifun-key`
- Contraseña de la clave: (la que pusiste en -keypass)

⚠️ **IMPORTANTE**: Nunca compartas este archivo ni las contraseñas. Si los pierdes, **NO podrás actualizar tu app en Google Play**.

---

## 🔒 Paso 2: Convertir el Keystore a Base64

GitHub Actions necesita el keystore en formato Base64 para almacenarlo como secret.

### En Linux/Mac:

```bash
base64 -i taifun-release-key.jks | tr -d '\n' > keystore_base64.txt
```

### En Windows (PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("taifun-release-key.jks")) | Out-File -Encoding ASCII keystore_base64.txt
```

Esto creará un archivo `keystore_base64.txt` con el contenido codificado en Base64.

---

## 🔐 Paso 3: Configurar GitHub Secrets

1. Ve a tu repositorio en GitHub
2. Haz clic en **Settings** (Configuración)
3. En el menú izquierdo, haz clic en **Secrets and variables → Actions**
4. Haz clic en **New repository secret** para cada uno de los siguientes:

### Secrets a crear:

| Nombre del Secret | Valor | Descripción |
|-------------------|-------|-------------|
| `KEYSTORE_BASE64` | Contenido de `keystore_base64.txt` | Keystore codificado en Base64 |
| `KEYSTORE_PASSWORD` | Tu contraseña del keystore | La que usaste al crear el keystore |
| `KEY_ALIAS` | `taifun-key` (o el que usaste) | Alias de la clave |
| `KEY_PASSWORD` | Tu contraseña de la clave | Puede ser igual que KEYSTORE_PASSWORD |

### Cómo añadir cada secret:

1. Haz clic en **New repository secret**
2. Nombre: `KEYSTORE_BASE64`
3. Secret: Abre `keystore_base64.txt` y copia **TODO** el contenido
4. Haz clic en **Add secret**
5. Repite para los otros 3 secrets

---

## ✅ Paso 4: Verificar la Configuración

1. Haz un push a tu repositorio o crea un nuevo tag
2. Ve a la pestaña **Actions** en GitHub
3. Espera a que termine el workflow
4. Verifica que el build summary muestre:
   ```
   🔐 Release APK is signed and ready for distribution
   ```
5. Descarga el artifact `app-release`
6. El APK ahora se llamará `app-release.apk` (firmado) en lugar de `app-release-unsigned.apk`

---

## 🧪 Cómo Verificar que el APK está Firmado

Descarga el APK de release y verifica su firma:

```bash
# Ver información de la firma
jarsigner -verify -verbose -certs app-release.apk

# Debería mostrar "jar verified" al final
```

O usando apksigner (parte del Android SDK):

```bash
apksigner verify --print-certs app-release.apk
```

---

## 📱 Instalación del APK Firmado

El APK firmado ahora se puede:
- ✅ Instalar directamente en cualquier dispositivo Android
- ✅ Distribuir a usuarios finales
- ✅ Publicar en Google Play Store
- ✅ Actualizar versiones anteriores (si usas el mismo keystore)

---

## 🚨 Backup del Keystore

**MUY IMPORTANTE:**

1. **Haz backup del archivo `.jks`** en un lugar seguro (Google Drive, USB cifrado, etc.)
2. **Guarda las contraseñas** en un gestor de contraseñas (1Password, LastPass, etc.)
3. **NUNCA subas el keystore al repositorio Git**

Si pierdes el keystore:
- ❌ No podrás actualizar tu app en Google Play
- ❌ Tendrás que publicar como nueva app (usuarios perderán sus datos)
- ❌ Perderás el nombre del paquete

---

## 🔧 Solución de Problemas

### El workflow falla con "keystore password was incorrect"
- Verifica que `KEYSTORE_PASSWORD` sea correcto en GitHub Secrets
- Verifica que `KEY_PASSWORD` sea correcto en GitHub Secrets

### El APK sale como "unsigned"
- Verifica que todos los 4 secrets estén configurados correctamente
- Revisa los logs del workflow en la sección "Build release APK (signed)"

### Error: "keystore file does not exist"
- Verifica que `KEYSTORE_BASE64` contenga el contenido completo del archivo base64
- Asegúrate de que no haya saltos de línea en el secret

---

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs del workflow en GitHub Actions
2. Verifica que todos los secrets estén configurados
3. Asegúrate de que el keystore sea válido ejecutando: `keytool -list -v -keystore taifun-release-key.jks`

---

**Generado con Claude Code**
