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
import com.example.myapplication.data.Repository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    var questionCount by remember { mutableStateOf(0) }
    var scanCount by remember { mutableStateOf(0) }
    var resultCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        questionCount = Repository.loadQuestions().size
        scanCount = Repository.loadScans().size
        resultCount = Repository.loadExamResults().size
    }
    
    val features = listOf(
        Triple("Scan Answers", "Quickly capture and recognize answer sheets", Icons.Default.PhotoCamera),
        Triple("Grade Exam", "Automatically grade scanned answer sheets", Icons.Default.Grade),
        Triple("Scan History", "View and manage all captured scans", Icons.Default.History),
        Triple("Question Bank", "Create and manage questions & keys", Icons.Default.ListAlt),
        Triple("Results", "View grading results and statistics", Icons.Default.Assessment),
        Triple("Students", "Manage student records and information", Icons.Default.School),
        Triple("Classes", "Organize students into class sections", Icons.Default.Class)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero header with gradient
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = "AI Exam Grader",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fast scanning, automatic grading and useful reports.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
            
            // Quick Stats
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
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
                            .shadow(elevation, shape = MaterialTheme.shapes.medium)
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
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Card(
                                    modifier = Modifier.size(56.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        imageVector = feature.third,
                                        contentDescription = feature.first,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = feature.first,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = feature.second,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                                contentDescription = "Go",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
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
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}
