package com.example.ime.engine

import com.example.ime.model.TonePlacement

open class TelexEngine : AutoCloseable {
    private val buffer = StringBuilder()
    private val history = mutableListOf<State>()
    private var lastAction: Action? = null
    var isReverted: Boolean = false
        private set

    open var tonePlacement: TonePlacement = TonePlacement.MODERN

    val composingText: String
        get() = buffer.toString()

    val hasInput: Boolean
        get() = buffer.isNotEmpty()

    open fun configure(tonePlacement: TonePlacement) {
        this.tonePlacement = tonePlacement
    }

    open fun process(codePoint: Int): String =
        processKey(String(Character.toChars(codePoint)))

    open fun processKey(key: String): String {
        for (char in key) {
            history += snapshot()
            processCharacter(char)
        }
        return buffer.toString()
    }

    open fun backspace(): String {
        if (history.isNotEmpty()) {
            restore(history.removeAt(history.lastIndex))
        }
        return buffer.toString()
    }

    open fun clear() = reset()

    open fun reset() {
        buffer.clear()
        history.clear()
        lastAction = null
        isReverted = false
    }

    open override fun close() = reset()

    private fun processCharacter(char: Char) {
        val lower = char.lowercaseChar()
        val action = lastAction
        if (!isReverted && action != null && action.key == lower) {
            buffer.setLength(0)
            buffer.append(action.previousText)
            buffer.append(char)
            lastAction = null
            isReverted = true
            return
        }

        if (isReverted) {
            buffer.append(char)
            return
        }

        lastAction = null
        when (lower) {
            's' -> applyTone(char, 1)
            'f' -> applyTone(char, 2)
            'r' -> applyTone(char, 3)
            'x' -> applyTone(char, 4)
            'j' -> applyTone(char, 5)
            'a', 'e', 'o', 'w' -> applyVowelModifier(char, lower)
            'd' -> applyDModifier(char)
            else -> buffer.append(char)
        }
    }

    private fun applyVowelModifier(key: Char, modifier: Char) {
        val indices = lastVowelRun()
        val bases = indices.map { baseVowel(buffer[it]).lowercaseChar() }.joinToString("")

        if (modifier == 'w' && bases.endsWith("uo") && indices.size >= 2) {
            val uIndex = indices[indices.lastIndex - 1]
            val oIndex = indices.last()
            val previousText = buffer.toString()
            buffer.setCharAt(uIndex, changeShape(buffer[uIndex], 'ư'))
            buffer.setCharAt(oIndex, changeShape(buffer[oIndex], 'ơ'))
            lastAction = Action(modifier, previousText)
            return
        }

        val targetIndex = when {
            modifier == 'a' && bases.endsWith("ie") -> indices.last()
            modifier == 'o' && bases.endsWith("uo") -> indices.last()
            else -> indices.asReversed().firstOrNull { index ->
                val base = baseVowel(buffer[index]).lowercaseChar()
                when (modifier) {
                    'a' -> base == 'a'
                    'e' -> base == 'e'
                    'o' -> base == 'o'
                    'w' -> base == 'a' || base == 'o' || base == 'u'
                    else -> false
                }
            }
        }

        if (targetIndex == null) {
            buffer.append(key)
            return
        }

        val target = buffer[targetIndex]
        val shape = when (modifier) {
            'a' -> if (bases.endsWith("ie")) 'ê' else 'â'
            'e' -> 'ê'
            'o' -> 'ô'
            'w' -> when (baseVowel(target).lowercaseChar()) {
                'a' -> 'ă'
                'o' -> 'ơ'
                'u' -> 'ư'
                else -> {
                    buffer.append(key)
                    return
                }
            }
            else -> return
        }
        val previousText = buffer.toString()
        buffer.setCharAt(targetIndex, changeShape(target, shape))
        lastAction = Action(modifier, previousText)
    }

    private fun applyDModifier(key: Char) {
        if (buffer.isNotEmpty() && buffer.last().lowercaseChar() == 'd') {
            val previousText = buffer.toString()
            val last = buffer.last()
            buffer.setCharAt(buffer.lastIndex, if (last.isUpperCase()) 'Đ' else 'đ')
            lastAction = Action('d', previousText)
        } else {
            buffer.append(key)
        }
    }

    private fun applyTone(key: Char, tone: Int) {
        val vowelIndices = buffer.indices.filter { VietnameseCharUtils.isVowel(buffer[it]) }
        if (vowelIndices.isEmpty()) {
            buffer.append(key)
            return
        }

        val bases = vowelIndices.map {
            baseVowel(buffer[it]).lowercaseChar()
        }.joinToString("")
        val targetIndex = when {
            vowelIndices.size == 1 -> vowelIndices.first()
            vowelIndices.size == 3 -> vowelIndices[1]
            vowelIndices.size == 2 && (
                bases in setOf("oa", "oe", "uy", "ươ") ||
                    vowelIndices.any {
                        VietnameseCharUtils.removeTone(buffer[it]).lowercaseChar() in "âêôơư"
                    }
                ) -> vowelIndices.last()
            else -> vowelIndices.first()
        }

        val previousText = buffer.toString()
        buffer.setCharAt(
            targetIndex,
            VietnameseCharUtils.applyTone(buffer[targetIndex], tone)
        )
        lastAction = Action(key.lowercaseChar(), previousText)
    }

    private fun lastVowelRun(): List<Int> {
        var index = buffer.lastIndex
        while (index >= 0 && !VietnameseCharUtils.isVowel(buffer[index])) index--
        if (index < 0) return emptyList()

        val end = index
        while (index >= 0 && VietnameseCharUtils.isVowel(buffer[index])) index--
        return (index + 1..end).toList()
    }

    private fun baseVowel(char: Char): Char =
        VietnameseCharUtils.getBaseVowelAndTone(char)?.first ?: char

    private fun changeShape(char: Char, shape: Char): Char {
        val info = VietnameseCharUtils.getBaseVowelAndTone(char)
        val shaped = if (char.isUpperCase()) shape.uppercaseChar() else shape
        return VietnameseCharUtils.applyTone(shaped, info?.second ?: 0)
    }

    private fun snapshot() = State(buffer.toString(), lastAction, isReverted)

    private fun restore(state: State) {
        val reverted = isReverted || state.isReverted
        buffer.setLength(0)
        buffer.append(state.text)
        lastAction = state.lastAction
        isReverted = reverted
    }

    private data class Action(val key: Char, val previousText: String)
    private data class State(
        val text: String,
        val lastAction: Action?,
        val isReverted: Boolean
    )
}
