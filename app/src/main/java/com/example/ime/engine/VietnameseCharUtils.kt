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

    fun toTelexSequence(text: String): String = buildString {
        for (char in text) {
            val toneInfo = getBaseVowelAndTone(char)
            val baseChar = toneInfo?.first ?: char
            val lower = baseChar.lowercaseChar()
            val spelling = when (lower) {
                'â' -> "aa"
                'ê' -> "ee"
                'ô' -> "oo"
                'đ' -> "dd"
                else -> baseChar.toString()
            }
            val isUpper = char.isUpperCase()
            append(if (isUpper) spelling.uppercase() else spelling)
            append(
                when (toneInfo?.second) {
                    1 -> if (isUpper) 'S' else 's'
                    2 -> if (isUpper) 'F' else 'f'
                    3 -> if (isUpper) 'R' else 'r'
                    4 -> if (isUpper) 'X' else 'x'
                    5 -> if (isUpper) 'J' else 'j'
                    else -> ""
                }
            )
        }
    }

    /**
     * Finds the character index that receives the tone for the current syllable.
     * MODERN follows the displayed app rule (hóa, hòa, thúy, khỏe); CLASSIC uses
     * the alternate open-pair spelling (hoá, hoà, thuý, khoẻ).
     */
    fun findToneVowelIndex(text: String, tonePlacement: TonePlacement): Int {
        val vowelIndices = text.indices.filter { isVowel(text[it]) }
        if (vowelIndices.isEmpty()) return -1

        val firstVowelIndex = vowelIndices.first()
        val startsWithGlideOnset = vowelIndices.size > 1 &&
            firstVowelIndex == 1 &&
            (text.startsWith("qu", ignoreCase = true) ||
                text.startsWith("gi", ignoreCase = true))
        val clusterIndices = if (startsWithGlideOnset) {
            vowelIndices.drop(1)
        } else {
            vowelIndices
        }
        val cluster = clusterIndices.joinToString("") {
            removeTone(text[it]).lowercaseChar().toString()
        }
        val hasEndingConsonant = clusterIndices.last() < text.lastIndex
        val specializedIndex = cluster.indexOfFirst { it in "ăâêôơư" }
        val relativeIndex = when {
            specializedIndex >= 0 && cluster.contains("ươ") -> cluster.indexOf('ơ')
            specializedIndex >= 0 -> specializedIndex
            cluster.length == 2 &&
                (cluster == "oa" || cluster == "oe" || cluster == "uy") -> when {
                hasEndingConsonant -> 1
                tonePlacement == TonePlacement.MODERN -> 0
                else -> 1
            }
            cluster.length == 2 &&
                (cluster == "ia" || cluster == "ya" ||
                    cluster == "ua" || cluster == "ưa") -> 0
            cluster.length == 3 -> 1
            hasEndingConsonant && cluster.length >= 2 -> 1
            else -> 0
        }
        return clusterIndices[relativeIndex.coerceIn(clusterIndices.indices)]
    }
}
