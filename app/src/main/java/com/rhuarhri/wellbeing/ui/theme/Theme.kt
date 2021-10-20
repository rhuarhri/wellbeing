package com.rhuarhri.wellbeing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200

    *//* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    *//*
)*/

private val DarkColorPalette = darkColors(
    primary = Color(red = 0x95, green = 0x00, blue = 0x00, alpha = 0xff),
    primaryVariant = Color(red = 0x95, green = 0x00, blue = 0x00, alpha = 0xa0),
    secondary = Color(red= 0xD5, green = 0x6B, blue = 0x00, alpha = 0xff)
)

private val LightColorPalette = lightColors(
    primary = Color(red = 0xcb, green = 0xe1, blue = 0xef, alpha = 0xff),
    primaryVariant = Color(red = 0xcd, green = 0xe1, blue = 0xef, alpha = 0xa0),
    secondary = Color(red= 0x5e, green = 0xa9, blue = 0xbe, alpha = 0xff),

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun WellbeingTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}