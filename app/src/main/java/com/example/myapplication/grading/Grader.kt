package com.example.myapplication.grading

import com.example.myapplication.data.Question
import com.example.myapplication.data.ExamResult
import kotlin.math.sqrt

object Grader {
    /**
     * Grade answers given a list of questions and a map of questionId -> studentAnswer
     * MCQ: exact match (case-insensitive) -> full points if equal
     * SHORT_TEXT: keyword matching + similarity (more flexible)
     * LONG_TEXT/ESSAY: compute text similarity (cosine over token frequencies) and scale to weight
     */
    fun grade(questions: List<Question>, answers: Map<String, String>, studentId: String, examId: String): ExamResult {
        val details = mutableMapOf<String, Double>()
        var total = 0.0

        android.util.Log.d("Grader", "=== TRADITIONAL GRADING ===")
        android.util.Log.d("Grader", "Questions: ${questions.size}, Answers: ${answers.size}")

        for (q in questions) {
            val studentAns = answers[q.id] ?: ""
            android.util.Log.d("Grader", "\nQuestion: ${q.id} (${q.type})")
            android.util.Log.d("Grader", "Correct: '${q.answer}'")
            android.util.Log.d("Grader", "Student: '$studentAns'")
            
            val score = when (q.type) {
                com.example.myapplication.data.QuestionType.MCQ -> {
                    val match = studentAns.trim().equals(q.answer.trim(), ignoreCase = true)
                    val result = if (match) q.weight else 0.0
                    android.util.Log.d("Grader", "MCQ Match: $match, Score: $result/${q.weight}")
                    result
                }
                com.example.myapplication.data.QuestionType.SHORT_TEXT -> {
                    // For short text, use keyword matching and partial credit
                    val sim = similarity(studentAns, q.answer)
                    // Give more generous scoring for short answers (min 40% if any keywords match)
                    val adjustedSim = if (sim > 0.3) sim.coerceAtLeast(0.4) else sim
                    val result = adjustedSim * q.weight
                    android.util.Log.d("Grader", "SHORT_TEXT Similarity: $sim, Adjusted: $adjustedSim, Score: $result/${q.weight}")
                    result
                }
                com.example.myapplication.data.QuestionType.LONG_TEXT,
                com.example.myapplication.data.QuestionType.ESSAY -> {
                    val sim = similarity(studentAns, q.answer)
                    val result = sim * q.weight
                    android.util.Log.d("Grader", "ESSAY Similarity: $sim, Score: $result/${q.weight}")
                    result
                }
            }
            details[q.id] = score
            total += score
        }

        android.util.Log.d("Grader", "\nTotal Score: $total")
        android.util.Log.d("Grader", "=== GRADING COMPLETE ===")
        return ExamResult(studentId = studentId, examId = examId, totalScore = total, details = details)
    }

    private fun tokenize(s: String): List<String> = s.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }

    // Simple cosine similarity
    private fun similarity(a: String, b: String): Double {
        val ta = tokenize(a)
        val tb = tokenize(b)
        if (ta.isEmpty() || tb.isEmpty()) return 0.0
        val freqa = mutableMapOf<String, Int>()
        val freqb = mutableMapOf<String, Int>()
        for (t in ta) freqa[t] = (freqa[t] ?: 0) + 1
        for (t in tb) freqb[t] = (freqb[t] ?: 0) + 1
        val all = (freqa.keys + freqb.keys)
        var dot = 0.0
        var norma = 0.0
        var normb = 0.0
        for (k in all) {
            val va = freqa[k]?.toDouble() ?: 0.0
            val vb = freqb[k]?.toDouble() ?: 0.0
            dot += va * vb
            norma += va * va
            normb += vb * vb
        }
        if (norma == 0.0 || normb == 0.0) return 0.0
        return dot / (sqrt(norma) * sqrt(normb))
    }
}
