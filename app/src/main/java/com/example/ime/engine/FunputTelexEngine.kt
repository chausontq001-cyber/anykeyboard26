package com.example.ime.engine

import app.funput.funput.ime.nativebridge.FunputNative
import com.example.ime.model.TonePlacement

internal class FunputTelexEngine : TelexEngine {
    private var handle = FunputNative.nativeCreate().also {
        check(it != InvalidHandle) { "Unable to create Funput native engine" }
    }

    override fun configure(tonePlacement: TonePlacement) = withHandle { value ->
        FunputNative.nativeConfigure(
            value,
            TelexMethod,
            if (tonePlacement == TonePlacement.MODERN) ModernToneStyle else TraditionalToneStyle,
            smartRestore = false,
            eagerRestore = false,
            spellCheck = false,
            autoCapitalize = false
        )
        FunputNative.nativeSetExtraOnsets(value, NoExtraOnsets)
        FunputNative.nativeSetEnabled(value, true)
    }

    override fun process(codePoint: Int): String =
        withHandle { FunputNative.nativeProcess(it, codePoint) }

    override fun backspace(): String =
        withHandle(FunputNative::nativeBackspace)

    override fun clear() = withHandle(FunputNative::nativeClear)

    override fun close() {
        if (handle == InvalidHandle) return
        FunputNative.nativeDestroy(handle)
        handle = InvalidHandle
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
