package com.example.myapplication.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.min
import kotlin.math.max

object TextRecognitionHelper {
    /**
     * Recognize text (including handwriting) from an image Uri as a suspend function.
     * Preprocesses image to improve handwriting recognition accuracy.
     */
    suspend fun recognizeImage(context: Context, imageUri: Uri): String =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            var processedBitmap: Bitmap? = null
            try {
                // Preprocess image for better handwriting recognition
                processedBitmap = preprocessImageForHandwriting(context, imageUri)
                val image = InputImage.fromBitmap(processedBitmap, 0)
                
                // Use ML Kit Text Recognition with default options
                // Note: For best handwriting results, ensure good lighting and contrast when capturing
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val task = recognizer.process(image)
                task.addOnSuccessListener { visionText ->
                    processedBitmap?.recycle() // Clean up after success
                    if (!cont.isCompleted) cont.resume(visionText.text) {}
                }
                task.addOnFailureListener { e ->
                    processedBitmap?.recycle() // Clean up after failure
                    android.util.Log.e("TextRecognition", "ML Kit recognition error", e)
                    if (!cont.isCompleted) cont.resume("ERROR: ${e.message}") {}
                }
            } catch (e: OutOfMemoryError) {
                processedBitmap?.recycle()
                android.util.Log.e("TextRecognition", "Out of memory during preprocessing", e)
                if (!cont.isCompleted) cont.resume("ERROR: Image too large. Please try with a smaller image.") {}
            } catch (e: Exception) {
                processedBitmap?.recycle()
                android.util.Log.e("TextRecognition", "Error during preprocessing", e)
                if (!cont.isCompleted) cont.resume("ERROR: ${e.message}") {}
            }
        }

    /**
     * Simple preprocessing for better text recognition:
     * 1. Resize if needed (optimal size for ML Kit)
     * 2. Slight contrast enhancement (using Android's built-in ColorMatrix)
     * 
     * Note: ML Kit works best with natural, minimally processed images.
     * Over-processing can destroy text features and reduce accuracy.
     */
    private fun preprocessImageForHandwriting(context: Context, imageUri: Uri): Bitmap {
        try {
            // Load the original bitmap
            val originalBitmap = loadBitmap(context, imageUri)
                ?: throw IllegalArgumentException("Cannot load image from URI")

            // Resize to optimal size for ML Kit (not too small, not too large)
            val processedBitmap = resizeForOCR(originalBitmap)
            
            // Apply gentle contrast enhancement
            val enhancedBitmap = enhanceContrastSimple(processedBitmap)
            
            // Clean up intermediate bitmap if different
            if (processedBitmap != originalBitmap && processedBitmap != enhancedBitmap) {
                processedBitmap.recycle()
            }
            if (originalBitmap != processedBitmap) {
                originalBitmap.recycle()
            }
            
            return enhancedBitmap
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("TextRecognition", "Out of memory during preprocessing", e)
            throw e
        }
    }

    private fun loadBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            if (imageUri.scheme == "file") {
                // Load with full quality for best text recognition
                BitmapFactory.decodeFile(imageUri.path)
            } else {
                context.contentResolver.openInputStream(imageUri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TextRecognition", "Error loading bitmap", e)
            null
        }
    }

    /**
     * Resize to optimal dimensions for ML Kit OCR
     * ML Kit works best with images between 640x480 and 4096x4096
     */
    private fun resizeForOCR(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Optimal range for ML Kit
        val minDimension = 1024
        val maxDimension = 2048
        
        // If image is too small, don't upscale (upscaling reduces quality)
        if (width < minDimension && height < minDimension) {
            return bitmap
        }
        
        // If image is in optimal range, keep as is
        if (width <= maxDimension && height <= maxDimension && 
            (width >= minDimension || height >= minDimension)) {
            return bitmap
        }
        
        // Calculate scale factor
        val scale = if (width > maxDimension || height > maxDimension) {
            // Too large - scale down
            min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        } else {
            // Too small - scale up slightly
            max(minDimension.toFloat() / width, minDimension.toFloat() / height)
        }
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Simple contrast enhancement using Android ColorMatrix
     * Much faster than pixel-by-pixel operations
     */
    private fun enhanceContrastSimple(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint()
        
        // Create color matrix for gentle contrast enhancement
        val colorMatrix = android.graphics.ColorMatrix()
        
        // Increase contrast slightly (1.2x) and brightness slightly
        val contrast = 1.2f
        val brightness = 10f
        
        colorMatrix.set(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return output
    }
}
