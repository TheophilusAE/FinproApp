package com.example.myapplication.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.example.myapplication.data.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/**
 * AI Service using Google Gemini for:
 * 1. OCR correction - Fix missing/incorrect text from ML Kit scan
 * 2. Answer grading - Grade student answers against correct answers
 */
object GeminiService {
    
    // Store API key (user should set this in settings)
    private var apiKey: String? = null
    
    fun setApiKey(key: String) {
        apiKey = key
    }
    
    fun getApiKey(): String? = apiKey
    
    fun hasApiKey(): Boolean = !apiKey.isNullOrBlank()
    
    /**
     * Enhance OCR results by having AI correct and complete the text
     * Takes the raw OCR text and the original image to produce better results
     */
    suspend fun enhanceOCRText(
        context: Context,
        imageUri: Uri,
        rawOcrText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!hasApiKey()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }
            
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey!!
            )
            
            // Load and prepare image
            val bitmap = loadBitmap(context, imageUri)
            
            val prompt = """
You are an expert OCR correction assistant. You have received:
1. Raw OCR text (which may have errors or missing parts)
2. The original handwritten answer sheet image

Your task:
- Carefully examine the image to identify any text that the OCR missed or got wrong
- Correct any OCR errors (misspelled words, wrong characters, etc.)
- Fill in any missing text that you can see in the image but wasn't captured by OCR
- Preserve the original format (numbered answers like "1. A", "2. B", etc.)
- Keep the same numbering and structure as the student wrote
- Output ONLY the corrected text, nothing else

Raw OCR Text:
$rawOcrText

Please provide the corrected and complete text from the image.
            """.trimIndent()
            
            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            
            val enhancedText = response.text?.trim() ?: rawOcrText
            Result.success(enhancedText)
            
        } catch (e: Exception) {
            android.util.Log.e("GeminiService", "OCR enhancement error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Grade student answers using AI
     * Provides more nuanced grading than simple matching, especially for essay questions
     */
    suspend fun gradeAnswers(
        questions: List<Question>,
        studentAnswers: Map<String, String>,
        examId: String
    ): Result<GradingResult> = withContext(Dispatchers.IO) {
        try {
            if (!hasApiKey()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }
            
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey!!
            )
            
            // Build grading prompt
            val questionsJson = JSONArray()
            questions.forEach { q ->
                val qObj = JSONObject()
                qObj.put("id", q.id)
                qObj.put("question", q.text)
                qObj.put("type", q.type.name)
                qObj.put("correctAnswer", q.answer)
                qObj.put("points", q.weight)
                qObj.put("studentAnswer", studentAnswers[q.id] ?: "")
                questionsJson.put(qObj)
            }
            
            val prompt = """
You are an expert exam grader. Grade the following student answers carefully and fairly.

For MCQ (Multiple Choice Questions):
- Award full points if the answer exactly matches (case-insensitive)
- Award 0 points if incorrect

For ESSAY questions:
- Award points based on correctness, completeness, and understanding (0-100% of points)
- Consider key concepts, accuracy, and depth of answer
- Be fair but thorough in evaluation

Questions and Answers:
${questionsJson.toString(2)}

Respond with ONLY a JSON object in this exact format:
{
  "scores": {
    "questionId1": score1,
    "questionId2": score2,
    ...
  },
  "totalScore": totalScore,
  "feedback": {
    "questionId1": "brief feedback",
    "questionId2": "brief feedback",
    ...
  }
}

Ensure all question IDs from the input are included in the output.
            """.trimIndent()
            
            val response = model.generateContent(prompt)
            val jsonText = response.text?.trim() ?: ""
            
            // Parse AI response
            val result = parseGradingResponse(jsonText, questions, studentAnswers)
            Result.success(result)
            
        } catch (e: Exception) {
            android.util.Log.e("GeminiService", "Grading error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse the AI grading response into a structured result
     */
    private fun parseGradingResponse(
        jsonText: String,
        questions: List<Question>,
        studentAnswers: Map<String, String>
    ): GradingResult {
        return try {
            // Extract JSON from markdown code blocks if present
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val jsonObj = JSONObject(cleanJson)
            val scoresObj = jsonObj.getJSONObject("scores")
            val feedbackObj = jsonObj.optJSONObject("feedback") ?: JSONObject()
            
            val scores = mutableMapOf<String, Double>()
            val feedback = mutableMapOf<String, String>()
            
            questions.forEach { q ->
                scores[q.id] = scoresObj.optDouble(q.id, 0.0)
                feedback[q.id] = feedbackObj.optString(q.id, "")
            }
            
            val totalScore = jsonObj.optDouble("totalScore", scores.values.sum())
            
            GradingResult(
                scores = scores,
                totalScore = totalScore,
                feedback = feedback
            )
        } catch (e: Exception) {
            android.util.Log.e("GeminiService", "Failed to parse grading response", e)
            // Fallback to basic grading
            fallbackGrading(questions, studentAnswers)
        }
    }
    
    /**
     * Fallback grading if AI fails
     */
    private fun fallbackGrading(
        questions: List<Question>,
        studentAnswers: Map<String, String>
    ): GradingResult {
        val scores = mutableMapOf<String, Double>()
        val feedback = mutableMapOf<String, String>()
        
        questions.forEach { q ->
            val studentAns = studentAnswers[q.id]?.trim() ?: ""
            val correctAns = q.answer.trim()
            
            val score = when (q.type) {
                com.example.myapplication.data.QuestionType.MCQ -> {
                    if (studentAns.equals(correctAns, ignoreCase = true)) {
                        feedback[q.id] = "Correct"
                        q.weight
                    } else {
                        feedback[q.id] = "Incorrect. Correct answer: $correctAns"
                        0.0
                    }
                }
                com.example.myapplication.data.QuestionType.ESSAY -> {
                    // Simple word matching for fallback
                    val similarity = calculateSimilarity(studentAns, correctAns)
                    feedback[q.id] = "Score based on keyword matching"
                    similarity * q.weight
                }
            }
            scores[q.id] = score
        }
        
        return GradingResult(
            scores = scores,
            totalScore = scores.values.sum(),
            feedback = feedback
        )
    }
    
    private fun calculateSimilarity(text1: String, text2: String): Double {
        val words1 = text1.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        val words2 = text2.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union > 0) intersection.toDouble() / union else 0.0
    }
    
    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream).also {
            inputStream?.close()
        }
    }
}

/**
 * Result of AI grading
 */
data class GradingResult(
    val scores: Map<String, Double>,
    val totalScore: Double,
    val feedback: Map<String, String>
)
