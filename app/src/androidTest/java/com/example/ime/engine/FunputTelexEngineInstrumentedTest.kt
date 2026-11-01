package com.example.ime.engine

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FunputTelexEngineInstrumentedTest {
    @Test
    fun nativeEngineUsesAppTonePlacementLabels() {
        val modernEngine = FunputTelexEngine()
        try {
            modernEngine.configure(TonePlacement.MODERN)
            var modernResult = ""
            "hoas".forEach { modernResult = modernEngine.process(it.code) }
            assertEquals("hóa", modernResult)
        } finally {
            modernEngine.close()
        }

        val classicEngine = FunputTelexEngine()
        try {
            classicEngine.configure(TonePlacement.CLASSIC)
            var classicResult = ""
            "hoas".forEach { classicResult = classicEngine.process(it.code) }
            assertEquals("hoá", classicResult)
        } finally {
            classicEngine.close()
        }
    }

    @Test
    fun nativeEngineComposesTelexAndSupportsBackspace() {
        val engine = VietnameseEngine(
            typingMethod = TypingMethod.TELEX,
            tonePlacement = TonePlacement.MODERN
        )

        try {
            "vieetj".forEach { engine.processChar(it) }
            assertEquals("việt", engine.composedWord)

            assertTrue(engine.processBackspace())
            "j".forEach { engine.processChar(it) }
            assertEquals("việt", engine.composedWord)

            engine.reset()
            "toans".forEach { engine.processChar(it) }
            assertEquals("toán", engine.composedWord)

            engine.reset()
            "casf".forEach { engine.processChar(it) }
            assertEquals("cà", engine.composedWord)

            engine.reset()
            "tieengs".forEach { engine.processChar(it) }
            assertEquals("tiếng", engine.composedWord)

            engine.reset()
            "ddươfng".forEach { engine.processChar(it) }
            assertEquals("đường", engine.composedWord)
        } finally {
            engine.close()
        }
    }
}
