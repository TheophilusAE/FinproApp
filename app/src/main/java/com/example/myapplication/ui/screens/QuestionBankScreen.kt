package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.Repository
import com.example.myapplication.data.Question

@Composable
fun QuestionBankScreen(navController: NavController) {
    val questions = remember { mutableStateListOf<Question>() }

    LaunchedEffect(Unit) {
        questions.clear()
        questions.addAll(Repository.loadQuestions())
    }

    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Question?>(null) }

    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { adding = true },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Question",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    androidx.compose.material3.IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Question Bank",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${questions.size} question${if (questions.size != 1) "s" else ""} • Tap + to add more",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                        )
                    }
                }
            }

            if (questions.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.LibraryBooks,
                        contentDescription = "No questions",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No questions yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create your first question",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(questions) { index, q ->
                        var isVisible by remember { mutableStateOf(false) }
                        
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 50L)
                            isVisible = true
                        }
                        
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                        ) {
                            QuestionCard(
                                question = q,
                                onEdit = { editing = q },
                                onDelete = {
                                    val newList = questions.filterNot { it.id == q.id }
                                    questions.clear()
                                    questions.addAll(newList)
                                    Repository.saveQuestions(questions.toList())
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (adding) {
        com.example.myapplication.ui.components.AddQuestionDialog(
            open = adding,
            onDismiss = { adding = false },
            onSave = { q ->
                questions.add(0, q)
                Repository.saveQuestions(questions.toList())
            }
        )
    }

    // Edit dialog
    editing?.let { question ->
        com.example.myapplication.ui.components.EditQuestionDialog(
            question = question,
            onDismiss = { editing = null },
            onSave = { updatedQuestion ->
                val index = questions.indexOfFirst { it.id == updatedQuestion.id }
                if (index != -1) {
                    questions[index] = updatedQuestion
                    Repository.saveQuestions(questions.toList())
                }
            }
        )
    }
}

@Composable
private fun QuestionCard(
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (question.type) {
                                com.example.myapplication.data.QuestionType.MCQ -> MaterialTheme.colorScheme.primaryContainer
                                com.example.myapplication.data.QuestionType.SHORT_TEXT -> MaterialTheme.colorScheme.tertiaryContainer
                                com.example.myapplication.data.QuestionType.LONG_TEXT -> MaterialTheme.colorScheme.secondaryContainer
                                com.example.myapplication.data.QuestionType.ESSAY -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = when (question.type) {
                                com.example.myapplication.data.QuestionType.MCQ -> "Multiple Choice"
                                com.example.myapplication.data.QuestionType.SHORT_TEXT -> "Short Text"
                                com.example.myapplication.data.QuestionType.LONG_TEXT -> "Long Text"
                                com.example.myapplication.data.QuestionType.ESSAY -> "Essay"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = when (question.type) {
                                com.example.myapplication.data.QuestionType.MCQ -> MaterialTheme.colorScheme.onPrimaryContainer
                                com.example.myapplication.data.QuestionType.SHORT_TEXT -> MaterialTheme.colorScheme.onTertiaryContainer
                                com.example.myapplication.data.QuestionType.LONG_TEXT -> MaterialTheme.colorScheme.onSecondaryContainer
                                com.example.myapplication.data.QuestionType.ESSAY -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.2
                    )
                    if (question.options.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Options: ${question.options.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${question.weight} points",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
