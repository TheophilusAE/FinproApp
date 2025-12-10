package com.example.myapplication.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.ocr.TextRecognitionHelper
import com.example.myapplication.data.Repository
import com.example.myapplication.utils.DocumentProcessor
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush

@Composable
fun ScanScreen(navController: NavController, cameraPermissionGranted: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var recognizedText by remember { mutableStateOf<String?>(null) }
    var scanAccuracy by remember { mutableStateOf<Float?>(null) }
    var confidenceLevel by remember { mutableStateOf("Medium") }
    var isProcessing by remember { mutableStateOf(false) }
    var captureSuccess by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var scanMode by remember { mutableStateOf("camera") } // "camera" or "upload"
    
    // File picker launcher for documents
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessing = true
            recognizedText = null
            
            scope.launch(Dispatchers.IO) {
                try {
                    val text = DocumentProcessor.extractTextFromDocument(context, it)
                    
                    val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    
                    Repository.saveScan(
                        com.example.myapplication.data.ScanRecord(
                            id = java.util.UUID.randomUUID().toString(),
                            filePath = it.toString(),
                            recognizedText = text,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    
                    withContext(Dispatchers.Main) {
                        recognizedText = text
                        captureSuccess = true
                        isProcessing = false
                    }
                } catch (e: Exception) {
                    Log.e("ScanScreen", "Error processing document", e)
                    withContext(Dispatchers.Main) {
                        recognizedText = "Error: ${e.message}"
                        captureSuccess = false
                        isProcessing = false
                    }
                }
            }
        }
    }
    
    // Image picker launcher for photos
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessing = true
            recognizedText = null
            
            scope.launch(Dispatchers.IO) {
                try {
                    val result = TextRecognitionHelper.recognizeImage(context, it)
                    
                    Repository.saveScan(
                        com.example.myapplication.data.ScanRecord(
                            id = java.util.UUID.randomUUID().toString(),
                            filePath = it.toString(),
                            recognizedText = result.text,
                            timestamp = System.currentTimeMillis(),
                            accuracy = result.accuracy,
                            confidenceLevel = result.confidenceLevel,
                            enhancedByAI = false
                        )
                    )
                    
                    withContext(Dispatchers.Main) {
                        recognizedText = result.text
                        scanAccuracy = result.accuracy
                        confidenceLevel = result.confidenceLevel
                        captureSuccess = true
                        isProcessing = false
                    }
                } catch (e: Exception) {
                    Log.e("ScanScreen", "Error processing image", e)
                    withContext(Dispatchers.Main) {
                        recognizedText = "Error: ${e.message}"
                        captureSuccess = false
                        isProcessing = false
                    }
                }
            }
        }
    }
    
    // Initialize Repository
    LaunchedEffect(Unit) {
        Repository.init(context)
    }

    // Animation states
    val pulseAnimation = rememberInfiniteTransition()
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (!cameraPermissionGranted) {
            // Permission denied state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please grant camera permission to scan answer sheets",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            return@Box
        }

        // Main content
        if (scanMode == "camera") {
            // Full screen camera mode with overlay UI
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera Preview - Full Screen
                val previewView = remember { PreviewView(context) }
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                ) { view ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
                            .build()
                            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setTargetRotation(android.view.Surface.ROTATION_0)
                            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e("ScanScreen", "Camera bind failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
                
                // Top Bar with gradient (Overlay)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Back button
                            IconButton(
                                onClick = { navController.navigateUp() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Handwriting Scanner",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Scans text, essays & paragraphs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        
                        // Mode switch button
                        IconButton(
                            onClick = { scanMode = "upload" },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Switch to Upload",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Capture Button - Bottom Center with backdrop gradient
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val captureButtonScale by animateFloatAsState(
                    targetValue = if (isCapturing) 0.9f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(captureButtonScale)
                        .clickable(enabled = !isProcessing) {
                            isCapturing = true
                            val ic = imageCapture
                            if (ic == null) {
                                isCapturing = false
                                return@clickable
                            }
                            isProcessing = true

                            val file = File.createTempFile("scan_", ".jpg", context.cacheDir)
                            val outputOptions =
                                ImageCapture.OutputFileOptions.Builder(file).build()

                            ic.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val uri: Uri = Uri.fromFile(file)
                                        isCapturing = false
                                        
                                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            try {
                                                val result =
                                                    TextRecognitionHelper.recognizeImage(
                                                        context,
                                                        uri
                                                    )
                                                val id = java.util.UUID.randomUUID().toString()
                                                Repository.saveScan(
                                                    com.example.myapplication.data.ScanRecord(
                                                        id = id,
                                                        filePath = file.absolutePath,
                                                        recognizedText = result.text,
                                                        timestamp = System.currentTimeMillis(),
                                                        accuracy = result.accuracy,
                                                        confidenceLevel = result.confidenceLevel,
                                                        enhancedByAI = false
                                                    )
                                                )
                                                
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    recognizedText = result.text
                                                    scanAccuracy = result.accuracy
                                                    confidenceLevel = result.confidenceLevel
                                                    captureSuccess = true
                                                    isProcessing = false
                                                }
                                            } catch (e: OutOfMemoryError) {
                                                android.util.Log.e(
                                                    "ScanScreen",
                                                    "Out of memory during processing",
                                                    e
                                                )
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    recognizedText =
                                                        "Error: Image too large. Please try with a smaller image."
                                                    captureSuccess = false
                                                    isProcessing = false
                                                    isCapturing = false
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("ScanScreen", "Error", e)
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    recognizedText = "Error: ${e.message}"
                                                    captureSuccess = false
                                                    isProcessing = false
                                                    isCapturing = false
                                                }
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        android.util.Log.e("ScanScreen", "Capture failed", exception)
                                        isCapturing = false
                                        isProcessing = false
                                        recognizedText = "Error: Capture failed"
                                        captureSuccess = false
                                    }
                                })
                        }
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                        .border(
                            BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Capture",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isProcessing) "Processing..." else "Tap to Scan",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            // Processing Overlay
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = Color.White,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Scanning Handwriting...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "AI is recognizing text (English & Indonesian)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            }
        } else {
            // UPLOAD MODE - Column layout
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar with gradient
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Back button
                            IconButton(
                                onClick = { navController.navigateUp() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Handwriting Scanner",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Upload handwritten text or documents",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        
                        // Mode switch button
                        IconButton(
                            onClick = { scanMode = "camera" },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Switch to Camera",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Upload Options
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Upload Handwritten Text",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Scans essays, paragraphs, short answers & MCQs\nEnglish & Indonesian supported",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Info card about capabilities
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "AI-Powered Recognition",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "85-98% accuracy • Any text length",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Upload buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { imagePicker.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isProcessing
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Image",
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Photo/Image")
                                    }
                                }
                                
                                OutlinedButton(
                                    onClick = { 
                                        documentPicker.launch("application/pdf")
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isProcessing
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "PDF",
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("PDF File")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedButton(
                                onClick = { 
                                    documentPicker.launch("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isProcessing
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Word",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Word Document (.docx)")
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Supported formats: JPG, PNG, PDF, DOCX",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        // Recognized Text Preview - Fullscreen (Shared between Camera and Upload modes)
        recognizedText?.let { text ->
            if (text.isNotEmpty() && !text.startsWith("ERROR") && !text.startsWith("Error")) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fullscreen results background
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Header with close button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Recognized Text",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (scanAccuracy != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (confidenceLevel) {
                                                        "High" -> Icons.Default.VerifiedUser
                                                        "Medium" -> Icons.Default.CheckCircle
                                                        else -> Icons.Default.Warning
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = when (confidenceLevel) {
                                                        "High" -> Color(0xFF4CAF50)
                                                        "Medium" -> Color(0xFFFFA726)
                                                        else -> Color(0xFFF44336)
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "$confidenceLevel Confidence • ${String.format("%.1f%%", scanAccuracy)} Accuracy",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = when (confidenceLevel) {
                                                        "High" -> Color(0xFF4CAF50)
                                                        "Medium" -> Color(0xFFFFA726)
                                                        else -> Color(0xFFF44336)
                                                    },
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                    IconButton(onClick = { 
                                        recognizedText = null
                                        scanAccuracy = null
                                        confidenceLevel = "Medium"
                                    }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Close",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Accuracy indicator card (if available)
                                if (scanAccuracy != null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (confidenceLevel) {
                                                "High" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                "Medium" -> Color(0xFFFFA726).copy(alpha = 0.1f)
                                                else -> Color(0xFFF44336).copy(alpha = 0.1f)
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Scan Quality",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = when (confidenceLevel) {
                                                        "High" -> "Excellent recognition"
                                                        "Medium" -> "Good recognition"
                                                        else -> "Consider rescanning"
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            
                                            // Circular progress indicator
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.size(56.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = (scanAccuracy!! / 100f).coerceIn(0f, 1f),
                                                    modifier = Modifier.size(56.dp),
                                                    strokeWidth = 5.dp,
                                                    color = when (confidenceLevel) {
                                                        "High" -> Color(0xFF4CAF50)
                                                        "Medium" -> Color(0xFFFFA726)
                                                        else -> Color(0xFFF44336)
                                                    },
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                Text(
                                                    text = "${scanAccuracy!!.toInt()}%",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (confidenceLevel) {
                                                        "High" -> Color(0xFF4CAF50)
                                                        "Medium" -> Color(0xFFFFA726)
                                                        else -> Color(0xFFF44336)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Scrollable text content
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Action button
                                Button(
                                    onClick = { navController.navigate("scanhistory") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View All Scans")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
