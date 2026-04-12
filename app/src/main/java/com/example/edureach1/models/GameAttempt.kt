package com.example.edureach1.models

data class GameAttempt(
    val attemptId: String = "",
    val studentUid: String = "",
    val gameType: String = "",
    val classLevel: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Int = 0,
    val playedAt: Long = 0L
)
