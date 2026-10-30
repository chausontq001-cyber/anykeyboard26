package com.example.ime.engine

import com.example.ime.model.TonePlacement

object VietnameseCharUtils {

    // Tone indices: 0 = none, 1 = acute (sắc), 2 = grave (huyền), 3 = hook (hỏi), 4 = tilde (ngã), 5 = dot (nặng)
    private val VOWEL_TABLE: Map<Char, List<Char>> = mapOf(
        'a' to listOf('a', 'á', 'à', 'ả', 'ã', 'ạ'),
        'ă' to listOf('ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ'),
        'â' to listOf('â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'),
        'e' to listOf('e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ'),
        'ê' to listOf('ê', 'ế', 'ề', 'ể', 'ễ', 'ệ'),
        'i' to listOf('i', 'í', 'ì', 'ỉ', 'ĩ', 'ị'),
        'o' to listOf('o', 'ó', 'ò', 'ỏ', 'õ', 'ọ'),
        'ô' to listOf('ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ'),
        'ơ' to listOf('ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ'),
        'u' to listOf('u', 'ú', 'ù', 'ủ', 'ũ', 'ụ'),
        'ư' to listOf('ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự'),
        'y' to listOf('y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ')
    )

    private val REVERSE_VOWEL_MAP: Map<Char, Pair<Char, Int>> = buildMap {
        for ((baseVowel, variants) in VOWEL_TABLE) {
            variants.forEachIndexed { toneIndex, char ->
                put(char, Pair(baseVowel, toneIndex))
                put(char.uppercaseChar(), Pair(baseVowel.uppercaseChar(), toneIndex))
            }
        }
    }

    fun isVowel(c: Char): Boolean {
        val lower = c.lowercaseChar()
        return REVERSE_VOWEL_MAP.containsKey(lower)
    }

    fun getBaseVowelAndTone(c: Char): Pair<Char, Int>? {
        return REVERSE_VOWEL_MAP[c]
    }

    fun applyTone(vowel: Char, toneIndex: Int): Char {
        val isUpper = vowel.isUpperCase()
        val lower = vowel.lowercaseChar()
        val base = REVERSE_VOWEL_MAP[lower]?.first ?: lower
        val variants = VOWEL_TABLE[base] ?: return vowel
        val boundedIndex = toneIndex.coerceIn(0, 5)
        val result = variants[boundedIndex]
        return if (isUpper) result.uppercaseChar() else result
    }

    fun removeTone(c: Char): Char {
        val info = REVERSE_VOWEL_MAP[c] ?: return c
        return if (c.isUpperCase()) info.first.uppercaseChar() else info.first
    }

    /**
     * Determines which vowel index in a vowel cluster should receive the tone mark.
     * Supports both modern (hóa, thúy) and classic (hoá, thuý) rules.
     */
    fun findToneVowelIndex(
        vowelCluster: String,
        hasEndingConsonant: Boolean,
        tonePlacement: TonePlacement
    ): Int {
        val lower = vowelCluster.lowercase()
        val len = lower.length
        if (len == 0) return 0
        if (len == 1) return 0

        // If cluster contains circumflex (ê, ô, â) or horn (ơ, ư), tone usually goes to it
        val specializedIndex = lower.indexOfFirst { it in "êôâơư" }
        if (specializedIndex != -1) {
            // For ươ, tone goes to ơ
            if (lower.contains("ươ")) {
                return lower.indexOf("ơ")
            }
            return specializedIndex
        }

        // For dipthongs: oa, oe, uy
        if (lower.startsWith("oa") || lower.startsWith("oe") || lower.startsWith("uy")) {
            return if (hasEndingConsonant) {
                // If there's an ending consonant (hoàng, quyến, khoán), tone is on the second vowel
                1
            } else {
                // No ending consonant (hóa vs hoá)
                if (tonePlacement == TonePlacement.MODERN) 1 else 0
            }
        }

        // For dipthongs ia, ya, ua, ưa: tone is on the first vowel (mía, múa, mứa)
        if (lower.startsWith("ia") || lower.startsWith("ya") || lower.startsWith("ua") || lower.startsWith("ưa")) {
            return 0
        }

        // For tripthongs: iêu, oai, uôi, uya, yêu, etc.
        if (len == 3) {
            return 1 // Center vowel
        }

        // Default: if ending consonant exists, place on 2nd vowel if 2 vowels; else 1st
        return if (hasEndingConsonant && len >= 2) 1 else 0
    }
}
