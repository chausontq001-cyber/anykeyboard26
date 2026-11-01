package com.example

import com.example.ime.engine.VietnameseDictionary
import com.example.ime.engine.TelexEngine
import com.example.ime.engine.VietnameseEngine
import com.example.ime.engine.VietnameseCharUtils
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod
import com.example.ime.model.KeyboardSettings
import com.example.ime.model.WorldLanguagesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VietnameseEngineTest {

    @Test
    fun testTelexDelegatesCompositionAndBackspace() {
        val telexEngine = FakeTelexEngine()
        val engine = VietnameseEngine(
            typingMethod = TypingMethod.TELEX,
            tonePlacement = TonePlacement.MODERN,
            telexEngineFactory = { telexEngine }
        )

        "abc".forEach { engine.processChar(it) }
        assertEquals("abc", engine.composedWord)
        assertTrue(engine.processBackspace())
        assertEquals("ab", engine.composedWord)

        engine.tonePlacement = TonePlacement.CLASSIC
        assertEquals(TonePlacement.CLASSIC, telexEngine.tonePlacement)
        engine.reset()
        assertEquals("", engine.composedWord)
        engine.close()
    }

    @Test
    fun testVniBasic() {
        val engine = VietnameseEngine(
            typingMethod = TypingMethod.VNI,
            tonePlacement = TonePlacement.MODERN
        )

        // In VNI: 'v', 'i', 'e', '6', 't', '5' -> 'việt'
        "vie6t5".forEach { engine.processChar(it) }
        assertEquals("việt", engine.composedWord)

        engine.reset()
        // Type 't', 'o', 'a', 'n', '1' -> 'toán'
        "toan1".forEach { engine.processChar(it) }
        assertEquals("toán", engine.composedWord)

        engine.reset()
        // Type 'd', '9', 'u', 'o', '7', 'n', 'g', '2' -> 'đường'
        "d9uo7ng2".forEach { engine.processChar(it) }
        assertEquals("đường", engine.composedWord)
    }

    @Test
    fun vniBackspaceRestoresToneAndModifierStateOneKeyAtATime() {
        val engine = VietnameseEngine(typingMethod = TypingMethod.VNI)

        "toan1".forEach { engine.processChar(it) }
        assertEquals("toán", engine.composedWord)

        assertTrue(engine.processBackspace())
        assertEquals("toan", engine.composedWord)

        engine.processChar('2')
        assertEquals("toàn", engine.composedWord)

        engine.reset()
        "vie6t5".forEach { engine.processChar(it) }
        assertEquals("việt", engine.composedWord)

        assertTrue(engine.processBackspace())
        assertEquals("viêt", engine.composedWord)

        engine.close()
    }

    @Test
    fun vniHonorsModernAndClassicTonePlacement() {
        val engine = VietnameseEngine(
            typingMethod = TypingMethod.VNI,
            tonePlacement = TonePlacement.MODERN
        )

        "hoa1".forEach { engine.processChar(it) }
        assertEquals("hóa", engine.composedWord)

        engine.reset()
        engine.tonePlacement = TonePlacement.CLASSIC
        "hoa1".forEach { engine.processChar(it) }
        assertEquals("hoá", engine.composedWord)

        engine.reset()
        "khoe3".forEach { engine.processChar(it) }
        assertEquals("khoẻ", engine.composedWord)

        engine.reset()
        "thuy1".forEach { engine.processChar(it) }
        assertEquals("thuý", engine.composedWord)

        engine.reset()
        "toan1".forEach { engine.processChar(it) }
        assertEquals("toán", engine.composedWord)

        engine.reset()
        "qua3".forEach { engine.processChar(it) }
        assertEquals("quả", engine.composedWord)

        engine.reset()
        "gia1".forEach { engine.processChar(it) }
        assertEquals("giá", engine.composedWord)
        engine.close()
    }

    @Test
    fun telexSequenceRestoresCommittedVietnameseTextForRecomposition() {
        assertEquals("cas", VietnameseCharUtils.toTelexSequence("cá"))
        assertEquals("ddươfng", VietnameseCharUtils.toTelexSequence("đường"))
    }

    @Test
    fun simplifiedTelexComposesWAndVowelModifiersWithoutTreatingWAsAStandaloneModifier() {
        val engine = VietnameseEngine(typingMethod = TypingMethod.TELEX)

        "w".forEach { engine.processChar(it) }
        assertEquals("w", engine.composedWord)

        engine.reset()
        "awowuw".forEach { engine.processChar(it) }
        assertEquals("ăơư", engine.composedWord)

        engine.reset()
        "aww".forEach { engine.processChar(it) }
        assertEquals("aw", engine.composedWord)

        engine.reset()
        "aas".forEach { engine.processChar(it) }
        assertEquals("ấ", engine.composedWord)

        engine.reset()
        "aa".forEach { engine.processChar(it) }
        assertEquals("â", engine.composedWord)

        engine.reset()
        "ee".forEach { engine.processChar(it) }
        assertEquals("ê", engine.composedWord)

        engine.reset()
        "oo".forEach { engine.processChar(it) }
        assertEquals("ô", engine.composedWord)

        engine.reset()
        "bama".forEach { engine.processChar(it) }
        assertEquals("bâm", engine.composedWord)

        engine.reset()
        "beme".forEach { engine.processChar(it) }
        assertEquals("bêm", engine.composedWord)

        engine.reset()
        "bomo".forEach { engine.processChar(it) }
        assertEquals("bôm", engine.composedWord)

        engine.reset()
        "dd".forEach { engine.processChar(it) }
        assertEquals("đ", engine.composedWord)
        engine.close()
    }

    @Test
    fun virtualNavigationDefaultsToLanguageAndHideKeyboardButtons() {
        val settings = KeyboardSettings()

        assertTrue(settings.navBarShowLanguageSwitch)
        assertTrue(settings.navBarShowHideKeyboard)
        assertTrue(!settings.navBarShowCursorArrows)
        assertTrue(!settings.navBarShowLineJump)
        assertTrue(!settings.navBarShowSelectAll)
        assertTrue(!settings.navBarShowPaste)
    }

    @Test
    fun onlyLanguagesWithAnIntegratedKeyboardEngineAreSupported() {
        assertTrue(WorldLanguagesRepository.isSupported("vi_telex"))
        assertTrue(WorldLanguagesRepository.isSupported("vi_vni"))
        assertTrue(WorldLanguagesRepository.isSupported("en_us"))
        assertFalse(WorldLanguagesRepository.isSupported("fr_fr"))
        assertFalse(WorldLanguagesRepository.isSupported("ja_jp"))
    }

    @Test
    fun testDictionarySuggestions() {
        val suggestionsForXin = VietnameseDictionary.getSuggestions("xin", null)
        assertTrue(suggestionsForXin.contains("chào") || suggestionsForXin.contains("lỗi"))

        assertTrue(!VietnameseDictionary.getSuggestions("dc", null).contains("được"))
    }

    private class FakeTelexEngine : TelexEngine() {
        private val buffer = StringBuilder()
        override var tonePlacement = TonePlacement.MODERN

        override fun configure(tonePlacement: TonePlacement) {
            this.tonePlacement = tonePlacement
        }

        override fun process(codePoint: Int): String {
            buffer.appendCodePoint(codePoint)
            return buffer.toString()
        }

        override fun backspace(): String {
            buffer.deleteCharAt(buffer.length - 1)
            return buffer.toString()
        }

        override fun clear() {
            buffer.clear()
        }

        override fun close() = Unit
    }
}
