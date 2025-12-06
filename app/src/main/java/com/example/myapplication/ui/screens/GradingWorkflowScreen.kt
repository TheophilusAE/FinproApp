package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.Question
import com.example.myapplication.data.Repository
import com.example.myapplication.data.ScanRecord
import com.example.myapplication.grading.Grader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingWorkflowScreen(navController: NavController) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var scans by remember { mutableStateOf<List<ScanRecord>>(emptyList()) }
    var students by remember { mutableStateOf<List<com.example.myapplication.data.Student>>(emptyList()) }
    var selectedScan by remember { mutableStateOf<ScanRecord?>(null) }
    var selectedStudent by remember { mutableStateOf<com.example.myapplication.data.Student?>(null) }
    var examId by remember { mutableStateOf("") }
    var isGrading by remember { mutableStateOf(false) }
    var gradingComplete by remember { mutableStateOf(false) }
    var lastScore by remember { mutableStateOf(0.0) }
    var showStudentPicker by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        questions = Repository.loadQuestions()
        scans = Repository.loadScans()
        students = Repository.loadStudents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Grade,
                        contentDescription = "Grade",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Grade Exam",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Select a scan and grade against question bank",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Cards
            item {
                InfoCard(
                    title = "Question Bank",
                    value = "${questions.size} questions",
                    description = if (questions.isEmpty()) "Add questions first" else "Ready to grade"
                )
            }
            
            item {
                InfoCard(
                    title = "Available Scans",
                    value = "${scans.size} scans",
                    description = if (scans.isEmpty()) "Capture answer sheets first" else "Select one below"
                )
            }

            // Student Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Exam Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Student Selection
                        OutlinedButton(
                            onClick = { showStudentPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (selectedStudent != null) selectedStudent!!.name else "Select Student",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (selectedStudent != null) {
                                        Text(
                                            text = "ID: ${selectedStudent!!.studentNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = examId,
                            onValueChange = { examId = it },
                            label = { Text("Exam ID") },
                            placeholder = { Text("e.g., MIDTERM_2024") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Scan Selection
            item {
                Text(
                    text = "Select Scan to Grade",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(scans) { scan ->
                ScanSelectionCard(
                    scan = scan,
                    isSelected = selectedScan?.id == scan.id,
                    onSelect = { selectedScan = scan }
                )
            }
        }

        // Grade Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AnimatedVisibility(
                    visible = gradingComplete,
                    enter = scaleIn(animationSpec = tween(500)),
                    exit = scaleOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Grading Complete!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Score: ${String.format("%.1f", lastScore)}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        if (selectedScan != null && selectedStudent != null && examId.isNotBlank() && questions.isNotEmpty()) {
                            scope.launch {
                                isGrading = true
                                gradingComplete = false
                                
                                // Improved answer parsing - handles multiple formats
                                val answers = parseAnswersFromText(
                                    selectedScan!!.recognizedText,
                                    questions
                                )
                                
                                // Grade
                                val result = Grader.grade(questions, answers, selectedStudent!!.id, examId)
                                lastScore = result.totalScore
                                
                                // Update scan with student ID
                                Repository.updateScanWithStudent(selectedScan!!.id, selectedStudent!!.id)
                                
                                // Save result
                                Repository.saveExamResult(result)
                                
                                isGrading = false
                                gradingComplete = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isGrading && selectedScan != null && selectedStudent != null && examId.isNotBlank() && questions.isNotEmpty()
                ) {
                    if (isGrading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grading...")
                    } else {
                        Icon(Icons.Default.Grade, contentDescription = "Grade")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grade Now", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (gradingComplete) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate("results")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("View Results")
                    }
                }
            }
        }
        
        // Student Picker Dialog
        if (showStudentPicker) {
            AlertDialog(
                onDismissRequest = { showStudentPicker = false },
                title = { Text("Select Student") },
                text = {
                    LazyColumn {
                        if (students.isEmpty()) {
                            item {
                                Text(
                                    "No students found. Add students first.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            items(students) { student ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedStudent = student
                                            showStudentPicker = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedStudent?.id == student.id)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ID: ${student.studentNumber}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (student.email.isNotBlank()) {
                                            Text(
                                                text = student.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStudentPicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, description: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanSelectionCard(
    scan: ScanRecord,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan ${scan.id.take(8)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = scan.recognizedText.take(60) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Improved answer parsing that handles multiple formats:
 * - "1. A" or "1) A" or "Q1: A" or "Question 1: A"
 * - Line-by-line answers
 * - Number-answer pairs
 */
private fun parseAnswersFromText(text: String, questions: List<Question>): Map<String, String> {
    val answers = mutableMapOf<String, String>()
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    
    // Try pattern matching first: "1. Answer" or "Q1: Answer"
    val patterns = listOf(
        Regex("""^(\d+)[\.):\s]+(.+)$"""),           // "1. Answer" or "1) Answer" or "1: Answer"
        Regex("""^[Qq](\d+)[\.):\s]+(.+)$"""),       // "Q1. Answer" or "q1: Answer"
        Regex("""^Question\s*(\d+)[\.):\s]+(.+)$""", RegexOption.IGNORE_CASE)  // "Question 1: Answer"
    )
    
    var matchedCount = 0
    for (line in lines) {
        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                val questionNum = match.groupValues[1].toIntOrNull() ?: continue
                val answer = match.groupValues[2].trim()
                
                // Map question number to question ID
                if (questionNum > 0 && questionNum <= questions.size) {
                    val questionId = questions[questionNum - 1].id
                    answers[questionId] = answer
                    matchedCount++
                }
                break
            }
        }
    }
    
    // If pattern matching didn't work well, fall back to line-by-line
    if (matchedCount < questions.size / 2) {
        answers.clear()
        lines.forEachIndexed { index, line ->
            if (index < questions.size) {
                // Remove common prefixes
                var cleanLine = line
                    .replace(Regex("""^(\d+[\.):\s]+|[Qq]\d+[\.):\s]+|Question\s*\d+[\.):\s]+)""", RegexOption.IGNORE_CASE), "")
                    .trim()
                
                if (cleanLine.isNotBlank()) {
                    answers[questions[index].id] = cleanLine
                }
            }
        }
    }
    
    return answers
}
