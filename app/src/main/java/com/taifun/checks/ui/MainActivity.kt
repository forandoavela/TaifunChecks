package com.taifun.checks.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.ui.navigation.AppNavHost
import com.taifun.checks.ui.navigation.Routes
import com.taifun.checks.ui.theme.TaifunTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar edge-to-edge para compatibilidad con Android 15+
        // Scaffold de Material3 maneja automáticamente los system bars insets
        enableEdgeToEdge()

        settingsRepo = SettingsRepository(this)

        // Leer el estado de primer lanzamiento de forma síncrona
        val isFirstLaunch = settingsRepo.getFirstLaunchSync()
        val startDestination = if (isFirstLaunch) Routes.FIRST_LAUNCH else Routes.HOME

        // Configurar la UI
        setupContent(startDestination)

        // Observar configuración de pantalla encendida
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepo.screenOnFlow.collect { keepOn ->
                    if (keepOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }
    }

    private fun setupContent(startDestination: String) {
        setContent {
            val darkTheme by settingsRepo.darkFlow.collectAsState(initial = false)
            val highContrast by settingsRepo.highContrastFlow.collectAsState(initial = false)

            TaifunTheme(
                darkTheme = darkTheme,
                highContrast = highContrast
            ) {
                val nav = rememberNavController()
                AppNavHost(
                    nav = nav,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Aplicar idioma antes de que se cree la Activity
        val context = applyLanguageContext(newBase)
        super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-aplicar el idioma cuando cambie la configuración
        applyLanguageContext(this)
        // La configuración se aplica automáticamente, no es necesario updateConfiguration (deprecado)
    }

    private fun applyLanguageContext(context: Context): Context {
        val settingsRepo = SettingsRepository(context)

        // Leer idioma de forma síncrona usando SharedPreferences
        val languageCode = settingsRepo.getLanguageSync()

        val locale = when (languageCode) {
            "es" -> Locale("es", "ES")
            "en" -> Locale("en", "US")
            else -> {
                // Auto: usar idioma del sistema, pero preferir español si no es inglés
                val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
                if (systemLocale.language == "en") {
                    Locale("en", "US")
                } else {
                    Locale("es", "ES")
                }
            }
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
