package com.example.ime.engine

import app.funput.funput.ime.nativebridge.FunputNative
import com.example.ime.model.TonePlacement

internal class FunputTelexEngine : TelexEngine {
    private var handle = FunputNative.nativeCreate().also {
        check(it != InvalidHandle) { "Unable to create Funput native engine" }
    }

    override var tonePlacement: TonePlacement = TonePlacement.MODERN
        private set
    override var composingText: String = ""
        private set

    override fun configure(tonePlacement: TonePlacement) = withHandle { value ->
        // Funput names the first-vowel style "Traditional" and the second-vowel style "Modern".
        FunputNative.nativeConfigure(
            value,
            TelexMethod,
            if (tonePlacement == TonePlacement.MODERN) TraditionalToneStyle else ModernToneStyle,
            smartRestore = false,
            eagerRestore = false,
            spellCheck = false,
            autoCapitalize = false
        )
        FunputNative.nativeSetExtraOnsets(value, NoExtraOnsets)
        FunputNative.nativeSetEnabled(value, true)
        this.tonePlacement = tonePlacement
    }

    override fun process(codePoint: Int): String =
        withHandle { value ->
            FunputNative.nativeProcess(value, codePoint).also { composingText = it }
        }

    override fun processKey(key: String): String {
        var result = composingText
        key.forEach { result = process(it.code) }
        return result
    }

    override fun backspace(): String =
        withHandle { value ->
            FunputNative.nativeBackspace(value).also { composingText = it }
        }

    override fun clear() = withHandle { value ->
        FunputNative.nativeClear(value)
        composingText = ""
    }

    override fun reset() = clear()

    override fun close() {
        if (handle == InvalidHandle) return
        FunputNative.nativeDestroy(handle)
        handle = InvalidHandle
        composingText = ""
    }

    private inline fun <T> withHandle(operation: (Long) -> T): T {
        check(handle != InvalidHandle) { "Funput native engine is closed" }
        return operation(handle)
    }

    private companion object {
        const val InvalidHandle = 0L
        const val TelexMethod = 0
        const val TraditionalToneStyle = 0
        const val ModernToneStyle = 1
        const val NoExtraOnsets = 0
    }
}
