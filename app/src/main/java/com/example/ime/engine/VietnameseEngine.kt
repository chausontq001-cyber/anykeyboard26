package com.example.ime.engine

import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod

class VietnameseEngine(
    typingMethod: TypingMethod = TypingMethod.TELEX,
    tonePlacement: TonePlacement = TonePlacement.MODERN,
    private val telexEngineFactory: () -> TelexEngine = { KotlinTelexEngine() }
) : AutoCloseable {
    var typingMethod: TypingMethod = typingMethod
        set(value) {
            if (field == value) return
            field = value
            vniHistory.clear()
            recompose()
        }

    var tonePlacement: TonePlacement = tonePlacement
        set(value) {
            if (field == value) return
            field = value
            telexEngine?.configure(value)
            recompose()
        }

    // Current word buffer holding raw typed characters
    private var rawBuffer = StringBuilder()
    private var telexEngine: TelexEngine? = null
    private val vniHistory = mutableListOf<VniState>()

    // Composed word displayed to the user
    var composedWord: String = ""
        private set

    // Tone applied: 0 = none, 1 = acute, 2 = grave, 3 = hook, 4 = tilde, 5 = dot
    private var currentTone: Int = 0

    // Last action record for double-key undo
    private var lastKeyWasTone: Char? = null
    private var lastKeyWasModifier: Char? = null

    fun reset() {
        rawBuffer.clear()
        vniHistory.clear()
        composedWord = ""
        currentTone = 0
        lastKeyWasTone = null
        lastKeyWasModifier = null
        telexEngine?.clear()
    }

    fun isWordActive(): Boolean = rawBuffer.isNotEmpty()

    override fun close() {
        telexEngine?.close()
        telexEngine = null
    }

    /**
     * Called when a key is typed.
     * Returns true if handled within the active word composition,
     * or false if it's a word-breaking character (like space, punctuation).
     */
    fun processChar(c: Char): ProcessResult {
        if (typingMethod == TypingMethod.OFF) {
            return ProcessResult.Unhandled(c)
        }

        // Check if character is a word boundary (space, punctuation, newline, symbol)
        if (!c.isLetter() && !(typingMethod == TypingMethod.VNI && c.isDigit())) {
            val committed = composedWord
            reset()
            return ProcessResult.WordBoundary(committed, c)
        }

        if (typingMethod == TypingMethod.TELEX) {
            return processTelex(c)
        } else if (typingMethod == TypingMethod.VNI) {
            val previousState = snapshotVniState()
            val result = processVni(c)
            if (result is ProcessResult.Composing) {
                vniHistory += previousState
            }
            return result
        }

        return ProcessResult.Unhandled(c)
    }

    /**
     * Backspace handling
     * Returns true if handled by removing from buffer, false if buffer was empty.
     */
    fun processBackspace(): Boolean {
        if (rawBuffer.isEmpty()) {
            return false
        }
        if (typingMethod == TypingMethod.VNI) {
            val previousState = vniHistory.removeLastOrNull() ?: return false
            rawBuffer.setLength(0)
            rawBuffer.append(previousState.rawText)
            currentTone = previousState.tone
            lastKeyWasTone = previousState.lastToneKey
            lastKeyWasModifier = previousState.lastModifierKey
            recompose()
            return true
        }

        rawBuffer.deleteCharAt(rawBuffer.length - 1)
        composedWord = if (typingMethod == TypingMethod.TELEX) {
            getTelexEngine().backspace()
        } else {
            recompose()
            composedWord
        }
        return true
    }

    private fun snapshotVniState() = VniState(
        rawText = rawBuffer.toString(),
        tone = currentTone,
        lastToneKey = lastKeyWasTone,
        lastModifierKey = lastKeyWasModifier
    )

    private fun processTelex(c: Char): ProcessResult {
        rawBuffer.append(c)
        composedWord = getTelexEngine().process(c.code)
        return ProcessResult.Composing(composedWord)
    }

    private fun processVni(c: Char): ProcessResult {
        // VNI Tone and modifiers by numbers:
        // 1: sắc, 2: huyền, 3: hỏi, 4: ngã, 5: nặng, 0: gỡ dấu
        // 6: mũ (â, ê, ô), 7: móc (ơ, ư), 8: trăng (ă), 9: đ
        if (c.isDigit()) {
            if (rawBuffer.isEmpty()) {
                return ProcessResult.Unhandled(c)
            }

            when (c) {
                '1' -> applyVniTone(1, c)
                '2' -> applyVniTone(2, c)
                '3' -> applyVniTone(3, c)
                '4' -> applyVniTone(4, c)
                '5' -> applyVniTone(5, c)
                '0' -> {
                    currentTone = 0
                    lastKeyWasTone = null
                    recompose()
                }
                '6', '7', '8', '9' -> {
                    rawBuffer.append(c)
                    recompose()
                }
                else -> {
                    val committed = composedWord
                    reset()
                    return ProcessResult.WordBoundary(committed, c)
                }
            }
            return ProcessResult.Composing(composedWord)
        } else {
            // Regular letter
            lastKeyWasTone = null
            lastKeyWasModifier = null
            rawBuffer.append(c)
            recompose()
            return ProcessResult.Composing(composedWord)
        }
    }

    private fun applyVniTone(toneIndex: Int, digit: Char) {
        if (currentTone == toneIndex) {
            currentTone = 0
            lastKeyWasTone = null
            rawBuffer.append(digit)
        } else {
            currentTone = toneIndex
            lastKeyWasTone = digit
        }
        recompose()
    }

    /**
     * Recompose the string from rawBuffer according to Vietnamese phonetic rules.
     */
    private fun recompose() {
        if (typingMethod == TypingMethod.TELEX) {
            if (rawBuffer.isEmpty()) {
                telexEngine?.clear()
                composedWord = ""
                return
            }
            val engine = getTelexEngine()
            engine.clear()
            composedWord = rawBuffer.fold("") { _, c -> engine.process(c.code) }
            return
        }

        telexEngine?.clear()
        if (rawBuffer.isEmpty()) {
            composedWord = ""
            return
        }

        var text = rawBuffer.toString()
        if (typingMethod == TypingMethod.VNI) {
            text = applyVniVowels(text)
        }
        if (currentTone > 0) {
            text = applyToneToComposedText(text, currentTone, tonePlacement)
        }
        composedWord = text
    }

    private fun getTelexEngine(): TelexEngine =
        telexEngine ?: telexEngineFactory().also {
            it.configure(tonePlacement)
            telexEngine = it
        }

    private fun applyVniVowels(input: String): String {
        var res = input

        // 9 -> d -> đ
        res = replaceCasePreserving(res, "d9", "đ")

        // 6 -> â, ê, ô
        res = replaceCasePreserving(res, "a6", "â")
        res = replaceCasePreserving(res, "e6", "ê")
        res = replaceCasePreserving(res, "o6", "ô")

        // 7 -> ơ, ư, ươ
        res = replaceCasePreserving(res, "uo7", "ươ")
        res = replaceCasePreserving(res, "u7", "ư")
        res = replaceCasePreserving(res, "o7", "ơ")

        // 8 -> ă
        res = replaceCasePreserving(res, "a8", "ă")

        return res
    }

    private fun replaceCasePreserving(source: String, target: String, replacement: String): String {
        val index = source.indexOf(target, ignoreCase = true)
        if (index == -1) return source

        val matched = source.substring(index, index + target.length)
        val isAllUpper = matched.all { it.isUpperCase() }
        val isFirstUpper = matched.first().isUpperCase()

        val finalReplacement = when {
            isAllUpper -> replacement.uppercase()
            isFirstUpper -> replacement.replaceFirstChar { it.uppercase() }
            else -> replacement.lowercase()
        }

        return source.substring(0, index) + finalReplacement + source.substring(index + target.length)
    }

    private fun applyToneToComposedText(
        text: String,
        tone: Int,
        placement: TonePlacement
    ): String {
        val targetCharIndex = VietnameseCharUtils.findToneVowelIndex(text, placement)
        if (targetCharIndex < 0) return text
        val targetChar = text[targetCharIndex]
        val tonedChar = VietnameseCharUtils.applyTone(targetChar, tone)

        val sb = java.lang.StringBuilder(text)
        sb.setCharAt(targetCharIndex, tonedChar)
        return sb.toString()
    }

    sealed class ProcessResult {
        data class Composing(val text: String) : ProcessResult()
        data class WordBoundary(val committedWord: String, val boundaryChar: Char) : ProcessResult()
        data class Unhandled(val char: Char) : ProcessResult()
    }

    private data class VniState(
        val rawText: String,
        val tone: Int,
        val lastToneKey: Char?,
        val lastModifierKey: Char?
    )
}
