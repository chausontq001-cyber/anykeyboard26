package com.example.ime.engine

import com.example.ime.model.TonePlacement
import com.example.ime.model.TypingMethod

class VietnameseEngine(
    var typingMethod: TypingMethod = TypingMethod.TELEX,
    var tonePlacement: TonePlacement = TonePlacement.MODERN
) {
    // Current word buffer holding raw typed characters
    private var rawBuffer = StringBuilder()

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
        composedWord = ""
        currentTone = 0
        lastKeyWasTone = null
        lastKeyWasModifier = null
    }

    fun isWordActive(): Boolean = rawBuffer.isNotEmpty()

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
            return processVni(c)
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
        rawBuffer.deleteCharAt(rawBuffer.length - 1)
        recompose()
        return true
    }

    private fun processTelex(c: Char): ProcessResult {
        val lower = c.lowercaseChar()

        // 1. Check double tone key undo (e.g. typing 's' when 's' just applied sắc -> undo to 'as')
        if (lastKeyWasTone == lower && currentTone != 0) {
            // Revert tone mark and append the literal tone key
            currentTone = 0
            lastKeyWasTone = null
            rawBuffer.append(c)
            recompose()
            return ProcessResult.Composing(composedWord)
        }

        // 2. Check tone keys: s, f, r, x, j, z
        val toneIndex = when (lower) {
            's' -> 1
            'f' -> 2
            'r' -> 3
            'x' -> 4
            'j' -> 5
            'z' -> 0
            else -> -1
        }

        if (toneIndex != -1 && rawBuffer.isNotEmpty()) {
            // Check if there are vowels in the buffer to receive the tone
            val hasVowels = rawBuffer.any { VietnameseCharUtils.isVowel(it) }
            if (hasVowels) {
                if (lower == 'z') {
                    currentTone = 0
                    lastKeyWasTone = null
                    recompose()
                    return ProcessResult.Composing(composedWord)
                }

                if (currentTone == toneIndex) {
                    // Double tone key pressed -> undo tone and insert literal
                    currentTone = 0
                    lastKeyWasTone = null
                    rawBuffer.append(c)
                    recompose()
                    return ProcessResult.Composing(composedWord)
                } else {
                    currentTone = toneIndex
                    lastKeyWasTone = lower
                    recompose()
                    return ProcessResult.Composing(composedWord)
                }
            }
        }

        // 3. Modifier keys: aa, aw, ee, oo, ow, uw, w, dd
        val lastRaw = rawBuffer.lastOrNull()?.lowercaseChar()

        if (lower == 'a' && lastRaw == 'a') {
            // aa -> â (or undo if already â)
            if (lastKeyWasModifier == 'a') {
                // Undo: change back to 'aa'
                lastKeyWasModifier = null
                rawBuffer.append(c)
            } else {
                lastKeyWasModifier = 'a'
                rawBuffer.append(c)
            }
            recompose()
            return ProcessResult.Composing(composedWord)
        }

        if (lower == 'w') {
            // w transforms preceding a -> ă, o -> ơ, u -> ư, or uo -> ươ
            lastKeyWasModifier = 'w'
            rawBuffer.append(c)
            recompose()
            return ProcessResult.Composing(composedWord)
        }

        if (lower == 'e' && lastRaw == 'e') {
            lastKeyWasModifier = 'e'
            rawBuffer.append(c)
            recompose()
            return ProcessResult.Composing(composedWord)
        }

        if (lower == 'o' && lastRaw == 'o') {
            lastKeyWasModifier = 'o'
            rawBuffer.append(c)
            recompose()
            return ProcessResult.Composing(composedWord)
        }

        if (lower == 'd' && lastRaw == 'd') {
            lastKeyWasModifier = 'd'
            rawBuffer.append(c)
            recompose()
            return ProcessResult.Composing(composedWord)
        }

        // Regular character append
        lastKeyWasTone = null
        lastKeyWasModifier = null
        rawBuffer.append(c)
        recompose()
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
        if (rawBuffer.isEmpty()) {
            composedWord = ""
            return
        }

        val raw = rawBuffer.toString()
        var text = raw

        if (typingMethod == TypingMethod.TELEX) {
            text = applyTelexVowels(text)
        } else if (typingMethod == TypingMethod.VNI) {
            text = applyVniVowels(text)
        }

        // Now place currentTone on the correct vowel
        if (currentTone > 0) {
            text = applyToneToComposedText(text, currentTone, tonePlacement)
        }

        composedWord = text
    }

    private fun applyTelexVowels(input: String): String {
        var res = input

        // dd -> đ
        res = replaceCasePreserving(res, "dd", "đ")

        // aa -> â
        res = replaceCasePreserving(res, "aa", "â")

        // aw -> ă
        res = replaceCasePreserving(res, "aw", "ă")

        // ee -> ê
        res = replaceCasePreserving(res, "ee", "ê")

        // oo -> ô
        res = replaceCasePreserving(res, "oo", "ô")

        // ow -> ơ
        res = replaceCasePreserving(res, "ow", "ơ")

        // uw -> ư
        res = replaceCasePreserving(res, "uw", "ư")

        // uow -> ươ
        res = replaceCasePreserving(res, "uow", "ươ")

        // w at the start or after consonants -> ư
        if (res.startsWith("w", ignoreCase = true)) {
            val isUpper = res.first().isUpperCase()
            res = (if (isUpper) "Ư" else "ư") + res.substring(1)
        }

        // uo + w or u + w
        res = replaceCasePreserving(res, "uow", "ươ")
        res = replaceCasePreserving(res, "uw", "ư")
        res = replaceCasePreserving(res, "ow", "ơ")

        // Handle standalone 'w' after 'u' and 'o' clusters like 'duongw' -> 'đương'
        if (res.contains("uongw", ignoreCase = true)) {
            res = replaceCasePreserving(res, "uongw", "ương")
        } else if (res.contains("uow", ignoreCase = true)) {
            res = replaceCasePreserving(res, "uow", "ươ")
        } else if (res.contains("uo", ignoreCase = true) && res.endsWith("w", ignoreCase = true)) {
            res = replaceCasePreserving(res.dropLast(1), "uo", "ươ")
        }

        return res
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
        // Find vowel clusters
        var firstVowel = -1
        var lastVowel = -1

        for (i in text.indices) {
            if (VietnameseCharUtils.isVowel(text[i])) {
                if (firstVowel == -1) firstVowel = i
                lastVowel = i
            }
        }

        if (firstVowel == -1) return text // No vowels found

        val vowelCluster = text.substring(firstVowel, lastVowel + 1)
        val hasEndingConsonant = lastVowel < text.length - 1

        val relativeToneIndex = VietnameseCharUtils.findToneVowelIndex(
            vowelCluster,
            hasEndingConsonant,
            placement
        )

        val targetCharIndex = firstVowel + relativeToneIndex
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
}
