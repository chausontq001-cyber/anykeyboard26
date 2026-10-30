package com.example

import com.example.ime.engine.VietnameseDictionary
import com.example.ime.engine.VietnameseEngine
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VietnameseEngineTest {

    @Test
    fun testTelexBasic() {
        val engine = VietnameseEngine(
            typingMethod = TypingMethod.TELEX,
            tonePlacement = TonePlacement.MODERN
        )

        // In Telex: 'v', 'i', 'e', 'e', 't', 'j' -> 'việt'
        "vieetj".forEach { engine.processChar(it) }
        assertEquals("việt", engine.composedWord)

        engine.reset()
        // Type 't', 'o', 'a', 'n', 's' -> 'toán'
        "toans".forEach { engine.processChar(it) }
        assertEquals("toán", engine.composedWord)

        engine.reset()
        // Type 't', 'i', 'e', 'e', 'n', 'g', 's' -> 'tiếng'
        "tieengs".forEach { engine.processChar(it) }
        assertEquals("tiếng", engine.composedWord)

        engine.reset()
        // Type 'd', 'd', 'u', 'o', 'n', 'g', 'w', 'f' -> 'đường'
        "dduongwf".forEach { engine.processChar(it) }
        assertEquals("đường", engine.composedWord)
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
    fun testSmartDictionarySuggestions() {
        val suggestionsForXin = VietnameseDictionary.getSuggestions("xin", null)
        assertTrue(suggestionsForXin.contains("chào") || suggestionsForXin.contains("lỗi"))

        val shortcuts = VietnameseDictionary.getSuggestions("dc", null)
        assertTrue(shortcuts.contains("được"))
    }
}
