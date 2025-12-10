package com.example.myapplication.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.myapplication.data.Repository
import com.example.myapplication.ai.GeminiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    var questionCount by remember { mutableStateOf(0) }
    var scanCount by remember { mutableStateOf(0) }
    var resultCount by remember { mutableStateOf(0) }
    val hasAI by remember { mutableStateOf(GeminiService.hasApiKey()) }
    
    LaunchedEffect(Unit) {
        questionCount = Repository.loadQuestions().size
        scanCount = Repository.loadScans().size
        resultCount = Repository.loadExamResults().size
    }
    
    val features = listOf(
        Triple("Scan Answers", "Quickly capture and recognize answer sheets", Icons.Default.PhotoCamera),
        Triple("Grade Exam", if (hasAI) "AI-powered grading with enhanced accuracy" else "Automatically grade scanned answer sheets", Icons.Default.Grade),
        Triple("Scan History", "View and manage all captured scans", Icons.Default.History),
        Triple("Question Bank", "Create and manage questions & keys", Icons.Default.ListAlt),
        Triple("Results", "View grading results and statistics", Icons.Default.Assessment),
        Triple("Students", "Manage student records and information", Icons.Default.School),
        Triple("Classes", "Organize students into class sections", Icons.Default.Class)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Hero header with animated gradient
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.size(64.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
                Column {
                    Text(
                        text = "AI Exam Grader",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (hasAI) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI-Powered • Enhanced Accuracy",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Smart grading with handwriting recognition",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f)
            )
            
            // Quick Stats
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    label = "Questions",
                    value = questionCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Scans",
                    value = scanCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Results",
                    value = resultCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            items(features.size) { index ->
                val feature = features[index]
                var isPressed by remember { mutableStateOf(false) }
                var isVisible by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 100L)
                    isVisible = true
                }
                
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = tween(durationMillis = 100)
                )
                
                val elevation by animateDpAsState(
                    targetValue = if (isPressed) 2.dp else 6.dp,
                    animationSpec = tween(durationMillis = 150)
                )

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(400)
                    )
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                            .shadow(elevation, shape = MaterialTheme.shapes.large)
                            .clickable {
                                when (feature.first) {
                                    "Scan Answers" -> navController.navigate("scan")
                                    "Grade Exam" -> navController.navigate("grading")
                                    "Scan History" -> navController.navigate("scanhistory")
                                    "Question Bank" -> navController.navigate("questionbank")
                                    "Results" -> navController.navigate("results")
                                    "Students" -> navController.navigate("students")
                                    "Classes" -> navController.navigate("classes")
                                }
                            },
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Card(
                                    modifier = Modifier.size(60.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (hasAI && (feature.first == "Grade Exam" || feature.first == "Scan Answers")) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        }
                                    ),
                                    shape = MaterialTheme.shapes.large,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = feature.third,
                                            contentDescription = feature.first,
                                            modifier = Modifier
                                                .size(32.dp),
                                            tint = if (hasAI && (feature.first == "Grade Exam" || feature.first == "Scan Answers")) {
                                                MaterialTheme.colorScheme.tertiary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = feature.first,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (hasAI && (feature.first == "Grade Exam" || feature.first == "Scan Answers")) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFFFFD700).copy(alpha = 0.25f)
                                                ),
                                                modifier = Modifier,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "AI",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color(0xFFFFB300),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = feature.second,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        maxLines = 2
                                    )
                                }
                            }
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                                contentDescription = "Go",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowHeader(title: String, subtitle: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}
