package com.example.ime

internal object CursorMovement {
    fun move(
        text: CharSequence,
        currentPosition: Int,
        horizontalSteps: Int,
        verticalSteps: Int
    ): Int {
        var position = currentPosition.coerceIn(0, text.length)

        repeat(kotlin.math.abs(horizontalSteps)) {
            position = (position + if (horizontalSteps > 0) 1 else -1)
                .coerceIn(0, text.length)
        }

        val preferredColumn =
            text.lastIndexOf('\n', position - 1).let { position - (it + 1) }
        repeat(kotlin.math.abs(verticalSteps)) {
            val lineStart = text.lastIndexOf('\n', position - 1) + 1
            val lineEnd = text.indexOf('\n', position).let {
                if (it < 0) text.length else it
            }

            position = if (verticalSteps < 0) {
                if (lineStart == 0) {
                    position
                } else {
                    val targetEnd = lineStart - 1
                    val targetStart = text.lastIndexOf('\n', targetEnd - 1) + 1
                    (targetStart + preferredColumn).coerceAtMost(targetEnd)
                }
            } else {
                if (lineEnd == text.length) {
                    position
                } else {
                    val targetStart = lineEnd + 1
                    val targetEnd = text.indexOf('\n', targetStart).let {
                        if (it < 0) text.length else it
                    }
                    (targetStart + preferredColumn).coerceAtMost(targetEnd)
                }
            }
        }

        return position
    }
}
