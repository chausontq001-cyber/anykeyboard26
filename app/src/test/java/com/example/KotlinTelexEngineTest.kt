package com.example

import com.example.ime.engine.KotlinTelexEngine
import com.example.ime.engine.VietnameseEngine
import com.example.ime.model.TonePlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinTelexEngineTest {
    @Test
    fun vietnameseEngineUsesKotlinTelexByDefault() {
        val engine = VietnameseEngine()
        try {
            "vieetj".forEach { engine.processChar(it) }
            assertEquals("việt", engine.composedWord)
        } finally {
            engine.close()
        }
    }

    @Test
    fun composesTelexModifiersAndTones() {
        assertEquals("việt", type("vieetj"))
        assertEquals("đường", type("ddươfng"))
        assertEquals("hóa", type("hoas", TonePlacement.MODERN))
        assertEquals("hoá", type("hoas", TonePlacement.CLASSIC))
    }

    @Test
    fun repeatedModifierRevertsAndBackspaceRestoresPreviousState() {
        val engine = KotlinTelexEngine()
        assertEquals("â", engine.processKey("aa"))
        assertEquals("a", engine.backspace())
        assertEquals("á", engine.processKey("s"))
        assertEquals("as", engine.processKey("s"))
        assertTrue(engine.isReverted)
    }

    private fun type(input: String, placement: TonePlacement = TonePlacement.MODERN): String {
        val engine = KotlinTelexEngine().apply { configure(placement) }
        return input.fold("") { _, key -> engine.process(key.code) }
    }
}
