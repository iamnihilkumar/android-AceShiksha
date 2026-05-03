package com.nikhil.aceshiksha.models

data class SubjectStat(
    val subject: String = "",
    val quizzesAttempted: Int = 0,
    val totalScore: Int = 0,
    val totalQuestions: Int = 0
) {
    val accuracy: Int
        get() = if (totalQuestions > 0) (totalScore * 100) / totalQuestions else 0
}

data class GameTypeStat(
    val gameType: String = "",
    val gamesPlayed: Int = 0,
    val totalScore: Int = 0,
    val totalQuestions: Int = 0
) {
    val accuracy: Int
        get() = if (totalQuestions > 0) (totalScore * 100) / totalQuestions else 0
}

data class StudentReport(
    // Quiz stats
    val totalQuizzesAttempted: Int = 0,
    val totalQuizScore: Int = 0,
    val totalQuizQuestions: Int = 0,
    val bestQuizScore: Int = 0,         // percentage
    val lastQuizDate: Long = 0L,

    // Game stats
    val totalGamesPlayed: Int = 0,
    val totalGameScore: Int = 0,
    val totalGameQuestions: Int = 0,
    val totalXpFromGames: Int = 0,

    // Subject breakdown
    val subjectStats: List<SubjectStat> = emptyList(),

    // Game type breakdown
    val gameTypeStats: List<GameTypeStat> = emptyList(),

    // Overall
    val totalXp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0
) {
    val quizAccuracy: Int
        get() = if (totalQuizQuestions > 0) (totalQuizScore * 100) / totalQuizQuestions else 0

    val gameAccuracy: Int
        get() = if (totalGameQuestions > 0) (totalGameScore * 100) / totalGameQuestions else 0
}
