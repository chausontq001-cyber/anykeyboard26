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
        val enabledSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        ).orEmpty()
        val enabledInSettings = enabledSetting.split(':').any {
            ComponentName.unflattenFromString(it.substringBefore(';')) == ime
        }
        val manager = context.getSystemService(InputMethodManager::class.java)
        val enabledInManager = manager?.enabledInputMethodList?.any {
            it.packageName == ime.packageName && it.serviceName == ime.className
        } == true
        return enabledInSettings || enabledInManager
    }

    fun isDefault(context: Context): Boolean {
        val defaultIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return !defaultIme.isNullOrEmpty() &&
            ComponentName.unflattenFromString(defaultIme) == component(context)
    }
}
