package com.example.ime.engine

import com.example.ime.model.TonePlacement

/**
 * Pure Kotlin implementation of Vietnamese Telex input engine.
 * Requires no native C++/Rust JNI libraries, avoiding UnsatisfiedLinkError crashes.
 */
class KotlinTelexEngine : TelexEngine {

    private val rawChars = mutableListOf<Char>()
    private var tonePlacement: TonePlacement = TonePlacement.MODERN

    override fun configure(tonePlacement: TonePlacement) {
        this.tonePlacement = tonePlacement
    }

    override fun process(codePoint: Int): String {
        val c = codePoint.toChar()
        rawChars.add(c)
        return compose()
    }

    override fun backspace(): String {
        if (rawChars.isNotEmpty()) {
            rawChars.removeAt(rawChars.lastIndex)
        }
        return compose()
    }

    override fun clear() {
        rawChars.clear()
    }

    override fun close() {
        rawChars.clear()
    }

    private fun compose(): String {
        if (rawChars.isEmpty()) return ""

        var text = StringBuilder()
        var currentTone = 0
        var lastToneChar: Char? = null

        for (ch in rawChars) {
            val lower = ch.lowercaseChar()
            val isUpper = ch.isUpperCase()

            when (lower) {
                's' -> {
                    if (currentTone == 1 && lastToneChar == lower) {
                        currentTone = 0
                        lastToneChar = null
                        text.append(ch)
                    } else if (hasVowel(text)) {
                        currentTone = 1
                        lastToneChar = lower
                    } else {
                        text.append(ch)
                    }
                }
                'f' -> {
                    if (currentTone == 2 && lastToneChar == lower) {
                        currentTone = 0
                        lastToneChar = null
                        text.append(ch)
                    } else if (hasVowel(text)) {
                        currentTone = 2
                        lastToneChar = lower
                    } else {
                        text.append(ch)
                    }
                }
                'r' -> {
                    if (currentTone == 3 && lastToneChar == lower) {
                        currentTone = 0
                        lastToneChar = null
                        text.append(ch)
                    } else if (hasVowel(text)) {
                        currentTone = 3
                        lastToneChar = lower
                    } else {
                        text.append(ch)
                    }
                }
                'x' -> {
                    if (currentTone == 4 && lastToneChar == lower) {
                        currentTone = 0
                        lastToneChar = null
                        text.append(ch)
                    } else if (hasVowel(text)) {
                        currentTone = 4
                        lastToneChar = lower
                    } else {
                        text.append(ch)
                    }
                }
                'j' -> {
                    if (currentTone == 5 && lastToneChar == lower) {
                        currentTone = 0
                        lastToneChar = null
                        text.append(ch)
                    } else if (hasVowel(text)) {
                        currentTone = 5
                        lastToneChar = lower
                    } else {
                        text.append(ch)
                    }
                }
                'z' -> {
                    if (currentTone != 0) {
                        currentTone = 0
                        lastToneChar = null
                    } else {
                        text.append(ch)
                    }
                }
                'a' -> {
                    if (text.isNotEmpty() && text.last().lowercaseChar() == 'a') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'Â' else 'â')
                    } else if (text.isNotEmpty() && text.last().lowercaseChar() == 'â') {
                        // Double undo: â + a -> aa
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'A' else 'a')
                        text.append(ch)
                    } else {
                        text.append(ch)
                    }
                }
                'w' -> {
                    if (text.isNotEmpty()) {
                        val lastIdx = text.length - 1
                        val prevLower = text[lastIdx].lowercaseChar()
                        val prevUpper = text[lastIdx].isUpperCase()

                        if (text.length >= 2 && text.substring(text.length - 2).lowercase() == "uo") {
                            val uUpper = text[text.length - 2].isUpperCase()
                            val oUpper = text[text.length - 1].isUpperCase()
                            text.setCharAt(text.length - 2, if (uUpper) 'Ư' else 'ư')
                            text.setCharAt(text.length - 1, if (oUpper) 'Ơ' else 'ơ')
                        } else if (prevLower == 'a') {
                            text.setCharAt(lastIdx, if (prevUpper) 'Ă' else 'ă')
                        } else if (prevLower == 'o') {
                            text.setCharAt(lastIdx, if (prevUpper) 'Ơ' else 'ơ')
                        } else if (prevLower == 'u') {
                            text.setCharAt(lastIdx, if (prevUpper) 'Ư' else 'ư')
                        } else if (prevLower == 'ă') {
                            text.setCharAt(lastIdx, if (prevUpper) 'A' else 'a')
                            text.append(ch)
                        } else if (prevLower == 'ơ') {
                            text.setCharAt(lastIdx, if (prevUpper) 'O' else 'o')
                            text.append(ch)
                        } else if (prevLower == 'ư') {
                            text.setCharAt(lastIdx, if (prevUpper) 'U' else 'u')
                            text.append(ch)
                        } else {
                            text.append(if (isUpper) 'Ư' else 'ư')
                        }
                    } else {
                        text.append(if (isUpper) 'Ư' else 'ư')
                    }
                }
                'e' -> {
                    if (text.isNotEmpty() && text.last().lowercaseChar() == 'e') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'Ê' else 'ê')
                    } else if (text.isNotEmpty() && text.last().lowercaseChar() == 'ê') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'E' else 'e')
                        text.append(ch)
                    } else {
                        text.append(ch)
                    }
                }
                'o' -> {
                    if (text.isNotEmpty() && text.last().lowercaseChar() == 'o') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'Ô' else 'ô')
                    } else if (text.isNotEmpty() && text.last().lowercaseChar() == 'ô') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'O' else 'o')
                        text.append(ch)
                    } else {
                        text.append(ch)
                    }
                }
                'd' -> {
                    if (text.isNotEmpty() && text.last().lowercaseChar() == 'd') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'Đ' else 'đ')
                    } else if (text.isNotEmpty() && text.last().lowercaseChar() == 'đ') {
                        val lastUpper = text.last().isUpperCase()
                        text.setCharAt(text.length - 1, if (lastUpper) 'D' else 'd')
                        text.append(ch)
                    } else {
                        text.append(ch)
                    }
                }
                else -> {
                    text.append(ch)
                }
            }
        }

        var result = text.toString()
        if (currentTone > 0) {
            result = applyToneToComposedText(result, currentTone, tonePlacement)
        }
        return result
    }

    private fun hasVowel(sb: CharSequence): Boolean {
        for (i in 0 until sb.length) {
            if (VietnameseCharUtils.isVowel(sb[i])) return true
        }
        return false
    }

    private fun applyToneToComposedText(
        text: String,
        tone: Int,
        placement: TonePlacement
    ): String {
        var firstVowel = -1
        var lastVowel = -1

        for (i in text.indices) {
            if (VietnameseCharUtils.isVowel(text[i])) {
                if (firstVowel == -1) firstVowel = i
                lastVowel = i
            }
        }

        if (firstVowel == -1) return text

        val vowelCluster = text.substring(firstVowel, lastVowel + 1)
        val hasEndingConsonant = lastVowel < text.length - 1

        val relativeToneIndex = VietnameseCharUtils.findToneVowelIndex(
            vowelCluster,
            hasEndingConsonant,
            placement
        )

        val targetCharIndex = firstVowel + relativeToneIndex
        if (targetCharIndex !in text.indices) return text

        val targetChar = text[targetCharIndex]
        val tonedChar = VietnameseCharUtils.applyTone(targetChar, tone)

        val sb = StringBuilder(text)
        sb.setCharAt(targetCharIndex, tonedChar)
        return sb.toString()
    }
}
