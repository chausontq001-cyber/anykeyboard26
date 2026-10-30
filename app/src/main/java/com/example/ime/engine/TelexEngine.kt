package com.example.ime.engine

import com.example.ime.model.TonePlacement

interface TelexEngine : AutoCloseable {
    fun configure(tonePlacement: TonePlacement)
    fun process(codePoint: Int): String
    fun backspace(): String
    fun clear()
    override fun close()
}
