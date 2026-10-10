package com.example.game

import com.example.model.Cell
import com.example.model.GameStatus
import com.example.model.Piece

object GameEngine {

    fun createEmptyBoard(size: Int): Array<Array<Piece>> {
        return Array(size) { Array(size) { Piece.NONE } }
    }

    fun cloneBoard(board: Array<Array<Piece>>): Array<Array<Piece>> {
        val size = board.size
        return Array(size) { r -> Array(size) { c -> board[r][c] } }
    }

    fun checkWin(board: Array<Array<Piece>>, winLength: Int): GameStatus.Won? {
        val size = board.size
        val foundLines = mutableListOf<List<Cell>>()
        var winnerPiece = Piece.NONE

        // 1. Horizontal check
        for (r in 0 until size) {
            for (c in 0..(size - winLength)) {
                val first = board[r][c]
                if (first != Piece.NONE) {
                    var match = true
                    for (i in 1 until winLength) {
                        if (board[r][c + i] != first) {
                            match = false
                            break
                        }
                    }
                    if (match) {
                        foundLines.add((0 until winLength).map { Cell(r, c + it) })
                        winnerPiece = first
                    }
                }
            }
        }

        // 2. Vertical check
        for (r in 0..(size - winLength)) {
            for (c in 0 until size) {
                val first = board[r][c]
                if (first != Piece.NONE) {
                    var match = true
                    for (i in 1 until winLength) {
                        if (board[r + i][c] != first) {
                            match = false
                            break
                        }
                    }
                    if (match) {
                        foundLines.add((0 until winLength).map { Cell(r + it, c) })
                        winnerPiece = first
                    }
                }
            }
        }

        // 3. Diagonal Down-Right (\)
        for (r in 0..(size - winLength)) {
            for (c in 0..(size - winLength)) {
                val first = board[r][c]
                if (first != Piece.NONE) {
                    var match = true
                    for (i in 1 until winLength) {
                        if (board[r + i][c + i] != first) {
                            match = false
                            break
                        }
                    }
                    if (match) {
                        foundLines.add((0 until winLength).map { Cell(r + it, c + it) })
                        winnerPiece = first
                    }
                }
            }
        }

        // 4. Diagonal Down-Left (/)
        for (r in 0..(size - winLength)) {
            for (c in (winLength - 1) until size) {
                val first = board[r][c]
                if (first != Piece.NONE) {
                    var match = true
                    for (i in 1 until winLength) {
                        if (board[r + i][c - i] != first) {
                            match = false
                            break
                        }
                    }
                    if (match) {
                        foundLines.add((0 until winLength).map { Cell(r + it, c - it) })
                        winnerPiece = first
                    }
                }
            }
        }

        if (foundLines.isNotEmpty()) {
            return GameStatus.Won(winnerPiece, foundLines)
        }
        return null
    }

    fun isDraw(board: Array<Array<Piece>>): Boolean {
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c] == Piece.NONE) return false
            }
        }
        return true
    }

    fun getValidMoves(board: Array<Array<Piece>>): List<Cell> {
        val moves = mutableListOf<Cell>()
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c] == Piece.NONE) {
                    moves.add(Cell(r, c))
                }
            }
        }
        return moves
    }
}
