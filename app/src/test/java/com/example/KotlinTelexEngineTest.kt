package com.example

import com.example.ime.engine.KotlinTelexEngine
import com.example.ime.model.TonePlacement
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
        assertEquals("ư", type("uw"))
        assertEquals("ươ", type("uow"))
    }

    @Test
    fun convertsToneKeysAndPlacesToneInSpecifiedVowelClusters() {
        assertEquals("á", type("as"))
        assertEquals("óa", type("oas"))
        assertEquals("oái", type("oais"))
        assertEquals("uáy", type("uays"))
    }

    @Test
    fun appliesModernAndClassicTonePlacementWithoutChangingOtherSyllableRules() {
        assertEquals("hóa", type("hoas", TonePlacement.MODERN))
        assertEquals("hoá", type("hoas", TonePlacement.CLASSIC))
        assertEquals("khỏe", type("khoer", TonePlacement.MODERN))
        assertEquals("khoẻ", type("khoer", TonePlacement.CLASSIC))
        assertEquals("thúy", type("thuys", TonePlacement.MODERN))
        assertEquals("thuý", type("thuys", TonePlacement.CLASSIC))
        assertEquals("hoàng", type("hoangf", TonePlacement.MODERN))
        assertEquals("hoàng", type("hoangf", TonePlacement.CLASSIC))
        assertEquals("quá", type("quas", TonePlacement.MODERN))
        assertEquals("giá", type("gias", TonePlacement.CLASSIC))
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
    fun repeatedVowelAndConsonantModifiersRevertAndLockSubsequentKeys() {
        val cases = listOf(
            "aaa" to "aa",
            "eee" to "ee",
            "ooo" to "oo",
            "ddd" to "dd",
            "uww" to "uw",
            "duoww" to "duow"
        )
        cases.forEach { (input, expected) ->
            val engine = KotlinTelexEngine()
            assertEquals(input, expected, type(engine, input))
            assertTrue("$input should enable the Telex lock", engine.isReverted)
            assertEquals("$input should append keys literally after reverting", expected + "z", engine.processKey("z"))
        }
    }

    @Test
    fun revertedTelexModifierKeysAreAppendedLiterally() {
        val engine = KotlinTelexEngine()
        assertEquals("deodd", type(engine, "deoddd"))
        assertTrue(engine.isReverted)
        assertEquals("deoddd", engine.processKey("d"))
    }

    @Test
    fun backspaceRestoresThePreviousCompositionState() {
        val engine = KotlinTelexEngine()
        assertEquals("â", engine.processKey("aa"))
        assertEquals("a", engine.backspace())
        assertEquals("", engine.backspace())
    }

    private fun type(input: String): String = type(KotlinTelexEngine(), input)

    private fun type(input: String, placement: TonePlacement): String =
        type(KotlinTelexEngine().apply { configure(placement) }, input)

    private fun type(engine: KotlinTelexEngine, input: String): String =
        input.fold("") { _, key -> engine.processKey(key.toString()) }
}
