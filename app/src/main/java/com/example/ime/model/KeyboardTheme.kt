package com.example.ime.model

import androidx.compose.ui.graphics.Color

data class KeyboardColorScheme(
    val id: KeyboardThemeId,
    val name: String,
    val backgroundColor: Color,
    val keyBackgroundColor: Color,
    val specialKeyColor: Color,
    val accentActionColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val suggestionBgColor: Color,
    val suggestionTextColor: Color,
    val navBarBgColor: Color,
    val navBarIconColor: Color,
    val borderColor: Color,
    val isDark: Boolean
)

object KeyboardThemes {
    val DarkMidnight = KeyboardColorScheme(
        id = KeyboardThemeId.DARK_MIDNIGHT,
        name = "OLED Midnight",
        backgroundColor = Color(0xFF101216),
        keyBackgroundColor = Color(0xFF1E222B),
        specialKeyColor = Color(0xFF282E3B),
        accentActionColor = Color(0xFF00E676),
        primaryTextColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFF90A4AE),
        suggestionBgColor = Color(0xFF171B22),
        suggestionTextColor = Color(0xFF00E676),
        navBarBgColor = Color(0xFF0D0F12),
        navBarIconColor = Color(0xFFB0BEC5),
        borderColor = Color(0xFF2E3646),
        isDark = true
    )

    val LightPure = KeyboardColorScheme(
        id = KeyboardThemeId.LIGHT_PURE,
        name = "Pure White",
        backgroundColor = Color(0xFFECEFF1),
        keyBackgroundColor = Color(0xFFFFFFFF),
        specialKeyColor = Color(0xFFDDE3EA),
        accentActionColor = Color(0xFF1976D2),
        primaryTextColor = Color(0xFF212121),
        secondaryTextColor = Color(0xFF546E7A),
        suggestionBgColor = Color(0xFFF5F7FA),
        suggestionTextColor = Color(0xFF1976D2),
        navBarBgColor = Color(0xFFE2E7EC),
        navBarIconColor = Color(0xFF455A64),
        borderColor = Color(0xFFCFD8DC),
        isDark = false
    )

    val OceanBlue = KeyboardColorScheme(
        id = KeyboardThemeId.OCEAN_BLUE,
        name = "Ocean Deep Blue",
        backgroundColor = Color(0xFF0A192F),
        keyBackgroundColor = Color(0xFF172A45),
        specialKeyColor = Color(0xFF1D3557),
        accentActionColor = Color(0xFF64FFDA),
        primaryTextColor = Color(0xFFCCD6F6),
        secondaryTextColor = Color(0xFF8892B0),
        suggestionBgColor = Color(0xFF0E223D),
        suggestionTextColor = Color(0xFF64FFDA),
        navBarBgColor = Color(0xFF071224),
        navBarIconColor = Color(0xFF8892B0),
        borderColor = Color(0xFF203A63),
        isDark = true
    )

    val CyberEmerald = KeyboardColorScheme(
        id = KeyboardThemeId.CYBER_EMERALD,
        name = "Cyber Neon Emerald",
        backgroundColor = Color(0xFF071A12),
        keyBackgroundColor = Color(0xFF0D281E),
        specialKeyColor = Color(0xFF143E30),
        accentActionColor = Color(0xFF00FF87),
        primaryTextColor = Color(0xFFD8F3DC),
        secondaryTextColor = Color(0xFF74C69D),
        suggestionBgColor = Color(0xFF0A2218),
        suggestionTextColor = Color(0xFF00FF87),
        navBarBgColor = Color(0xFF05140E),
        navBarIconColor = Color(0xFF52B788),
        borderColor = Color(0xFF1B4D3E),
        isDark = true
    )

    val SunsetAmber = KeyboardColorScheme(
        id = KeyboardThemeId.SUNSET_AMBER,
        name = "Sunset Bronze",
        backgroundColor = Color(0xFF1C130E),
        keyBackgroundColor = Color(0xFF2D1F17),
        specialKeyColor = Color(0xFF3F2B20),
        accentActionColor = Color(0xFFFF9100),
        primaryTextColor = Color(0xFFFFE0B2),
        secondaryTextColor = Color(0xFFBCAAA4),
        suggestionBgColor = Color(0xFF241812),
        suggestionTextColor = Color(0xFFFFAB40),
        navBarBgColor = Color(0xFF150D0A),
        navBarIconColor = Color(0xFFA1887F),
        borderColor = Color(0xFF4E342E),
        isDark = true
    )

    val PastelLavender = KeyboardColorScheme(
        id = KeyboardThemeId.PASTEL_LAVENDER,
        name = "Pastel Lavender",
        backgroundColor = Color(0xFFF3E5F5),
        keyBackgroundColor = Color(0xFFFFFFFF),
        specialKeyColor = Color(0xFFE1BEE7),
        accentActionColor = Color(0xFF8E24AA),
        primaryTextColor = Color(0xFF37474F),
        secondaryTextColor = Color(0xFF7B1FA2),
        suggestionBgColor = Color(0xFFEDE7F6),
        suggestionTextColor = Color(0xFF6A1B9A),
        navBarBgColor = Color(0xFFE8D8ED),
        navBarIconColor = Color(0xFF4A148C),
        borderColor = Color(0xFFD1C4E9),
        isDark = false
    )

    val HighContrast = KeyboardColorScheme(
        id = KeyboardThemeId.HIGH_CONTRAST,
        name = "Tương phản cao",
        backgroundColor = Color(0xFF000000),
        keyBackgroundColor = Color(0xFF181818),
        specialKeyColor = Color(0xFF2C2C2C),
        accentActionColor = Color(0xFFFFEB3B),
        primaryTextColor = Color(0xFFFFFF00),
        secondaryTextColor = Color(0xFFFFFFFF),
        suggestionBgColor = Color(0xFF121212),
        suggestionTextColor = Color(0xFFFFEB3B),
        navBarBgColor = Color(0xFF080808),
        navBarIconColor = Color(0xFFFFEB3B),
        borderColor = Color(0xFFFFEB3B),
        isDark = true
    )

    fun getTheme(id: KeyboardThemeId): KeyboardColorScheme {
        return when (id) {
            KeyboardThemeId.DARK_MIDNIGHT -> DarkMidnight
            KeyboardThemeId.LIGHT_PURE -> LightPure
            KeyboardThemeId.OCEAN_BLUE -> OceanBlue
            KeyboardThemeId.CYBER_EMERALD -> CyberEmerald
            KeyboardThemeId.SUNSET_AMBER -> SunsetAmber
            KeyboardThemeId.PASTEL_LAVENDER -> PastelLavender
            KeyboardThemeId.HIGH_CONTRAST -> HighContrast
            KeyboardThemeId.DYNAMIC_M3 -> DarkMidnight
        }
    }

    fun resolveTheme(themeMode: ThemeMode, themeId: KeyboardThemeId, isSystemDark: Boolean): KeyboardColorScheme {
        return if (themeMode == ThemeMode.FOLLOW_SYSTEM) {
            if (isSystemDark) DarkMidnight else LightPure
        } else {
            getTheme(themeId)
        }
    }

    val allThemes = listOf(
        DarkMidnight,
        LightPure,
        OceanBlue,
        CyberEmerald,
        SunsetAmber,
        PastelLavender,
        HighContrast
    )
}
