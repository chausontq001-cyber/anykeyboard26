package com.example.ime

import android.content.Context
import android.provider.Settings
import androidx.core.view.WindowInsetsCompat

object NavigationBarPolicy {
    private const val NAVIGATION_MODE_SETTING = "navigation_mode"
    private const val THREE_BUTTON_NAVIGATION_MODE = 0
    private const val GESTURAL_NAVIGATION_MODE = 2
    private const val UNKNOWN_NAVIGATION_MODE = -1

    fun shouldHideVirtualNavigationBar(
        context: Context,
        insets: WindowInsetsCompat
    ): Boolean {
        if (insets.isVisible(WindowInsetsCompat.Type.navigationBars())) return false

        val navigationMode = Settings.Secure.getInt(
            context.contentResolver,
            NAVIGATION_MODE_SETTING,
            UNKNOWN_NAVIGATION_MODE
        )
        val isGestureNavigation = if (navigationMode != UNKNOWN_NAVIGATION_MODE) {
            navigationMode == GESTURAL_NAVIGATION_MODE
        } else {
            val systemGestureBottom =
                insets.getInsets(WindowInsetsCompat.Type.systemGestures()).bottom
            val tappableElementBottom =
                insets.getInsets(WindowInsetsCompat.Type.tappableElement()).bottom
            systemGestureBottom > 0 && tappableElementBottom == 0
        }

        val isThreeButtonNavigation =
            navigationMode == THREE_BUTTON_NAVIGATION_MODE || navigationMode == UNKNOWN_NAVIGATION_MODE
        return isThreeButtonNavigation && !isGestureNavigation
    }

}
