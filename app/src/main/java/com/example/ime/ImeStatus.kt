package com.example.ime

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import com.example.ime.service.VietnameseInputMethodService

object ImeStatus {
    private fun component(context: Context) =
        ComponentName(context, VietnameseInputMethodService::class.java)

    fun isEnabled(context: Context): Boolean {
        val ime = component(context)
        val manager = context.getSystemService(InputMethodManager::class.java)
        val enabledInManager = manager?.enabledInputMethodList?.any {
            it.packageName == ime.packageName && it.serviceName == ime.className
        } == true

        if (enabledInManager) return true

        val enabledSetting = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_INPUT_METHODS
            ).orEmpty()
        } catch (_: SecurityException) {
            ""
        } catch (_: Exception) {
            ""
        }

        return enabledSetting.split(':').any {
            val unflattened = ComponentName.unflattenFromString(it.substringBefore(';'))
            unflattened == ime || (unflattened?.packageName == ime.packageName && unflattened?.className?.endsWith("VietnameseInputMethodService") == true)
        }
    }

    fun isDefault(context: Context): Boolean {
        val ime = component(context)
        val defaultIme = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }

        if (!defaultIme.isNullOrEmpty()) {
            val unflattened = ComponentName.unflattenFromString(defaultIme)
            return unflattened == ime || (unflattened?.packageName == ime.packageName && unflattened?.className?.endsWith("VietnameseInputMethodService") == true)
        }

        return false
    }
}
