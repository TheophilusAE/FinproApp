package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ClassSection
import com.example.myapplication.data.Repository
import com.example.myapplication.data.Student
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    repository: Repository,
    onNavigateBack: () -> Unit
) {
    var students by remember { mutableStateOf(emptyList<Student>()) }
    var classSections by remember { mutableStateOf(emptyList<ClassSection>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Student?>(null) }
    var visible by remember { mutableStateOf(false) }

    // Reload students every time screen becomes visible
    DisposableEffect(Unit) {
        students = repository.loadStudents()
        classSections = repository.loadClassSections()
        android.util.Log.d("StudentManagement", "Loaded ${students.size} students")
        
        onDispose { }
    }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Student Management", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Add Student",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Card with Stats
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
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(
                                icon = Icons.Default.Person,
                                value = students.size.toString(),
                                label = "Students"
                            )
                            StatItem(
                                icon = Icons.Default.Class,
                                value = classSections.size.toString(),
                                label = "Classes"
                            )
                            StatItem(
                                icon = Icons.Default.People,
                                value = students.filter { it.classSection != null }.size.toString(),
                                label = "Assigned"
                            )
                        }
                    }
                }
            }

            // Search and Filter
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(300, 100)) + fadeIn()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search students...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        },
                        singleLine = true
                    )

                    if (classSections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedClassFilter == null,
                                onClick = { selectedClassFilter = null },
                                label = { Text("All") }
                            )
                            classSections.take(3).forEach { cls ->
                                FilterChip(
                                    selected = selectedClassFilter == cls.id,
                                    onClick = { 
                                        selectedClassFilter = if (selectedClassFilter == cls.id) null else cls.id 
                                    },
                                    label = { Text(cls.name) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student List
            val filteredStudents = students.filter { student ->
                val matchesSearch = searchQuery.isEmpty() || 
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.studentNumber.contains(searchQuery, ignoreCase = true) ||
                    student.email.contains(searchQuery, ignoreCase = true)
                
                val matchesClass = selectedClassFilter == null || student.classSection == selectedClassFilter
                
                matchesSearch && matchesClass
            }

            if (filteredStudents.isEmpty()) {
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
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Text(
                            text = if (searchQuery.isEmpty() && selectedClassFilter == null) 
                                "No students yet" 
                            else 
                                "No matching students",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (searchQuery.isEmpty() && selectedClassFilter == null) {
                            Text(
                                text = "Add your first student to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredStudents) { index, student ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 50L)
                            itemVisible = true
                        }

                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = slideInHorizontally(tween(300)) + fadeIn()
                        ) {
                            StudentCard(
                                student = student,
                                className = classSections.firstOrNull { it.id == student.classSection }?.name,
                                onEdit = { editingStudent = student },
                                onDelete = { showDeleteConfirm = student }
                            )
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AddEditStudentDialog(
                student = null,
                classes = classSections,
                onDismiss = { showAddDialog = false },
                onSave = { newStudent ->
                    repository.saveStudent(newStudent)
                    students = repository.loadStudents()
                    // Update class student count
                    newStudent.classSection?.let { classId ->
                        repository.updateClassStudentCount(classId)
                        classSections = repository.loadClassSections()
                    }
                    showAddDialog = false
                }
            )
        }

        // Edit Dialog
        editingStudent?.let { student ->
            AddEditStudentDialog(
                student = student,
                classes = classSections,
                onDismiss = { editingStudent = null },
                onSave = { updatedStudent ->
                    val oldClassId = student.classSection
                    val newClassId = updatedStudent.classSection
                    
                    repository.saveStudent(updatedStudent)
                    students = repository.loadStudents()
                    
                    // Update student counts for affected classes
                    if (oldClassId != newClassId) {
                        oldClassId?.let { repository.updateClassStudentCount(it) }
                        newClassId?.let { repository.updateClassStudentCount(it) }
                        classSections = repository.loadClassSections()
                    }
                    
                    editingStudent = null
                }
            )
        }

        // Delete Confirmation
        showDeleteConfirm?.let { student ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Student") },
                text = { Text("Are you sure you want to delete ${student.name}? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.deleteStudent(student.id)
                            students = repository.loadStudents()
                            student.classSection?.let { classId ->
                                repository.updateClassStudentCount(classId)
                                classSections = repository.loadClassSections()
                            }
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
private fun StatItem(
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
private fun StudentCard(
    student: Student,
    className: String?,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.firstOrNull()?.uppercase() ?: "S",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Student Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = student.studentNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = student.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (className != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(className, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Class, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditStudentDialog(
    student: Student?,
    classes: List<ClassSection>,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var studentNumber by remember { mutableStateOf(student?.studentNumber ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var selectedClassId by remember { mutableStateOf(student?.classSection) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Person, null) },
        title = { Text(if (student == null) "Add Student" else "Edit Student") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = studentNumber,
                    onValueChange = { studentNumber = it },
                    label = { Text("Student Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (classes.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = classes.firstOrNull { it.id == selectedClassId }?.name ?: "No Class",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class Section") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Class") },
                                onClick = {
                                    selectedClassId = null
                                    expanded = false
                                }
                            )
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls.name) },
                                    onClick = {
                                        selectedClassId = cls.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && studentNumber.isNotBlank() && email.isNotBlank()) {
                        val newStudent = Student(
                            id = student?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            studentNumber = studentNumber,
                            email = email,
                            classSection = selectedClassId,
                            createdAt = student?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(newStudent)
                    }
                },
                enabled = name.isNotBlank() && studentNumber.isNotBlank() && email.isNotBlank()
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
