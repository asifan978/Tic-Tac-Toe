package com.example

import com.example.game.AIEngine
import com.example.game.GameEngine
import com.example.model.AIDifficulty
import com.example.model.BoardSize
import com.example.model.Cell
import com.example.model.GameStatus
import com.example.model.Piece
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun test3x3HorizontalWin() {
        val board = GameEngine.createEmptyBoard(3)
        board[0][0] = Piece.X
        board[0][1] = Piece.X
        board[0][2] = Piece.X

        val win = GameEngine.checkWin(board, winLength = 3)
        assertNotNull(win)
        assertEquals(Piece.X, (win as GameStatus.Won).winner)
        assertEquals(3, win.winningCells.size)
        assertEquals(listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2)), win.winningLines.first())
    }

    @Test
    fun test6x6FourInARowWin() {
        val board = GameEngine.createEmptyBoard(6)
        board[1][1] = Piece.O
        board[2][2] = Piece.O
        board[3][3] = Piece.O
        board[4][4] = Piece.O

        val win = GameEngine.checkWin(board, winLength = 4)
        assertNotNull(win)
        assertEquals(Piece.O, (win as GameStatus.Won).winner)
        assertEquals(4, win.winningCells.size)
        assertEquals(listOf(Cell(1, 1), Cell(2, 2), Cell(3, 3), Cell(4, 4)), win.winningLines.first())
    }

    @Test
    fun test9x9FiveInARowWin() {
        val board = GameEngine.createEmptyBoard(9)
        board[2][6] = Piece.X
        board[3][5] = Piece.X
        board[4][4] = Piece.X
        board[5][3] = Piece.X
        board[6][2] = Piece.X

        val win = GameEngine.checkWin(board, winLength = 5)
        assertNotNull(win)
        assertEquals(Piece.X, (win as GameStatus.Won).winner)
        assertEquals(5, win.winningCells.size)
        assertEquals(listOf(Cell(2, 6), Cell(3, 5), Cell(4, 4), Cell(5, 3), Cell(6, 2)), win.winningLines.first())
    }

    @Test
    fun testDrawDetection() {
        val board = GameEngine.createEmptyBoard(3)
        val moves = listOf(
            listOf(Piece.X, Piece.O, Piece.X),
            listOf(Piece.X, Piece.O, Piece.O),
            listOf(Piece.O, Piece.X, Piece.X)
        )
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                board[r][c] = moves[r][c]
            }
        }
        assertTrue(GameEngine.isDraw(board))
        assertEquals(null, GameEngine.checkWin(board, 3))
    }

    @Test
    fun testAiBlocksImmediateWin() {
        val board = GameEngine.createEmptyBoard(3)
        // Human X is about to win at (0, 2)
        board[0][0] = Piece.X
        board[0][1] = Piece.X
        board[1][0] = Piece.O

        val aiMove = AIEngine.getBestMove(board, Piece.O, winLength = 3, AIDifficulty.HARD)
        assertNotNull(aiMove)
        assertEquals(0, aiMove!!.row)
        assertEquals(2, aiMove.col)
    }
}
