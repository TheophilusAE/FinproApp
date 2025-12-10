package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Question
import com.example.myapplication.data.QuestionType

@Composable
fun EditQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var questionText by remember { mutableStateOf(question.text) }
    var selectedType by remember { mutableStateOf(question.type) }
    var optionsText by remember { mutableStateOf(question.options.joinToString(", ")) }
    var answerText by remember { mutableStateOf(question.answer) }
    var weightText by remember { mutableStateOf(question.weight.toString()) }
    var explanationText by remember { mutableStateOf(question.explanation ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Question") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Text(text = "Question Type", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == QuestionType.MCQ,
                        onClick = { selectedType = QuestionType.MCQ },
                        label = { Text("MCQ") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == QuestionType.SHORT_TEXT,
                        onClick = { selectedType = QuestionType.SHORT_TEXT },
                        label = { Text("Short") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == QuestionType.LONG_TEXT,
                        onClick = { selectedType = QuestionType.LONG_TEXT },
                        label = { Text("Long") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == QuestionType.ESSAY,
                        onClick = { selectedType = QuestionType.ESSAY },
                        label = { Text("Essay") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedType == QuestionType.MCQ) {
                    OutlinedTextField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = { Text("Options (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("A, B, C, D") }
                    )
                }

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text(if (selectedType == QuestionType.MCQ) "Correct Answer" else "Sample Answer") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (selectedType == QuestionType.ESSAY) 3 else 1
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Points") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = explanationText,
                    onValueChange = { explanationText = it },
                    label = { Text("Explanation (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedQuestion = question.copy(
                        text = questionText,
                        type = selectedType,
                        options = if (selectedType == QuestionType.MCQ) 
                            optionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        else emptyList(),
                        answer = answerText,
                        weight = weightText.toDoubleOrNull() ?: 1.0,
                        explanation = explanationText.ifEmpty { null }
                    )
                    onSave(updatedQuestion)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
