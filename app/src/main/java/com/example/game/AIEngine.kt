package com.example.game

import com.example.model.AIDifficulty
import com.example.model.Cell
import com.example.model.Piece
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object AIEngine {

    fun getBestMove(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        winLength: Int,
        difficulty: AIDifficulty
    ): Cell? {
        val validMoves = GameEngine.getValidMoves(board)
        if (validMoves.isEmpty()) return null

        val humanPiece = aiPiece.opponent

        return when (difficulty) {
            AIDifficulty.EASY -> getEasyMove(board, aiPiece, humanPiece, winLength, validMoves)
            AIDifficulty.MEDIUM -> getMediumMove(board, aiPiece, humanPiece, winLength, validMoves)
            AIDifficulty.HARD -> getHardMove(board, aiPiece, humanPiece, winLength, validMoves)
        }
    }

    private fun getEasyMove(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        humanPiece: Piece,
        winLength: Int,
        validMoves: List<Cell>
    ): Cell {
        // Can AI win right now?
        for (move in validMoves) {
            board[move.row][move.col] = aiPiece
            val win = GameEngine.checkWin(board, winLength)
            board[move.row][move.col] = Piece.NONE
            if (win != null) return move
        }

        // 30% chance to block immediate opponent win
        if (Random.nextFloat() < 0.30f) {
            for (move in validMoves) {
                board[move.row][move.col] = humanPiece
                val oppWin = GameEngine.checkWin(board, winLength)
                board[move.row][move.col] = Piece.NONE
                if (oppWin != null) return move
            }
        }

        // Prefer moves near center or existing pieces
        val candidates = getCandidateMoves(board, validMoves, maxDistance = 2)
        return candidates.randomOrNull() ?: validMoves.random()
    }

    private fun getMediumMove(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        humanPiece: Piece,
        winLength: Int,
        validMoves: List<Cell>
    ): Cell {
        val size = board.size
        // 1. Immediate win check
        for (move in validMoves) {
            board[move.row][move.col] = aiPiece
            val win = GameEngine.checkWin(board, winLength)
            board[move.row][move.col] = Piece.NONE
            if (win != null) return move
        }

        // 2. Immediate block check
        for (move in validMoves) {
            board[move.row][move.col] = humanPiece
            val oppWin = GameEngine.checkWin(board, winLength)
            board[move.row][move.col] = Piece.NONE
            if (oppWin != null) return move
        }

        if (size == 3) {
            return minimaxBestMove(board, aiPiece, winLength, maxDepth = 4)
        }

        // For larger boards: depth 2 heuristic minimax
        val candidates = getCandidateMoves(board, validMoves, maxDistance = 2).take(12)
        var bestScore = Int.MIN_VALUE
        var bestMove = candidates.firstOrNull() ?: validMoves.random()

        for (move in candidates) {
            board[move.row][move.col] = aiPiece
            val score = minimaxScore(board, aiPiece, humanPiece, winLength, depth = 1, isMaximizing = false, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE, maxDepth = 2)
            board[move.row][move.col] = Piece.NONE

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun getHardMove(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        humanPiece: Piece,
        winLength: Int,
        validMoves: List<Cell>
    ): Cell {
        val size = board.size

        // 1. Check immediate win
        for (move in validMoves) {
            board[move.row][move.col] = aiPiece
            val win = GameEngine.checkWin(board, winLength)
            board[move.row][move.col] = Piece.NONE
            if (win != null) return move
        }

        // 2. Check immediate block
        for (move in validMoves) {
            board[move.row][move.col] = humanPiece
            val oppWin = GameEngine.checkWin(board, winLength)
            board[move.row][move.col] = Piece.NONE
            if (oppWin != null) return move
        }

        if (size == 3) {
            return minimaxBestMove(board, aiPiece, winLength, maxDepth = 9)
        }

        // For 6x6 and 9x9: Candidate moves near pieces, sorted by quick evaluation
        val candidates = getCandidateMoves(board, validMoves, maxDistance = 2)
        val sortedCandidates = candidates.sortedByDescending { move ->
            evaluateCandidatePriority(board, move, aiPiece, humanPiece, winLength)
        }.take(10)

        var bestScore = Int.MIN_VALUE
        var bestMove = sortedCandidates.firstOrNull() ?: validMoves.random()

        val searchDepth = if (size == 6) 3 else 2

        for (move in sortedCandidates) {
            board[move.row][move.col] = aiPiece
            val score = minimaxScore(board, aiPiece, humanPiece, winLength, depth = 1, isMaximizing = false, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE, maxDepth = searchDepth)
            board[move.row][move.col] = Piece.NONE

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun minimaxBestMove(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        winLength: Int,
        maxDepth: Int
    ): Cell {
        val validMoves = GameEngine.getValidMoves(board)
        var bestScore = Int.MIN_VALUE
        var bestMove = validMoves.first()

        for (move in validMoves) {
            board[move.row][move.col] = aiPiece
            val score = minimaxScore(board, aiPiece, aiPiece.opponent, winLength, depth = 1, isMaximizing = false, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE, maxDepth = maxDepth)
            board[move.row][move.col] = Piece.NONE

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun minimaxScore(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        humanPiece: Piece,
        winLength: Int,
        depth: Int,
        isMaximizing: Boolean,
        alpha: Int,
        beta: Int,
        maxDepth: Int
    ): Int {
        val win = GameEngine.checkWin(board, winLength)
        if (win != null) {
            return if (win.winner == aiPiece) 100000 - depth else -100000 + depth
        }
        if (GameEngine.isDraw(board) || depth >= maxDepth) {
            return evaluateBoard(board, aiPiece, humanPiece, winLength)
        }

        var curAlpha = alpha
        var curBeta = beta
        val moves = getCandidateMoves(board, GameEngine.getValidMoves(board), maxDistance = 2).take(8)

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in moves) {
                board[move.row][move.col] = aiPiece
                val evaluation = minimaxScore(board, aiPiece, humanPiece, winLength, depth + 1, false, curAlpha, curBeta, maxDepth)
                board[move.row][move.col] = Piece.NONE
                maxEval = max(maxEval, evaluation)
                curAlpha = max(curAlpha, evaluation)
                if (curBeta <= curAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in moves) {
                board[move.row][move.col] = humanPiece
                val evaluation = minimaxScore(board, aiPiece, humanPiece, winLength, depth + 1, true, curAlpha, curBeta, maxDepth)
                board[move.row][move.col] = Piece.NONE
                minEval = min(minEval, evaluation)
                curBeta = min(curBeta, evaluation)
                if (curBeta <= curAlpha) break
            }
            return minEval
        }
    }

    private fun getCandidateMoves(
        board: Array<Array<Piece>>,
        validMoves: List<Cell>,
        maxDistance: Int
    ): List<Cell> {
        val size = board.size
        val hasPieces = board.any { row -> row.any { it != Piece.NONE } }
        if (!hasPieces) {
            val center = Cell(size / 2, size / 2)
            return if (validMoves.contains(center)) listOf(center) else validMoves
        }

        val candidates = validMoves.filter { move ->
            isNearExistingPiece(board, move, maxDistance)
        }

        return if (candidates.isNotEmpty()) candidates else validMoves
    }

    private fun isNearExistingPiece(board: Array<Array<Piece>>, cell: Cell, maxDistance: Int): Boolean {
        val size = board.size
        val minR = max(0, cell.row - maxDistance)
        val maxR = min(size - 1, cell.row + maxDistance)
        val minC = max(0, cell.col - maxDistance)
        val maxC = min(size - 1, cell.col + maxDistance)

        for (r in minR..maxR) {
            for (c in minC..maxC) {
                if (board[r][c] != Piece.NONE) return true
            }
        }
        return false
    }

    private fun evaluateCandidatePriority(
        board: Array<Array<Piece>>,
        cell: Cell,
        aiPiece: Piece,
        humanPiece: Piece,
        winLength: Int
    ): Int {
        var score = 0
        board[cell.row][cell.col] = aiPiece
        score += evaluateBoard(board, aiPiece, humanPiece, winLength)
        board[cell.row][cell.col] = humanPiece
        score -= evaluateBoard(board, aiPiece, humanPiece, winLength)
        board[cell.row][cell.col] = Piece.NONE
        return score
    }

    private fun evaluateBoard(
        board: Array<Array<Piece>>,
        aiPiece: Piece,
        humanPiece: Piece,
        winLength: Int
    ): Int {
        val size = board.size
        var totalScore = 0

        fun evaluateLine(cells: List<Piece>) {
            var aiCount = 0
            var oppCount = 0
            for (p in cells) {
                if (p == aiPiece) aiCount++
                else if (p == humanPiece) oppCount++
            }
            if (aiCount > 0 && oppCount > 0) return // blocked line
            if (aiCount > 0) {
                totalScore += when (aiCount) {
                    winLength -> 50000
                    winLength - 1 -> 800
                    winLength - 2 -> 60
                    else -> 5
                }
            } else if (oppCount > 0) {
                totalScore -= when (oppCount) {
                    winLength -> 50000
                    winLength - 1 -> 950
                    winLength - 2 -> 70
                    else -> 5
                }
            }
        }

        // Rows
        for (r in 0 until size) {
            for (c in 0..(size - winLength)) {
                evaluateLine((0 until winLength).map { board[r][c + it] })
            }
        }
        // Columns
        for (r in 0..(size - winLength)) {
            for (c in 0 until size) {
                evaluateLine((0 until winLength).map { board[r + it][c] })
            }
        }
        // Diagonal Down-Right
        for (r in 0..(size - winLength)) {
            for (c in 0..(size - winLength)) {
                evaluateLine((0 until winLength).map { board[r + it][c + it] })
            }
        }
        // Diagonal Down-Left
        for (r in 0..(size - winLength)) {
            for (c in (winLength - 1) until size) {
                evaluateLine((0 until winLength).map { board[r + it][c - it] })
            }
        }

        return totalScore
    }
}
