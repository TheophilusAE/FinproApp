package com.example.myapplication.data

data class User(
    val id: String,
    val email: String,
    val password: String, // In production, this should be hashed
    val name: String,
    val role: UserRole = UserRole.TEACHER,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    TEACHER,
    ADMIN
}

data class UserSession(
    val userId: String,
    val email: String,
    val name: String,
    val role: UserRole
)
