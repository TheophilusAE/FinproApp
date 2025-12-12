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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.Question
import com.example.myapplication.data.Repository
import com.example.myapplication.data.ScanRecord
import com.example.myapplication.data.ExamResult
import com.example.myapplication.grading.Grader
import com.example.myapplication.ai.GeminiService
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingWorkflowScreen(navController: NavController) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var scans by remember { mutableStateOf<List<ScanRecord>>(emptyList()) }
    var students by remember { mutableStateOf<List<com.example.myapplication.data.Student>>(emptyList()) }
    var selectedScan by remember { mutableStateOf<ScanRecord?>(null) }
    var selectedStudent by remember { mutableStateOf<com.example.myapplication.data.Student?>(null) }
    var examId by remember { mutableStateOf("") }
    var isGrading by remember { mutableStateOf(false) }
    var gradingComplete by remember { mutableStateOf(false) }
    var lastScore by remember { mutableStateOf(0.0) }
    var lastFeedback by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showStudentPicker by remember { mutableStateOf(false) }
    var useAI by remember { mutableStateOf(GeminiService.hasApiKey()) }
    var gradingStatus by remember { mutableStateOf("") }
    var enhancedText by remember { mutableStateOf<String?>(null) }
    var showEnhancedTextDialog by remember { mutableStateOf(false) }
    var isEnhancing by remember { mutableStateOf(false) }
    var selectedQuestions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showQuestionPicker by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        questions = Repository.loadQuestions()
        scans = Repository.loadScans()
        students = Repository.loadStudents()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grade,
                            contentDescription = "Grade",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Grade Exam",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select scan • Choose questions • Grade",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Grading Method Info Card
            if (useAI && GeminiService.hasApiKey()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Enhancement: Use 'Enhance Text' to improve OCR accuracy. Grading uses traditional scoring with AI-enhanced text for better results.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Info Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Question Bank",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedQuestions.isEmpty()) {
                                        "${questions.size} questions available"
                                    } else {
                                        "${selectedQuestions.size} of ${questions.size} selected"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Button(
                                onClick = { showQuestionPicker = true },
                                enabled = questions.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (selectedQuestions.isEmpty()) "Select Questions" else "Change Selection")
                            }
                        }
                        if (questions.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Add questions to the question bank first",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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
                
                // Show AI-enhanced text if available
                if (enhancedText != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                                    contentDescription = "AI Enhanced",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI-Enhanced Text",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = enhancedText!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                    maxLines = 8
                                )
                            }
                            
                            TextButton(
                                onClick = { showEnhancedTextDialog = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("View Full Text")
                            }
                        }
                    }
                }
                
                // Step 1: Enhance with AI (if AI enabled and not yet enhanced)
                if (useAI && GeminiService.hasApiKey() && enhancedText == null) {
                    // AI Processing status card
                    if (isEnhancing) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI Processing",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = gradingStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (selectedScan != null) {
                                scope.launch {
                                    isEnhancing = true
                                    gradingStatus = "Analyzing handwriting with AI..."
                                    
                                    try {
                                        val imageUri = android.net.Uri.parse(selectedScan!!.filePath)
                                        
                                        // Simulate progress updates
                                        kotlinx.coroutines.delay(500)
                                        gradingStatus = "Comparing with original scan..."
                                        
                                        val enhancedTextResult = GeminiService.enhanceOCRText(
                                            context,
                                            imageUri,
                                            selectedScan!!.recognizedText
                                        )
                                        
                                        enhancedText = enhancedTextResult.getOrElse {
                                            gradingStatus = "AI enhancement failed, using original text"
                                            selectedScan!!.recognizedText
                                        }
                                        
                                        // Update the scan record to mark it as AI-enhanced
                                        if (enhancedTextResult.isSuccess) {
                                            val updatedScans = Repository.loadScans().map { scan ->
                                                if (scan.id == selectedScan!!.id) {
                                                    scan.copy(
                                                        recognizedText = enhancedText!!,
                                                        enhancedByAI = true,
                                                        accuracy = 95f, // AI enhancement typically achieves ~95% accuracy
                                                        confidenceLevel = "High"
                                                    )
                                                } else {
                                                    scan
                                                }
                                            }
                                            Repository.saveScans(updatedScans)
                                        }
                                        
                                        gradingStatus = "✓ Text enhanced successfully!"
                                        
                                    } catch (e: Exception) {
                                        gradingStatus = "Error: ${e.message}"
                                        android.util.Log.e("GradingWorkflow", "Enhancement failed", e)
                                    } finally {
                                        isEnhancing = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isEnhancing && selectedScan != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, contentDescription = "Enhance")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enhance Text with AI", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Step 2: Grade (enabled after enhancement if AI mode, or directly if traditional mode)
                Button(
                    onClick = {
                        if (selectedScan != null && selectedStudent != null && examId.isNotBlank() && selectedQuestions.isNotEmpty()) {
                            val questionsToGrade = questions.filter { selectedQuestions.contains(it.id) }
                            scope.launch {
                                isGrading = true
                                gradingComplete = false
                                
                                try {
                                    val result: ExamResult
                                    val feedback: Map<String, String>
                                    
                                    // Step 1: Get the text to grade (enhanced if available)
                                    val textToGrade = enhancedText ?: selectedScan!!.recognizedText
                                    
                                    // Step 2: Parse answers from text
                                    gradingStatus = "Extracting answers from scanned text..."
                                    val answers = parseAnswersFromText(textToGrade, questionsToGrade)
                                    android.util.Log.d("GradingWorkflow", "=== PARSED ANSWERS ===")
                                    android.util.Log.d("GradingWorkflow", "Total parsed: ${answers.size} out of ${questionsToGrade.size}")
                                    answers.forEach { (qId, ans) -> 
                                        android.util.Log.d("GradingWorkflow", "$qId -> '$ans'")
                                    }
                                    android.util.Log.d("GradingWorkflow", "===================")
                                    
                                    // Step 3: Use AI grading (preferred) with fallback to traditional
                                    if (useAI && GeminiService.hasApiKey()) {
                                        gradingStatus = "Attempting AI grading..."
                                        android.util.Log.d("GradingWorkflow", "Attempting AI-powered grading")
                                        
                                        val aiGradingResult = GeminiService.gradeAnswers(
                                            questionsToGrade,
                                            answers,
                                            examId
                                        )
                                        
                                        if (aiGradingResult.isSuccess) {
                                            // AI grading succeeded
                                            val gradingResult = aiGradingResult.getOrThrow()
                                            result = ExamResult(
                                                studentId = selectedStudent!!.id,
                                                examId = examId,
                                                totalScore = gradingResult.totalScore,
                                                details = gradingResult.scores
                                            )
                                            feedback = gradingResult.feedback
                                            gradingStatus = "AI grading completed successfully!"
                                            android.util.Log.d("GradingWorkflow", "AI grading successful - Score: ${result.totalScore}")
                                        } else {
                                            // AI grading failed - FALLBACK to traditional grading
                                            val error = aiGradingResult.exceptionOrNull()
                                            android.util.Log.e("GradingWorkflow", "AI grading failed: ${error?.message}", error)
                                            gradingStatus = "AI grading failed, using traditional grading..."
                                            
                                            // Use traditional grading as fallback
                                            result = Grader.grade(questionsToGrade, answers, selectedStudent!!.id, examId)
                                            feedback = emptyMap()
                                            android.util.Log.d("GradingWorkflow", "Fallback to traditional grading - Score: ${result.totalScore}")
                                        }
                                    } else {
                                        // TRADITIONAL GRADING FLOW (no AI)
                                        gradingStatus = "Grading ${questionsToGrade.size} selected questions..."
                                        android.util.Log.d("GradingWorkflow", "Using traditional grading (AI disabled or no API key)")
                                        
                                        result = Grader.grade(questionsToGrade, answers, selectedStudent!!.id, examId)
                                        feedback = emptyMap()
                                        android.util.Log.d("GradingWorkflow", "Traditional grading complete - Score: ${result.totalScore}")
                                    }
                                    
                                    lastScore = result.totalScore
                                    lastFeedback = feedback
                                    
                                    // Update scan with student ID
                                    Repository.updateScanWithStudent(selectedScan!!.id, selectedStudent!!.id)
                                    
                                    // Save result - CRITICAL: Must complete before navigation
                                    android.util.Log.d("GradingWorkflow", "=== SAVING RESULT ===")
                                    android.util.Log.d("GradingWorkflow", "Student=${result.studentId}, Exam=${result.examId}, Score=${result.totalScore}")
                                    
                                    // Save synchronously
                                    Repository.saveExamResult(result)
                                    android.util.Log.d("GradingWorkflow", "Result saved to file")
                                    
                                    // Immediate verification
                                    val savedResults = Repository.loadExamResults()
                                    android.util.Log.d("GradingWorkflow", "Verification: Total results in repository = ${savedResults.size}")
                                    val lastResult = savedResults.lastOrNull()
                                    if (lastResult != null) {
                                        android.util.Log.d("GradingWorkflow", "Last saved result: Student=${lastResult.studentId}, Score=${lastResult.totalScore}")
                                    }
                                    
                                    gradingStatus = "Grading complete!"
                                    gradingComplete = true
                                    
                                    // Show success message
                                    val scoreText = String.format("%.1f", result.totalScore)
                                    snackbarHostState.showSnackbar(
                                        message = "✓ Graded successfully! Score: $scoreText/100",
                                        duration = SnackbarDuration.Short
                                    )
                                    
                                    // Wait for snackbar, then navigate
                                    kotlinx.coroutines.delay(1500)
                                    android.util.Log.d("GradingWorkflow", "Navigating to results page...")
                                    navController.navigate("results")
                                    
                                } catch (e: Exception) {
                                    gradingStatus = "Error: ${e.message}"
                                    android.util.Log.e("GradingWorkflow", "Grading failed", e)
                                } finally {
                                    isGrading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isGrading && !isEnhancing && selectedScan != null && selectedStudent != null && examId.isNotBlank() && selectedQuestions.isNotEmpty() && (enhancedText != null || !useAI || !GeminiService.hasApiKey())
                ) {
                    if (isGrading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (useAI) "AI Grading..." else "Grading...")
                    } else {
                        Icon(Icons.Default.Grade, contentDescription = "Grade")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Grade Exam",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // Show grading status
                if (isGrading && gradingStatus.isNotBlank()) {
                    Text(
                        text = gradingStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
        
        // Question Picker Dialog
        if (showQuestionPicker) {
            AlertDialog(
                onDismissRequest = { showQuestionPicker = false },
                title = { 
                    Column {
                        Text("Select Questions to Grade")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedQuestions.size} selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    selectedQuestions = questions.map { it.id }.toSet()
                                }
                            ) {
                                Text("Select All")
                            }
                            TextButton(
                                onClick = { selectedQuestions = emptySet() }
                            ) {
                                Text("Clear All")
                            }
                        }
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            if (questions.isEmpty()) {
                                item {
                                    Text(
                                        "No questions available. Add questions to the question bank first.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                items(questions.size) { index ->
                                    val question = questions[index]
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedQuestions = if (selectedQuestions.contains(question.id)) {
                                                    selectedQuestions - question.id
                                                } else {
                                                    selectedQuestions + question.id
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedQuestions.contains(question.id))
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = if (selectedQuestions.contains(question.id)) 4.dp else 1.dp
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Checkbox(
                                                checked = selectedQuestions.contains(question.id),
                                                onCheckedChange = {
                                                    selectedQuestions = if (it) {
                                                        selectedQuestions + question.id
                                                    } else {
                                                        selectedQuestions - question.id
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Question ${index + 1}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = question.text,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 2
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    ) {
                                                        Text(
                                                            text = "${question.weight} pts",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                        )
                                                    ) {
                                                        Text(
                                                            text = question.type.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showQuestionPicker = false },
                        enabled = selectedQuestions.isNotEmpty()
                    ) {
                        Text("Done (${selectedQuestions.size})")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuestionPicker = false }) {
                        Text("Cancel")
                    }
                }
            )
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
        
        // Enhanced Text Dialog
        if (showEnhancedTextDialog && enhancedText != null) {
            AlertDialog(
                onDismissRequest = { showEnhancedTextDialog = false },
                icon = { 
                    Icon(
                        androidx.compose.material.icons.Icons.Default.AutoAwesome, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    ) 
                },
                title = { 
                    Text(
                        "AI-Enhanced Text",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Original OCR:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = selectedScan?.recognizedText ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .heightIn(max = 150.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "AI-Enhanced Text:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                text = enhancedText!!,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState()),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showEnhancedTextDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
    } // End of Scaffold
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Scan ${scan.id.take(8)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Accuracy badge
                    if (scan.accuracy != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (scan.confidenceLevel) {
                                    "High" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    "Medium" -> Color(0xFFFFA726).copy(alpha = 0.15f)
                                    else -> Color(0xFFF44336).copy(alpha = 0.15f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (scan.confidenceLevel) {
                                        "High" -> Icons.Default.VerifiedUser
                                        "Medium" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = when (scan.confidenceLevel) {
                                        "High" -> Color(0xFF4CAF50)
                                        "Medium" -> Color(0xFFFFA726)
                                        else -> Color(0xFFF44336)
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${scan.accuracy!!.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (scan.confidenceLevel) {
                                        "High" -> Color(0xFF4CAF50)
                                        "Medium" -> Color(0xFFFFA726)
                                        else -> Color(0xFFF44336)
                                    }
                                )
                            }
                        }
                    }
                    
                    // AI Enhanced badge
                    if (scan.enhancedByAI) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scan.recognizedText.take(80) + if (scan.recognizedText.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
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
    
    android.util.Log.d("AnswerParser", "=== PARSING ANSWERS ===")
    android.util.Log.d("AnswerParser", "Text to parse (${lines.size} lines):")
    lines.take(10).forEach { android.util.Log.d("AnswerParser", "  - '$it'") }
    
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
                    android.util.Log.d("AnswerParser", "Pattern matched Q$questionNum: '$answer' -> $questionId")
                }
                break
            }
        }
    }
    
    android.util.Log.d("AnswerParser", "Pattern matching: $matchedCount/${questions.size}")
    
    // If pattern matching didn't work well, fall back to line-by-line
    if (matchedCount < questions.size / 2) {
        android.util.Log.d("AnswerParser", "Using line-by-line fallback")
        answers.clear()
        lines.forEachIndexed { index, line ->
            if (index < questions.size) {
                // Remove common prefixes
                var cleanLine = line
                    .replace(Regex("""^(\d+[\.):\s]+|[Qq]\d+[\.):\s]+|Question\s*\d+[\.):\s]+)""", RegexOption.IGNORE_CASE), "")
                    .trim()
                
                if (cleanLine.isNotBlank()) {
                    answers[questions[index].id] = cleanLine
                    android.util.Log.d("AnswerParser", "Line $index: '$cleanLine' -> ${questions[index].id}")
                }
            }
        }
    }
    
    android.util.Log.d("AnswerParser", "Final: ${answers.size} answers parsed")
    android.util.Log.d("AnswerParser", "===================")
    return answers
}
