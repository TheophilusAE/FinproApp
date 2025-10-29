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
    var isProcessing by remember { mutableStateOf(false) }
    var captureSuccess by remember { mutableStateOf(false) }
    var showTips by remember { mutableStateOf(true) }
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
                    val text = TextRecognitionHelper.recognizeImage(context, it)
                    
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
                    Column {
                        Text(
                            text = "Document Scanner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Position paper in frame",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Tips toggle button
                    IconButton(
                        onClick = { showTips = !showTips },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Toggle Tips",
                            tint = Color.White
                        )
                    }
                }
            }

            // Mode Switcher (Camera vs Upload)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { scanMode = "camera" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (scanMode == "camera") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Camera",
                        color = if (scanMode == "camera") 
                            Color.White 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = { scanMode = "upload" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (scanMode == "upload") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Upload",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upload File",
                        color = if (scanMode == "upload") 
                            Color.White 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tips Card (Collapsible)
            AnimatedVisibility(
                visible = showTips,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scanning Tips",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Use good lighting (natural light is best)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Clear, dark handwriting on white paper",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Hold phone directly above paper (no angle)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Avoid shadows and glare on the text",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Camera Preview with Frame Overlay OR Upload Options
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                if (scanMode == "camera") {
                    // Camera Mode
                    val previewView = remember { PreviewView(context) }

                    // Camera Preview
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                        .setFlashMode(ImageCapture.FLASH_MODE_AUTO) // Auto flash for better lighting
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

                        // Frame Guide Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp)
                                .border(
                                    BorderStroke(3.dp, Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )

                        // Corner markers
                        val cornerSize = 40.dp
                        val cornerThickness = 4.dp
                        
                        // Top-left corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(28.dp)
                                .size(cornerSize)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(cornerSize)
                                    .height(cornerThickness)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .width(cornerThickness)
                                    .height(cornerSize)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        
                        // Top-right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(28.dp)
                                .size(cornerSize)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(cornerSize)
                                    .height(cornerThickness)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(cornerThickness)
                                    .height(cornerSize)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        
                        // Bottom-left corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(28.dp)
                                .size(cornerSize)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(cornerSize)
                                    .height(cornerThickness)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(cornerThickness)
                                    .height(cornerSize)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        
                        // Bottom-right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(28.dp)
                                .size(cornerSize)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(cornerSize)
                                    .height(cornerThickness)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(cornerThickness)
                                    .height(cornerSize)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                // Processing Overlay
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clip(RoundedCornerShape(16.dp)),
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
                                text = "Processing...",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (scanMode == "camera") "Recognizing handwritten text" else "Extracting text from document",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                } else {
                    // Upload Mode
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                                text = "Upload Document or Image",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Choose from gallery or select a document file",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
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

            // Bottom Control Panel
            if (scanMode == "camera") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Capture Button
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
                                            
                                            // Use proper coroutine scope with IO dispatcher for heavy processing
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                try {
                                                    val text =
                                                        TextRecognitionHelper.recognizeImage(
                                                            context,
                                                            uri
                                                        )
                                                    val id = java.util.UUID.randomUUID().toString()
                                                    Repository.saveScan(
                                                        com.example.myapplication.data.ScanRecord(
                                                            id = id,
                                                            filePath = file.absolutePath,
                                                            recognizedText = text,
                                                            timestamp = System.currentTimeMillis()
                                                        )
                                                    )
                                                    
                                                    // Switch to Main dispatcher for UI updates
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        recognizedText = text
                                                        captureSuccess = true
                                                        isProcessing = false
                                                    }
                                                } catch (e: OutOfMemoryError) {
                                                    android.util.Log.e("ScanScreen", "Out of memory during processing", e)
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        recognizedText = "Error: Image too large. Please try with a smaller image."
                                                        captureSuccess = false
                                                        isProcessing = false
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("ScanScreen", "Error processing image", e)
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        recognizedText = "Error: ${e.message}"
                                                        captureSuccess = false
                                                        isProcessing = false
                                                    }
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            recognizedText = "Capture failed: ${exception.message}"
                                            isProcessing = false
                                            captureSuccess = false
                                            isCapturing = false
                                        }
                                    })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer ring with pulse animation
                        if (!isProcessing) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(pulseScale)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                        
                        // Middle ring
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        
                        // Inner circle
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Color.White,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Capture",
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isProcessing) "Processing..." else "Tap to Scan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (isProcessing) 0.5f else 1f
                        )
                    )

                    // Success/Error Messages
                    AnimatedVisibility(
                        visible = captureSuccess && !isProcessing,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Scan Successful!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Saved to scan history",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Recognized Text Preview (Shared between Camera and Upload modes)
        recognizedText?.let { text ->
            if (text.isNotEmpty() && !text.startsWith("ERROR") && !text.startsWith("Error")) {
                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically() + fadeIn()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recognized Text",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = { navController.navigate("scanhistory") }
                                    ) {
                                        Text("View All")
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = text.take(100) + if (text.length > 100) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
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
