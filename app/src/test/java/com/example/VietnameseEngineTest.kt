package com.example

import com.example.ime.engine.VietnameseDictionary
import com.example.ime.engine.TelexEngine
import com.example.ime.engine.VietnameseEngine
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod
import org.junit.Assert.assertEquals
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
    fun testDictionarySuggestions() {
        val suggestionsForXin = VietnameseDictionary.getSuggestions("xin", null)
        assertTrue(suggestionsForXin.contains("chào") || suggestionsForXin.contains("lỗi"))

        assertTrue(!VietnameseDictionary.getSuggestions("dc", null).contains("được"))
    }

    private class FakeTelexEngine : TelexEngine {
        private val buffer = StringBuilder()
        var tonePlacement = TonePlacement.MODERN
            private set

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
