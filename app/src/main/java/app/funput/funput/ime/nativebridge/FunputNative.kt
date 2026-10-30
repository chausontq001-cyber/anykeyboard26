package app.funput.funput.ime.nativebridge

internal object FunputNative {
    init {
        System.loadLibrary("funput_jni")
    }

    external fun nativeCreate(): Long
    external fun nativeDestroy(handle: Long)
    external fun nativeClear(handle: Long)
    external fun nativeConfigure(
        handle: Long,
        method: Int,
        toneStyle: Int,
        smartRestore: Boolean,
        eagerRestore: Boolean,
        spellCheck: Boolean,
        autoCapitalize: Boolean
    )
    external fun nativeSetExtraOnsets(handle: Long, letters: Int)
    external fun nativeSetEnabled(handle: Long, enabled: Boolean)
    external fun nativeProcess(handle: Long, codePoint: Int): String
    external fun nativeBackspace(handle: Long): String
}
