package com.nikhil.aceshiksha.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student",
    val classLevel: String = "",
    val xp: Int = 0,
    val streak: Int = 0,
    val level: Int = 1,
    val badges: List<String> = emptyList(),
)