package com.example

import android.content.Context
import android.content.ComponentName
import android.provider.Settings
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import com.example.ime.ImeStatus
import com.example.ime.NavigationBarPolicy
import com.example.ime.service.VietnameseInputMethodService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AnyKeyboard26", appName)
  }

  @Test
  fun `ime status follows secure settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val ime = ComponentName(context, VietnameseInputMethodService::class.java)

    Settings.Secure.putString(
      context.contentResolver,
      Settings.Secure.ENABLED_INPUT_METHODS,
      "${ime.flattenToShortString()};123"
    )
    Settings.Secure.putString(
      context.contentResolver,
      Settings.Secure.DEFAULT_INPUT_METHOD,
      ime.flattenToShortString()
    )

    assertTrue(ImeStatus.isEnabled(context))
    assertTrue(ImeStatus.isDefault(context))

    Settings.Secure.putString(
      context.contentResolver,
      Settings.Secure.DEFAULT_INPUT_METHOD,
      null
    )
    assertFalse(ImeStatus.isDefault(context))
  }

  @Test
  fun `hide virtual navigation bar only for hidden non-gesture navigation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val navigationBars = WindowInsetsCompat.Type.navigationBars()
    val systemGestures = WindowInsetsCompat.Type.systemGestures()
    val tappableElement = WindowInsetsCompat.Type.tappableElement()
    val hiddenNavigationBarInsets = WindowInsetsCompat.Builder()
      .setVisible(navigationBars, false)
      .setInsets(systemGestures, Insets.of(0, 0, 0, 0))
      .setInsets(tappableElement, Insets.of(0, 0, 0, 0))
      .build()

    Settings.Secure.putInt(context.contentResolver, "navigation_mode", 0)
    assertTrue(
      NavigationBarPolicy.shouldHideVirtualNavigationBar(context, hiddenNavigationBarInsets)
    )

    Settings.Secure.putInt(context.contentResolver, "navigation_mode", 2)
    assertFalse(
      NavigationBarPolicy.shouldHideVirtualNavigationBar(context, hiddenNavigationBarInsets)
    )

    Settings.Secure.putInt(context.contentResolver, "navigation_mode", 1)
    assertFalse(
      NavigationBarPolicy.shouldHideVirtualNavigationBar(context, hiddenNavigationBarInsets)
    )

    Settings.Secure.putInt(context.contentResolver, "navigation_mode", 0)
    val visibleNavigationBarInsets = WindowInsetsCompat.Builder()
      .setVisible(navigationBars, true)
      .build()
    assertFalse(
      NavigationBarPolicy.shouldHideVirtualNavigationBar(context, visibleNavigationBarInsets)
    )
  }

  @Test
  fun `infer gesture navigation from insets when setting is unavailable`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    Settings.Secure.putString(context.contentResolver, "navigation_mode", null)
    val hiddenGestureNavigationInsets = WindowInsetsCompat.Builder()
      .setVisible(WindowInsetsCompat.Type.navigationBars(), false)
      .setInsets(
        WindowInsetsCompat.Type.systemGestures(),
        Insets.of(0, 0, 0, 24)
      )
      .setInsets(
        WindowInsetsCompat.Type.tappableElement(),
        Insets.of(0, 0, 0, 0)
      )
      .build()

    assertFalse(
      NavigationBarPolicy.shouldHideVirtualNavigationBar(context, hiddenGestureNavigationInsets)
    )
  }
}
