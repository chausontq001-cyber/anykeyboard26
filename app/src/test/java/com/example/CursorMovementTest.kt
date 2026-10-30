package com.example

import com.example.ime.CursorMovement
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorMovementTest {
    @Test
    fun movesBetweenLinesAndPreservesPreferredColumn() {
        val text = "abcde\nxy\n12345"

        assertEquals(7, CursorMovement.move(text, 1, 0, 1))
        assertEquals(3, CursorMovement.move(text, 12, 0, -2))
    }

    @Test
    fun horizontalMovementRemainsIndependentFromVerticalMovement() {
        val text = "abcde\n12345"

        assertEquals(8, CursorMovement.move(text, 1, 1, 1))
        assertEquals(1, CursorMovement.move(text, 8, -1, -1))
    }
}
