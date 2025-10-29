package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Question
import com.example.myapplication.data.QuestionType

@Composable
fun AddQuestionDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    if (!open) return

    val textState = remember { mutableStateOf("") }
    val answerState = remember { mutableStateOf("") }
    val optionsState = remember { mutableStateOf("") }
    val weightState = remember { mutableStateOf("1.0") }
    val typeState = remember { mutableStateOf(QuestionType.MCQ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val opts = if (typeState.value == QuestionType.MCQ) {
                    optionsState.value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                } else emptyList()

                val weight = weightState.value.toDoubleOrNull() ?: 1.0
                val q = Question(
                    id = java.util.UUID.randomUUID().toString(),
                    text = textState.value,
                    type = typeState.value,
                    options = opts,
                    answer = answerState.value,
                    weight = weight
                )
                onSave(q)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(text = "Add Question") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(value = textState.value, onValueChange = { textState.value = it }, label = { Text("Question text") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                // Type selector (simple)
                RowTypeSelector(typeState = typeState.value, onTypeChange = { typeState.value = it })
                Spacer(modifier = Modifier.height(8.dp))
                if (typeState.value == QuestionType.MCQ) {
                    OutlinedTextField(value = optionsState.value, onValueChange = { optionsState.value = it }, label = { Text("Options (comma separated)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(value = answerState.value, onValueChange = { answerState.value = it }, label = { Text("Answer") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = weightState.value, onValueChange = { weightState.value = it }, label = { Text("Weight (numeric)") }, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@Composable
private fun RowTypeSelector(typeState: QuestionType, onTypeChange: (QuestionType) -> Unit) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        Button(onClick = { onTypeChange(QuestionType.MCQ) }, modifier = Modifier.weight(1f)) { Text("MCQ") }
        Button(onClick = { onTypeChange(QuestionType.ESSAY) }, modifier = Modifier.weight(1f)) { Text("Essay") }
    }
}
