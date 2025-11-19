package com.taifun.checks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores corporativos de aviación
private val AviationBlue = Color(0xFF0A64C9)
private val AviationBlueLight = Color(0xFF4A8FE7)
private val AviationBlueDark = Color(0xFF003C8F)
private val SafetyGreen = Color(0xFF34A853)
private val WarningAmber = Color(0xFFFBBC04)
private val ErrorRed = Color(0xFFEA4335)

// Tema claro - optimizado para uso diurno
private val LightColors = lightColorScheme(
    primary = AviationBlue,
    onPrimary = Color.White,
    primaryContainer = AviationBlueLight,
    onPrimaryContainer = AviationBlueDark,

    secondary = SafetyGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF1B5E20),

    tertiary = WarningAmber,
    onTertiary = Color.Black,

    error = ErrorRed,
    onError = Color.White,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1C1C),

    surface = Color.White,
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFE7E7E7),
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFF79747E)
)

// Tema oscuro - optimizado para uso nocturno
private val DarkColors = darkColorScheme(
    primary = AviationBlueLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = AviationBlueDark,
    onPrimaryContainer = Color(0xFFB8D4FF),

    secondary = SafetyGreen,
    onSecondary = Color(0xFF00390D),
    secondaryContainer = Color(0xFF00531A),
    onSecondaryContainer = Color(0xFFB7F0B7),

    tertiary = WarningAmber,
    onTertiary = Color.Black,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),

    background = Color(0xFF1C1C1C),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF121212),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2B2B2B),
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99)
)

// Tema alto contraste - máxima legibilidad para condiciones difíciles
private val HighContrastColors = darkColorScheme(
    primary = Color(0xFF00D4FF), // Cyan brillante
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0096C7),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF00FF41), // Verde brillante
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00C136),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFFFD600), // Amarillo brillante
    onTertiary = Color.Black,

    error = Color(0xFFFF0000), // Rojo puro
    onError = Color.White,

    background = Color.Black,
    onBackground = Color.White,

    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFFFFFFF),

    outline = Color(0xFFFFFFFF)
)

@Composable
fun TaifunTheme(
    darkTheme: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColors
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
