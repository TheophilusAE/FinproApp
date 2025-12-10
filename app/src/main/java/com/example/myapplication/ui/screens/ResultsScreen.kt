package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.Repository
import com.example.myapplication.data.ExamResult

@Composable
fun ResultsScreen(navController: NavController) {
    val allResults = remember { mutableStateListOf<ExamResult>() }
    var filteredResults by remember { mutableStateOf<List<ExamResult>>(emptyList()) }
    var selectedExamId by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var deleteConfirmResult by remember { mutableStateOf<ExamResult?>(null) }

    LaunchedEffect(Unit) {
        allResults.clear()
        allResults.addAll(Repository.loadExamResults())
        filteredResults = allResults.toList()
    }
    
    LaunchedEffect(selectedExamId) {
        filteredResults = if (selectedExamId == null) {
            allResults.toList()
        } else {
            allResults.filter { it.examId == selectedExamId }
        }
    }
    
    val uniqueExams = allResults.map { it.examId }.distinct()
    val average = if (filteredResults.isEmpty()) 0.0 else filteredResults.map { it.totalScore }.average()
    val maxScore = if (filteredResults.isEmpty()) 0.0 else filteredResults.maxOf { it.totalScore }
    val minScore = if (filteredResults.isEmpty()) 0.0 else filteredResults.minOf { it.totalScore }
    val passingCount = filteredResults.count { it.totalScore >= 60.0 }
    val passingRate = if (filteredResults.isEmpty()) 0.0 else (passingCount.toDouble() / filteredResults.size) * 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                        Column {
                            Text(
                                text = "Grading Results",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${filteredResults.size} exam${if (filteredResults.size != 1) "s" else ""} • View statistics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showFilters = !showFilters },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filter")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            // Filter chips
            item {
                AnimatedVisibility(visible = showFilters && uniqueExams.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Filter by Exam",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedExamId == null,
                                    onClick = { selectedExamId = null },
                                    label = { Text("All Exams") }
                                )
                            }
                            items(uniqueExams) { examId ->
                                FilterChip(
                                    selected = selectedExamId == examId,
                                    onClick = { selectedExamId = examId },
                                    label = { Text(examId) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            // Enhanced Statistics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Average",
                        value = String.format("%.1f", average),
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Highest",
                        value = String.format("%.1f", maxScore),
                        icon = Icons.Default.Star,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                // Passing Rate Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (passingRate >= 70.0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = "Passing Rate",
                                    tint = if (passingRate >= 70.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Passing Rate",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$passingCount of ${filteredResults.size} students (≥60%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = String.format("%.0f%%", passingRate),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (passingRate >= 70.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (passingRate / 100.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (passingRate >= 70.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
            
            // Score Distribution
            item {
                if (filteredResults.isNotEmpty()) {
                    ScoreDistributionCard(filteredResults)
                }
            }
            
            // Individual Results
            item {
                Text(
                    text = "Individual Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(filteredResults) { r ->
                ResultCard(
                    result = r,
                    onDelete = { deleteConfirmResult = r }
                )
            }
            
            // Empty state
            if (filteredResults.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (allResults.isEmpty()) "No results yet" else "No results match filter",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (allResults.isEmpty()) "Grade some exams to see results" else "Try a different filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    deleteConfirmResult?.let { result ->
        AlertDialog(
            onDismissRequest = { deleteConfirmResult = null },
            title = { Text("Delete Result?") },
            text = { 
                Text("Are you sure you want to delete the result for student ${result.studentId} in exam ${result.examId}? This action cannot be undone.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        Repository.deleteExamResult(result.studentId, result.examId)
                        allResults.removeAll { it.studentId == result.studentId && it.examId == result.examId }
                        filteredResults = if (selectedExamId == null) {
                            allResults.toList()
                        } else {
                            allResults.filter { it.examId == selectedExamId }
                        }
                        deleteConfirmResult = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmResult = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScoreDistributionCard(results: List<ExamResult>) {
    val ranges = listOf(
        "90-100" to results.count { it.totalScore >= 90.0 },
        "80-89" to results.count { it.totalScore in 80.0..89.9 },
        "70-79" to results.count { it.totalScore in 70.0..79.9 },
        "60-69" to results.count { it.totalScore in 60.0..69.9 },
        "Below 60" to results.count { it.totalScore < 60.0 }
    )
    val maxCount = ranges.maxOfOrNull { it.second } ?: 1

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Score Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            ranges.forEach { (range, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = range,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    LinearProgressIndicator(
                        progress = { if (maxCount > 0) count.toFloat() / maxCount else 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp),
                        color = when {
                            range.startsWith("90") -> MaterialTheme.colorScheme.primary
                            range.startsWith("80") -> MaterialTheme.colorScheme.tertiary
                            range.startsWith("70") -> MaterialTheme.colorScheme.secondary
                            range.startsWith("60") -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.error
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: ExamResult,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.studentId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Exam: ${result.examId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        result.totalScore >= 90 -> MaterialTheme.colorScheme.primaryContainer
                        result.totalScore >= 70 -> MaterialTheme.colorScheme.tertiaryContainer
                        result.totalScore >= 60 -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.1f", result.totalScore),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            result.totalScore >= 90 -> MaterialTheme.colorScheme.onPrimaryContainer
                            result.totalScore >= 70 -> MaterialTheme.colorScheme.onTertiaryContainer
                            result.totalScore >= 60 -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = when {
                            result.totalScore >= 90 -> "A"
                            result.totalScore >= 80 -> "B"
                            result.totalScore >= 70 -> "C"
                            result.totalScore >= 60 -> "D"
                            else -> "F"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            result.totalScore >= 90 -> MaterialTheme.colorScheme.onPrimaryContainer
                            result.totalScore >= 70 -> MaterialTheme.colorScheme.onTertiaryContainer
                            result.totalScore >= 60 -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Delete Result",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
