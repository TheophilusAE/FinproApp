package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DocumentProcessor {
    
    private var isPDFBoxInitialized = false
    
    /**
     * Initialize PDFBox (required for PDF processing on Android)
     */
    private fun initPDFBox(context: Context) {
        if (!isPDFBoxInitialized) {
            try {
                PDFBoxResourceLoader.init(context)
                isPDFBoxInitialized = true
            } catch (e: Exception) {
                Log.e("DocumentProcessor", "Failed to initialize PDFBox", e)
            }
        }
    }
    
    /**
     * Extract text from a document (PDF or DOCX)
     */
    suspend fun extractTextFromDocument(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val mimeType = context.contentResolver.getType(uri)
            val fileName = getFileName(context, uri)
            
            Log.d("DocumentProcessor", "Processing file: $fileName, MIME type: $mimeType")
            
            return@withContext when {
                mimeType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true) -> {
                    extractTextFromPDF(context, uri)
                }
                mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" 
                    || fileName.endsWith(".docx", ignoreCase = true) -> {
                    extractTextFromDOCX(context, uri)
                }
                else -> {
                    "Error: Unsupported file format. Please upload PDF or DOCX files."
                }
            }
        } catch (e: Exception) {
            Log.e("DocumentProcessor", "Error extracting text from document", e)
            "Error: Failed to process document - ${e.message}"
        }
    }
    
    /**
     * Extract text from PDF file
     */
    private fun extractTextFromPDF(context: Context, uri: Uri): String {
        initPDFBox(context)
        
        var document: PDDocument? = null
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                
                return if (text.isNotBlank()) {
                    text.trim()
                } else {
                    "No text found in PDF. The document may contain only images."
                }
            } ?: return "Error: Unable to open PDF file"
        } catch (e: Exception) {
            Log.e("DocumentProcessor", "Error processing PDF", e)
            return "Error processing PDF: ${e.message}"
        } finally {
            document?.close()
        }
    }
    
    /**
     * Extract text from DOCX file (Word 2007+)
     */
    private fun extractTextFromDOCX(context: Context, uri: Uri): String {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = XWPFDocument(inputStream)
                val text = StringBuilder()
                
                // Extract text from paragraphs
                document.paragraphs.forEach { paragraph ->
                    text.append(paragraph.text)
                    text.append("\n")
                }
                
                // Extract text from tables
                document.tables.forEach { table ->
                    table.rows.forEach { row ->
                        row.tableCells.forEach { cell ->
                            text.append(cell.text)
                            text.append("\t")
                        }
                        text.append("\n")
                    }
                }
                
                document.close()
                
                return if (text.isNotBlank()) {
                    text.toString().trim()
                } else {
                    "No text found in Word document."
                }
            } ?: return "Error: Unable to open Word document"
        } catch (e: Exception) {
            Log.e("DocumentProcessor", "Error processing DOCX", e)
            return "Error processing Word document: ${e.message}"
        }
    }
    
    /**
     * Get file name from URI
     */
    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
}
