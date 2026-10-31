package com.example.ime.model

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import org.json.JSONArray
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

@Immutable
data class KeyboardSettings(
    val typingMethod: TypingMethod = TypingMethod.TELEX,
    val tonePlacement: TonePlacement = TonePlacement.MODERN,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val themeId: KeyboardThemeId = KeyboardThemeId.DARK_MIDNIGHT,
    val keyboardHeightPercent: Int = 100, // 80% to 130%
    val keyScalePercent: Int = 100,        // 80% to 130% key size
    val rowSpacingDp: Int = 5,             // 2 to 12 dp row spacing
    val fontScalePercent: Int = 100,       // 85% to 130%
    val alignment: KeyboardAlignment = KeyboardAlignment.CENTER,
    val bottomChinHeightDp: Int = 44,      // Virtual Navigation Bar height (36 to 64 dp)
    val showVirtualNavBar: Boolean = true,  // Bottom navigation chin
    val smartFeaturesEnabled: Boolean = true,
    val tonePlacementEnabled: Boolean = true,
    val showSuggestions: Boolean = true,
    val autoCapitalize: Boolean = true,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val showNumberRow: Boolean = false,
    val showNumberRowInLandscape: Boolean = false,
    val spaceBarVietnameseIndicator: Boolean = true,
    val navBarShowCursorArrows: Boolean = false,
    val navBarShowLineJump: Boolean = false,
    val navBarShowSelectAll: Boolean = false,
    val navBarShowPaste: Boolean = false,
    val navBarShowLanguageSwitch: Boolean = true,
    val navBarShowHideKeyboard: Boolean = true,
    // Gesture options
    val swipeSpaceToMoveCursor: Boolean = true,
    val holdSpaceToSwitchLanguage: Boolean = true,
    val swipeBackspaceToDeleteFast: Boolean = true,
    // Language & Input methods
    val activeLanguageId: String = "vi_telex",
    val downloadedLanguages: Set<String> = setOf("vi_telex", "vi_vni", "en_us"),
    val enabledLanguages: Set<String> = setOf("vi_telex", "en_us"),
    // Floating & Split keyboard options
    val displayStyle: KeyboardDisplayStyle = KeyboardDisplayStyle.DOCKED,
    val autoSplitInLandscape: Boolean = true,
    val floatingScalePercent: Int = 80,
    // Popup key preview effect
    val showKeyPreview: Boolean = true,
    val showCommaKey: Boolean = true,
    val showPeriodKey: Boolean = true,
    val showEmojiKey: Boolean = true,
    val recentEmojis: List<String> = emptyList()
)

class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(KeyboardSettings())
    val settings: StateFlow<KeyboardSettings> = _settings.asStateFlow()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val settingsReadGeneration = AtomicLong()
    private val settingsLock = Any()
    private val pendingUpdates = mutableListOf<(KeyboardSettings) -> KeyboardSettings>()
    private var settingsLoaded = false

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        reloadSettings()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        migrateNavigationBarDefaults()
        reloadSettings()
    }

    private fun migrateNavigationBarDefaults() {
        if (prefs.getInt(KEY_NAV_BAR_DEFAULTS_VERSION, 0) >= NAV_BAR_DEFAULTS_VERSION) return

        prefs.edit()
            .putBoolean(KEY_NAV_ARROWS, false)
            .putBoolean(KEY_NAV_LINE_JUMP, false)
            .putBoolean(KEY_NAV_SELECT_ALL, false)
            .putBoolean(KEY_NAV_PASTE, false)
            .putBoolean(KEY_NAV_LANGUAGE_SWITCH, true)
            .putBoolean(KEY_NAV_HIDE_KB, true)
            .putInt(KEY_NAV_BAR_DEFAULTS_VERSION, NAV_BAR_DEFAULTS_VERSION)
            .apply()
    }

    private fun reloadSettings() {
        val generation = settingsReadGeneration.incrementAndGet()
        ioScope.launch {
            val loaded = loadSettings()
            if (settingsReadGeneration.get() != generation) return@launch
            synchronized(settingsLock) {
                if (settingsReadGeneration.get() != generation) return@synchronized
                if (!settingsLoaded) {
                    val merged = pendingUpdates.fold(loaded) { current, update -> update(current) }
                    if (pendingUpdates.isNotEmpty()) persistSettings(merged)
                    pendingUpdates.clear()
                    settingsLoaded = true
                    _settings.value = merged
                } else {
                    _settings.value = loaded
                }
            }
        }
    }

    private fun loadSettings(): KeyboardSettings {
        return KeyboardSettings(
            typingMethod = TypingMethod.valueOf(
                prefs.getString(KEY_TYPING_METHOD, TypingMethod.TELEX.name) ?: TypingMethod.TELEX.name
            ),
            tonePlacement = TonePlacement.valueOf(
                prefs.getString(KEY_TONE_PLACEMENT, TonePlacement.MODERN.name) ?: TonePlacement.MODERN.name
            ),
            themeMode = ThemeMode.valueOf(
                prefs.getString(KEY_THEME_MODE, ThemeMode.FOLLOW_SYSTEM.name) ?: ThemeMode.FOLLOW_SYSTEM.name
            ),
            themeId = KeyboardThemeId.valueOf(
                prefs.getString(KEY_THEME_ID, KeyboardThemeId.DARK_MIDNIGHT.name) ?: KeyboardThemeId.DARK_MIDNIGHT.name
            ),
            keyboardHeightPercent = prefs.getInt(KEY_KEYBOARD_HEIGHT, 100),
            keyScalePercent = prefs.getInt(KEY_KEY_SCALE, 100),
            rowSpacingDp = prefs.getInt(KEY_ROW_SPACING, 5),
            fontScalePercent = prefs.getInt(KEY_FONT_SCALE, 100),
            alignment = KeyboardAlignment.valueOf(
                prefs.getString(KEY_ALIGNMENT, KeyboardAlignment.CENTER.name) ?: KeyboardAlignment.CENTER.name
            ),
            bottomChinHeightDp = prefs.getInt(KEY_BOTTOM_CHIN_HEIGHT, 44),
            showVirtualNavBar = prefs.getBoolean(KEY_SHOW_VIRTUAL_NAV_BAR, true),
            smartFeaturesEnabled = prefs.getBoolean(KEY_SMART_FEATURES_ENABLED, true),
            tonePlacementEnabled = prefs.getBoolean(KEY_TONE_PLACEMENT_ENABLED, true),
            showSuggestions = prefs.getBoolean(KEY_SHOW_SUGGESTIONS, true),
            autoCapitalize = prefs.getBoolean(KEY_AUTO_CAPITALIZE, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true),
            soundFeedback = prefs.getBoolean(KEY_SOUND_FEEDBACK, false),
            showNumberRow = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, false),
            showNumberRowInLandscape = prefs.getBoolean(KEY_SHOW_NUMBER_ROW_LANDSCAPE, false),
            spaceBarVietnameseIndicator = prefs.getBoolean(KEY_SPACE_INDICATOR, true),
            navBarShowCursorArrows = prefs.getBoolean(KEY_NAV_ARROWS, false),
            navBarShowLineJump = prefs.getBoolean(KEY_NAV_LINE_JUMP, false),
            navBarShowSelectAll = prefs.getBoolean(KEY_NAV_SELECT_ALL, false),
            navBarShowPaste = prefs.getBoolean(KEY_NAV_PASTE, false),
            navBarShowLanguageSwitch = prefs.getBoolean(KEY_NAV_LANGUAGE_SWITCH, true),
            navBarShowHideKeyboard = prefs.getBoolean(KEY_NAV_HIDE_KB, true),
            swipeSpaceToMoveCursor = prefs.getBoolean(KEY_SWIPE_SPACE_CURSOR, true),
            holdSpaceToSwitchLanguage = prefs.getBoolean(KEY_HOLD_SPACE_LANG, true),
            swipeBackspaceToDeleteFast = prefs.getBoolean(KEY_SWIPE_BACKSPACE_DELETE, true),
            activeLanguageId = prefs.getString(KEY_ACTIVE_LANG, "vi_telex") ?: "vi_telex",
            downloadedLanguages = prefs.getStringSet(KEY_DOWNLOADED_LANGS, setOf("vi_telex", "vi_vni", "en_us"))
                ?: setOf("vi_telex", "vi_vni", "en_us"),
            enabledLanguages = prefs.getStringSet(KEY_ENABLED_LANGS, setOf("vi_telex", "en_us"))
                ?: setOf("vi_telex", "en_us"),
            displayStyle = KeyboardDisplayStyle.valueOf(
                prefs.getString(KEY_DISPLAY_STYLE, KeyboardDisplayStyle.DOCKED.name) ?: KeyboardDisplayStyle.DOCKED.name
            ),
            autoSplitInLandscape = prefs.getBoolean(KEY_AUTO_SPLIT_LANDSCAPE, true),
            floatingScalePercent = prefs.getInt(KEY_FLOATING_SCALE, 80),
            showKeyPreview = prefs.getBoolean(KEY_SHOW_KEY_PREVIEW, true),
            showCommaKey = prefs.getBoolean(KEY_SHOW_COMMA_KEY, true),
            showPeriodKey = prefs.getBoolean(KEY_SHOW_PERIOD_KEY, true),
            showEmojiKey = prefs.getBoolean(KEY_SHOW_EMOJI_KEY, true),
            recentEmojis = runCatching {
                val json = JSONArray(prefs.getString(KEY_RECENT_EMOJIS, "[]") ?: "[]")
                (0 until minOf(json.length(), MAX_RECENT_EMOJIS))
                    .map { json.optString(it) }
                    .filter(String::isNotEmpty)
                    .distinct()
            }.getOrDefault(emptyList())
        )
    }

    fun updateSettings(transform: (KeyboardSettings) -> KeyboardSettings) {
        synchronized(settingsLock) {
            val updated = transform(_settings.value)
            if (settingsLoaded) {
                persistSettings(updated)
            } else {
                pendingUpdates += transform
            }
            _settings.value = updated
        }
    }

    private fun persistSettings(updated: KeyboardSettings) {
        prefs.edit().apply {
            putString(KEY_TYPING_METHOD, updated.typingMethod.name)
            putString(KEY_TONE_PLACEMENT, updated.tonePlacement.name)
            putString(KEY_THEME_MODE, updated.themeMode.name)
            putString(KEY_THEME_ID, updated.themeId.name)
            putInt(KEY_KEYBOARD_HEIGHT, updated.keyboardHeightPercent)
            putInt(KEY_KEY_SCALE, updated.keyScalePercent)
            putInt(KEY_ROW_SPACING, updated.rowSpacingDp)
            putInt(KEY_FONT_SCALE, updated.fontScalePercent)
            putString(KEY_ALIGNMENT, updated.alignment.name)
            putInt(KEY_BOTTOM_CHIN_HEIGHT, updated.bottomChinHeightDp)
            putBoolean(KEY_SHOW_VIRTUAL_NAV_BAR, updated.showVirtualNavBar)
            putBoolean(KEY_SMART_FEATURES_ENABLED, updated.smartFeaturesEnabled)
            putBoolean(KEY_TONE_PLACEMENT_ENABLED, updated.tonePlacementEnabled)
            putBoolean(KEY_SHOW_SUGGESTIONS, updated.showSuggestions)
            putBoolean(KEY_AUTO_CAPITALIZE, updated.autoCapitalize)
            putBoolean(KEY_HAPTIC_FEEDBACK, updated.hapticFeedback)
            putBoolean(KEY_SOUND_FEEDBACK, updated.soundFeedback)
            putBoolean(KEY_SHOW_NUMBER_ROW, updated.showNumberRow)
            putBoolean(KEY_SHOW_NUMBER_ROW_LANDSCAPE, updated.showNumberRowInLandscape)
            putBoolean(KEY_SPACE_INDICATOR, updated.spaceBarVietnameseIndicator)
            putBoolean(KEY_NAV_ARROWS, updated.navBarShowCursorArrows)
            putBoolean(KEY_NAV_LINE_JUMP, updated.navBarShowLineJump)
            putBoolean(KEY_NAV_SELECT_ALL, updated.navBarShowSelectAll)
            putBoolean(KEY_NAV_PASTE, updated.navBarShowPaste)
            putBoolean(KEY_NAV_LANGUAGE_SWITCH, updated.navBarShowLanguageSwitch)
            putBoolean(KEY_NAV_HIDE_KB, updated.navBarShowHideKeyboard)
            putBoolean(KEY_SWIPE_SPACE_CURSOR, updated.swipeSpaceToMoveCursor)
            putBoolean(KEY_HOLD_SPACE_LANG, updated.holdSpaceToSwitchLanguage)
            putBoolean(KEY_SWIPE_BACKSPACE_DELETE, updated.swipeBackspaceToDeleteFast)
            putString(KEY_ACTIVE_LANG, updated.activeLanguageId)
            putStringSet(KEY_DOWNLOADED_LANGS, updated.downloadedLanguages)
            putStringSet(KEY_ENABLED_LANGS, updated.enabledLanguages)
            putString(KEY_DISPLAY_STYLE, updated.displayStyle.name)
            putBoolean(KEY_AUTO_SPLIT_LANDSCAPE, updated.autoSplitInLandscape)
            putInt(KEY_FLOATING_SCALE, updated.floatingScalePercent)
            putBoolean(KEY_SHOW_KEY_PREVIEW, updated.showKeyPreview)
            putBoolean(KEY_SHOW_COMMA_KEY, updated.showCommaKey)
            putBoolean(KEY_SHOW_PERIOD_KEY, updated.showPeriodKey)
            putBoolean(KEY_SHOW_EMOJI_KEY, updated.showEmojiKey)
            putString(KEY_RECENT_EMOJIS, JSONArray(updated.recentEmojis.distinct().take(MAX_RECENT_EMOJIS)).toString())
            apply()
        }
    }

    fun exportSettingsMap(): Map<String, Any?> {
        return prefs.all
    }

    fun importSettingsMap(map: Map<String, Any?>) {
        val editor = prefs.edit()
        for ((key, value) in map) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> @Suppress("UNCHECKED_CAST") editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                is List<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                is Number -> editor.putInt(key, value.toInt())
            }
        }
        editor.apply()
        reloadSettings()
    }

    companion object {
        private const val PREFS_NAME = "viet_keyboard_preferences"
        private const val KEY_TYPING_METHOD = "typing_method"
        private const val KEY_TONE_PLACEMENT = "tone_placement"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_KEYBOARD_HEIGHT = "keyboard_height"
        private const val KEY_KEY_SCALE = "key_scale"
        private const val KEY_ROW_SPACING = "row_spacing"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_ALIGNMENT = "alignment"
        private const val KEY_BOTTOM_CHIN_HEIGHT = "bottom_chin_height"
        private const val KEY_SHOW_VIRTUAL_NAV_BAR = "show_virtual_nav_bar"
        private const val KEY_SMART_FEATURES_ENABLED = "smart_features_enabled"
        private const val KEY_TONE_PLACEMENT_ENABLED = "tone_placement_enabled"
        private const val KEY_SHOW_SUGGESTIONS = "show_suggestions"
        private const val KEY_AUTO_CAPITALIZE = "auto_capitalize"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SOUND_FEEDBACK = "sound_feedback"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_SHOW_NUMBER_ROW_LANDSCAPE = "show_number_row_landscape"
        private const val KEY_SPACE_INDICATOR = "space_indicator"
        private const val KEY_NAV_ARROWS = "nav_arrows"
        private const val KEY_NAV_LINE_JUMP = "nav_line_jump"
        private const val KEY_NAV_SELECT_ALL = "nav_select_all"
        private const val KEY_NAV_PASTE = "nav_paste"
        private const val KEY_NAV_LANGUAGE_SWITCH = "nav_language_switch"
        private const val KEY_NAV_HIDE_KB = "nav_hide_kb"
        private const val KEY_NAV_BAR_DEFAULTS_VERSION = "nav_bar_defaults_version"
        private const val NAV_BAR_DEFAULTS_VERSION = 1
        private const val KEY_SWIPE_SPACE_CURSOR = "swipe_space_cursor"
        private const val KEY_HOLD_SPACE_LANG = "hold_space_lang"
        private const val KEY_SWIPE_BACKSPACE_DELETE = "swipe_backspace_delete"
        private const val KEY_ACTIVE_LANG = "active_lang"
        private const val KEY_DOWNLOADED_LANGS = "downloaded_langs"
        private const val KEY_ENABLED_LANGS = "enabled_langs"
        private const val KEY_DISPLAY_STYLE = "display_style"
        private const val KEY_AUTO_SPLIT_LANDSCAPE = "auto_split_landscape"
        private const val KEY_FLOATING_SCALE = "floating_scale"
        private const val KEY_SHOW_KEY_PREVIEW = "show_key_preview"
        private const val KEY_SHOW_COMMA_KEY = "show_comma_key"
        private const val KEY_SHOW_PERIOD_KEY = "show_period_key"
        private const val KEY_SHOW_EMOJI_KEY = "show_emoji_key"
        private const val KEY_RECENT_EMOJIS = "recent_emojis"
        private const val MAX_RECENT_EMOJIS = 24

        @Volatile
        private var instance: KeyboardPreferences? = null

        fun getInstance(context: Context): KeyboardPreferences {
            return instance ?: synchronized(this) {
                instance ?: KeyboardPreferences(context).also { instance = it }
            }
        }
    }
}
