package com.taifun.checks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.taifun.checks.R

/**
 * Data class para representar una opción de color predefinida
 */
data class ColorOption(
    val nameResId: Int,
    val hexValue: String
)

/**
 * Paleta de colores predefinidos
 */
val predefinedColors = listOf(
    ColorOption(R.string.color_none, ""),
    ColorOption(R.string.color_red, "#F44336"),
    ColorOption(R.string.color_green, "#4CAF50"),
    ColorOption(R.string.color_blue, "#2196F3"),
    ColorOption(R.string.color_yellow, "#FFEB3B"),
    ColorOption(R.string.color_orange, "#FF9800"),
    ColorOption(R.string.color_purple, "#9C27B0"),
    ColorOption(R.string.color_pink, "#E91E63"),
    ColorOption(R.string.color_cyan, "#00BCD4")
)

/**
 * Diálogo para seleccionar color de checklist
 */
@Composable
fun ColorPickerDialog(
    currentColor: String?,
    onColorSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    // Estado temporal para el color seleccionado (solo se aplica al confirmar)
    var selectedColor by remember { mutableStateOf(currentColor) }
    var customHex by remember { mutableStateOf(currentColor ?: "") }
    var isValidHex by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Grid de colores predefinidos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedColors) { colorOption ->
                        ColorCircle(
                            colorOption = colorOption,
                            isSelected = selectedColor == colorOption.hexValue ||
                                        (selectedColor.isNullOrBlank() && colorOption.hexValue.isBlank()),
                            onClick = {
                                // Guardar selección temporalmente
                                selectedColor = colorOption.hexValue.ifBlank { null }
                                customHex = colorOption.hexValue
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Campo para color personalizado
                Text(
                    text = stringResource(R.string.color_custom),
                    style = MaterialTheme.typography.labelLarge
                )

                OutlinedTextField(
                    value = customHex,
                    onValueChange = { newValue ->
                        customHex = newValue
                        isValidHex = isValidHexColor(newValue)
                        // Actualizar selección temporal si es válido
                        if (isValidHex && newValue.isNotBlank()) {
                            selectedColor = newValue.uppercase()
                        }
                    },
                    label = { Text(stringResource(R.string.color_hex_label)) },
                    placeholder = { Text(stringResource(R.string.color_hex_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    isError = !isValidHex && customHex.isNotBlank(),
                    supportingText = {
                        if (!isValidHex && customHex.isNotBlank()) {
                            Text(stringResource(R.string.color_invalid))
                        }
                    },
                    trailingIcon = {
                        if (customHex.isNotBlank() && isValidHex) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = hexToColor(customHex),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Aplicar el color seleccionado
                    onColorSelected(selectedColor)
                    onDismiss()
                },
                enabled = customHex.isBlank() || isValidHex
            ) {
                Text(stringResource(R.string.aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

/**
 * Círculo de color para la paleta
 */
@Composable
private fun ColorCircle(
    colorOption: ColorOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (colorOption.hexValue.isBlank()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        hexToColor(colorOption.hexValue)
                    },
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = CircleShape
                )
        ) {
            // Si es "sin color", mostrar diagonal
            if (colorOption.hexValue.isBlank()) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = stringResource(colorOption.nameResId),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Valida si un string es un color hexadecimal válido
 */
fun isValidHexColor(hex: String): Boolean {
    if (hex.isBlank()) return true
    val pattern = Regex("^#?[0-9A-Fa-f]{6}$")
    return pattern.matches(hex)
}

/**
 * Convierte string hex a Color de Compose
 */
fun hexToColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        Color(("FF" + cleanHex).toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}
