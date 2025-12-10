package com.example.myapplication.data

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType = QuestionType.MCQ,
    val options: List<String> = emptyList(),
    val answer: String = "",
    val weight: Double = 1.0,
    val explanation: String? = null
)

enum class QuestionType { MCQ, ESSAY }

data class ExamResult(
    val studentId: String,
    val examId: String,
    val totalScore: Double,
    val details: Map<String, Double> = emptyMap()
)

data class ScanRecord(
    val id: String,
    val filePath: String,
    val recognizedText: String,
    val studentId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float? = null, // OCR accuracy percentage (0-100)
    val confidenceLevel: String = "Medium", // Low, Medium, High
    val enhancedByAI: Boolean = false // Whether AI enhanced the text
)
