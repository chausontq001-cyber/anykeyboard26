package com.example.ime.engine

import com.example.ime.model.TonePlacement

interface TelexEngine : AutoCloseable {
    var tonePlacement: TonePlacement
    val composingText: String
    val hasInput: Boolean
        get() = composingText.isNotEmpty()

    fun configure(tonePlacement: TonePlacement)
    fun process(codePoint: Int): String
    fun processKey(key: String): String {
        var result = composingText
        key.forEach { result = process(it.code) }
        return result
    }
    fun backspace(): String
    fun clear()
    fun reset() = clear()
}
