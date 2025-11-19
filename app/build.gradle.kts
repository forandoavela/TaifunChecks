plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.taifun.checks"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.taifun.checks"
        minSdk = 24
        targetSdk = 35

        // Versión manual con incremento automático basado en commits
        // x.y.z donde:
        //   x = major (cambios importantes) - cambiar manualmente
        //   y = minor (nuevas funcionalidades) - cambiar manualmente
        //   z = patch (incrementa automáticamente con cada commit)

        val majorVersion = 1
        val minorVersion = 5 // v1.5: Fixed Google Play translations, native debug symbols, language splitting

        // Intentar obtener el número de commits, si falla usar valor base
        val patchVersion = try {
            val process = Runtime.getRuntime().exec("git rev-list --count HEAD")
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()

            val count = output.toIntOrNull() ?: 0

            // Si devuelve 1, probablemente es un shallow clone en CI/CD
            // En ese caso, usar el número del último commit conocido + 1
            if (count <= 1) {
                println("⚠ Shallow clone detected (count=$count), using base version")
                8  // Base para v1.5 en CI/CD - updated for version 10508
            } else {
                println("✓ Git commit count: $count")
                // Subtract because we're at commit 109, but want version 10508 (patch 8)
                count - 101
            }
        } catch (e: Exception) {
            println("⚠ Git not available: ${e.message}")
            8  // Fallback manual para v1.5
        }

        // versionCode que siempre incrementa con major.minor.patch
        // Fórmula: major*10000 + minor*100 + patch
        // v1.3.70 → 10370, v1.4.0 → 10400, garantiza incremento
        versionCode = majorVersion * 10000 + minorVersion * 100 + patchVersion
        versionName = "$majorVersion.$minorVersion.$patchVersion"

        println("═══════════════════════════════════════════════")
        println("  Building version: $versionName")
        println("  Version code: $versionCode")
        println("═══════════════════════════════════════════════")

        // Configurar NDK para generar símbolos de depuración
        ndk {
            // Soportar las arquitecturas más comunes
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            // Habilitar R8 para ofuscación y reducción de tamaño
            isMinifyEnabled = true
            isShrinkResources = true  // Reactivado: El problema era language splitting, no resource shrinking
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Generar símbolos de depuración nativos para Play Console
            ndk {
                debugSymbolLevel = "FULL"
            }
            // Sign release builds if keystore is available
            if (System.getenv("KEYSTORE_PASSWORD") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // Configuración para Android App Bundle
    bundle {
        language {
            // Deshabilitar el splitting de idiomas para asegurar que todos los idiomas estén siempre incluidos
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }


    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
        jniLibs {
            // Mantener los símbolos de depuración (.so files)
            useLegacyPackaging = false
            // No excluir ninguna librería nativa
            pickFirsts.clear()
        }
    }
    sourceSets["main"].assets.srcDirs("src/main/assets")
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Material Components para Theme.Material3.DayNight.*
    implementation("com.google.android.material:material:1.12.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // SnakeYAML (parser YAML)
    implementation("org.yaml:snakeyaml:2.2")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
