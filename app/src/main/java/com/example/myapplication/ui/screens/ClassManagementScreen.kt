package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ClassSection
import com.example.myapplication.data.Repository
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    repository: Repository,
    teacherId: String,
    onNavigateBack: () -> Unit
) {
    var classes by remember { mutableStateOf(repository.loadClassSections()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassSection?>(null) }
    var viewingClass by remember { mutableStateOf<ClassSection?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<ClassSection?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Class Management", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(tween(500)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Add, "Add Class")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Card
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically() + fadeIn()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ClassStatItem(
                                icon = Icons.Default.Class,
                                value = classes.size.toString(),
                                label = "Classes"
                            )
                            ClassStatItem(
                                icon = Icons.Default.People,
                                value = classes.sumOf { it.studentCount }.toString(),
                                label = "Total Students"
                            )
                            ClassStatItem(
                                icon = Icons.Default.Groups,
                                value = if (classes.isNotEmpty()) 
                                    (classes.sumOf { it.studentCount }.toDouble() / classes.size).toInt().toString()
                                else "0",
                                label = "Avg Size"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Class List
            if (classes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No classes yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Create your first class to organize students",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(classes) { index, classSection ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 50L)
                            itemVisible = true
                        }

                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = slideInHorizontally(tween(300)) + fadeIn()
                        ) {
                            ClassCard(
                                classSection = classSection,
                                onView = { viewingClass = classSection },
                                onEdit = { editingClass = classSection },
                                onDelete = { showDeleteConfirm = classSection }
                            )
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AddEditClassDialog(
                classSection = null,
                teacherId = teacherId,
                onDismiss = { showAddDialog = false },
                onSave = { newClass ->
                    repository.saveClassSection(newClass)
                    classes = repository.loadClassSections()
                    showAddDialog = false
                }
            )
        }

        // Edit Dialog
        editingClass?.let { cls ->
            AddEditClassDialog(
                classSection = cls,
                teacherId = teacherId,
                onDismiss = { editingClass = null },
                onSave = { updatedClass ->
                    repository.saveClassSection(updatedClass)
                    classes = repository.loadClassSections()
                    editingClass = null
                }
            )
        }

        // View Students Dialog
        viewingClass?.let { cls ->
            ViewClassStudentsDialog(
                classSection = cls,
                students = repository.getStudentsByClass(cls.id),
                onDismiss = { viewingClass = null }
            )
        }

        // Delete Confirmation
        showDeleteConfirm?.let { cls ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Class") },
                text = { 
                    Text("Are you sure you want to delete ${cls.name}? " +
                         "This will unassign ${cls.studentCount} student(s) from this class.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Unassign students from this class
                            val students = repository.getStudentsByClass(cls.id)
                            students.forEach { student ->
                                repository.saveStudent(student.copy(classSection = null))
                            }
                            
                            repository.deleteClassSection(cls.id)
                            classes = repository.loadClassSections()
                            showDeleteConfirm = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ClassStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun ClassCard(
    classSection: ClassSection,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Class,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classSection.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (classSection.description != null) {
                        Text(
                            text = classSection.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "${classSection.studentCount} students",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onView) {
                        Icon(
                            Icons.Default.RemoveRedEye,
                            contentDescription = "View",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
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

@Composable
private fun AddEditClassDialog(
    classSection: ClassSection?,
    teacherId: String,
    onDismiss: () -> Unit,
    onSave: (ClassSection) -> Unit
) {
    var name by remember { mutableStateOf(classSection?.name ?: "") }
    var description by remember { mutableStateOf(classSection?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Class, null) },
        title = { Text(if (classSection == null) "Add Class" else "Edit Class") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Class Name") },
                    placeholder = { Text("e.g., CS101-A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g., Introduction to Computer Science") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newClass = ClassSection(
                            id = classSection?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            teacherId = teacherId,
                            studentCount = classSection?.studentCount ?: 0,
                            createdAt = classSection?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(newClass)
                    }
                },
                enabled = name.isNotBlank()
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

@Composable
private fun ViewClassStudentsDialog(
    classSection: ClassSection,
    students: List<com.example.myapplication.data.Student>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.People, null) },
        title = { 
            Column {
                Text(classSection.name)
                Text(
                    "${students.size} student(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        text = {
            if (students.isEmpty()) {
                Text(
                    "No students assigned to this class yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(students) { index, student ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = student.studentNumber,
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
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
