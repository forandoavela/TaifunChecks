plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.taifun.checks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.taifun.checks"
        minSdk = 24
        targetSdk = 36

        // Sistema de versiones: w.x.yy.zz donde:
        //   w = versión principal (cambios muy importantes) - cambiar manualmente
        //   x = grandes funcionalidades - cambiar manualmente
        //   yy = grandes grupos de mejoras y correcciones - cambiar manualmente
        //   zz = commits desde baseCommitCount (auto-incrementado)
        //
        // IMPORTANTE: Cuando cambies w, x, o yy, actualiza baseCommitCount al número de commit actual

        val majorVersion = 1      // w - versión principal
        val minorVersion = 0      // x - grandes funcionalidades
        val patchVersion = 9      // yy - grupos de mejoras (00-99)
        val baseCommitCount = 165 // Número de commit en el que se actualizó yy (para que zz = 0)

        // Calcular zz como: commits actuales - baseCommitCount
        val buildVersion = try {
            val countProcess = Runtime.getRuntime().exec("git rev-list --count HEAD")
            val output = countProcess.inputStream.bufferedReader().readText().trim()
            countProcess.waitFor()
            val currentCommits = output.toIntOrNull() ?: baseCommitCount
            val zz = (currentCommits - baseCommitCount).coerceAtLeast(0)
            println("✓ Commits: $currentCommits, Base: $baseCommitCount, ZZ: $zz")
            zz
        } catch (e: Exception) {
            println("⚠ Git not available: ${e.message}, using fallback version 0")
            0  // Fallback si git no está disponible
        }

        // versionCode: wxyyzz (concatenación directa)
        // Ejemplo: v1.0.00.00 → 100000, v1.2.15.03 → 121503
        versionCode = majorVersion * 100000 + minorVersion * 10000 + patchVersion * 100 + buildVersion
        versionName = "$majorVersion.$minorVersion.${patchVersion.toString().padStart(2, '0')}.${buildVersion.toString().padStart(2, '0')}"

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

    // Configuración para NO comprimir archivos .gz en assets
    // Esto evita que R8/AAPT elimine archivos .gz durante resource shrinking
    androidResources {
        noCompress += listOf("gz")
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
    // Compose BOM - Updated to latest stable (December 2024)
    implementation(platform("androidx.compose:compose-bom:2024.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Material Components para Theme.Material3.DayNight.*
    implementation("com.google.android.material:material:1.12.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines - Updated to latest stable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // SnakeYAML (parser YAML) - Updated to latest stable
    implementation("org.yaml:snakeyaml:2.3")

    // Compose Icons - Font Awesome (para iconos de aviación)
    implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.1")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
