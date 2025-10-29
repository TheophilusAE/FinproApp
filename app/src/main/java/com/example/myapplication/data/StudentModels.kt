package com.example.myapplication.data

data class Student(
    val id: String,
    val name: String,
    val studentNumber: String,
    val email: String,
    val classSection: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ClassSection(
    val id: String,
    val name: String,
    val description: String? = null,
    val teacherId: String,
    val studentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class AnswerKeyTemplate(
    val id: String,
    val name: String,
    val examId: String,
    val questions: List<QuestionAnswer>,
    val totalPoints: Double,
    val passingScore: Double = 60.0,
    val createdAt: Long = System.currentTimeMillis()
)

data class QuestionAnswer(
    val questionId: String,
    val correctAnswer: String,
    val points: Double
)
