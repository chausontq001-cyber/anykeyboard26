package com.example

import com.example.ime.engine.KotlinTelexEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinTelexEngineTest {
    @Test
    fun appliesVowelModifiersByScanningBackwardsAndHandlesSmartClusters() {
        assertEquals("bâm", type("bama"))
        assertEquals("bưm", type("bumw"))
        assertEquals("dươc", type("duocw"))
        assertEquals("iê", type("iea"))
        assertEquals("uô", type("uoo"))
        assertEquals("aw", type("aww"))
    }

    @Test
    fun convertsToneKeysAndPlacesToneInSpecifiedVowelClusters() {
        assertEquals("á", type("as"))
        assertEquals("oá", type("oas"))
        assertEquals("oái", type("oais"))
        assertEquals("uáy", type("uays"))
    }

    @Test
    fun repeatingTheModifierRevertsAndLocksTelexUntilReset() {
        val engine = KotlinTelexEngine()

        assertEquals("á", type(engine, "as"))
        assertEquals("as", engine.processKey("s"))
        assertTrue(engine.isReverted)
        assertEquals("ass", engine.processKey("s"))

        engine.reset()
        assertFalse(engine.isReverted)
        assertEquals("ă", engine.processKey("aw"))
        assertEquals("aw", engine.processKey("w"))
        assertTrue(engine.isReverted)
        assertEquals("aww", engine.processKey("w"))
    }

    @Test
    fun backspaceRestoresThePreviousCompositionState() {
        val engine = KotlinTelexEngine()
        assertEquals("â", engine.processKey("aa"))
        assertEquals("a", engine.backspace())
        assertEquals("", engine.backspace())
    }

    private fun type(input: String): String = type(KotlinTelexEngine(), input)

    private fun type(engine: KotlinTelexEngine, input: String): String =
        input.fold("") { _, key -> engine.processKey(key.toString()) }
}
