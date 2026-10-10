package com.example.model

enum class Piece {
    NONE, X, O;

    val opponent: Piece
        get() = when (this) {
            X -> O
            O -> X
            NONE -> NONE
        }
}

data class Cell(val row: Int, val col: Int)

enum class BoardSize(
    val size: Int,
    val winLength: Int,
    val title: String,
    val ruleLabel: String,
    val description: String
) {
    THREE_BY_THREE(3, 3, "3×3 Classic", "3 in a row to win", "Quick, classic & cozy"),
    SIX_BY_SIX(6, 4, "6×6 Grid", "4 in a row to win", "Strategic & exciting"),
    NINE_BY_NINE(9, 5, "9×9 Grand", "5 in a row to win", "Grand tactical cloud match")
}

enum class GameMode {
    VS_AI,
    TWO_PLAYERS
}

enum class AIDifficulty(val label: String, val desc: String, val xpReward: Int) {
    EASY("Easy", "Gentle & playful cloud", 10),
    MEDIUM("Medium", "Clever little sky buddy", 25),
    HARD("Hard", "Unbeatable celestial mind", 50)
}

sealed class GameStatus {
    object InProgress : GameStatus()
    data class Won(val winner: Piece, val winningLines: List<List<Cell>>) : GameStatus() {
        val winningCells: List<Cell> get() = winningLines.flatten()

        companion object {
            fun singleLine(winner: Piece, winningCells: List<Cell>) = Won(winner, listOf(winningCells))
        }
    }
    object Draw : GameStatus()
}

enum class Screen {
    SPLASH,
    HOME,
    BOARD_SELECT,
    GAMEPLAY,
    SETTINGS
}

data class MoveRecord(
    val cell: Cell,
    val piece: Piece
)

data class AvatarOption(
    val id: String,
    val name: String,
    val description: String,
    val tag: String
)

val AVATAR_OPTIONS = listOf(
    AvatarOption("cloud_bow", "Cloud Bow", "Sweet fluffy puff with pink ribbon", "🎀"),
    AvatarOption("bunny_puff", "Bunny Puff", "Floppy-eared dream rabbit", "🐰"),
    AvatarOption("bear_fluff", "Bear Fluff", "Pastel sleepy bear cub", "🧸"),
    AvatarOption("star_kitty", "Star Kitty", "Whimsical cosmic kitten", "🐱"),
    AvatarOption("sleepy_moon", "Sleepy Moon", "Crescent moon slumbering", "🌙"),
    AvatarOption("angel_puff", "Angel Puff", "Golden halo cloud cherub", "👼")
)
