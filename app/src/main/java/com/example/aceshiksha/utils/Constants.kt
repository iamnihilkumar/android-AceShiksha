package com.nikhil.aceshiksha.utils

object Constants {
    const val COLLECTION_USERS = "users"
    const val ROLE_STUDENT = "student"
    const val ROLE_TEACHER = "teacher"
    const val PREF_NAME = "EduReachPrefs"
    const val PREF_USER_ROLE = "user_role"


    val GEMINI_API_KEY: String get() = com.nikhil.aceshiksha.BuildConfig.GEMINI_API_KEY

    // XP rewards
    const val XP_LESSON_COMPLETE = 20
    const val XP_CORRECT_ANSWER = 10

    // Game collections
    const val COLLECTION_GAME_QUESTIONS = "game_questions"
    const val COLLECTION_GAME_ATTEMPTS  = "game_attempts"

    // Quiz collections
    const val COLLECTION_QUIZZES        = "quizzes"
    const val COLLECTION_QUIZ_ATTEMPTS  = "student_quiz_attempts"

    // Game types
    const val GAME_TYPE_MAZE        = "maze"
    const val GAME_TYPE_CAR_RACE    = "car_race"
    const val GAME_TYPE_QUIZ_BATTLE = "quiz_battle"
    // XP for games
    const val XP_GAME_COMPLETE = 30
    const val XP_GAME_CORRECT  = 10

    const val GAME_TOTAL_QUESTIONS = 10
}
