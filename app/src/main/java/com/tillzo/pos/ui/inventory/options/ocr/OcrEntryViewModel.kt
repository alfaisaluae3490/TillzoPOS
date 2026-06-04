package com.tillzo.pos.ui.inventory.options.ocr

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtractedOcrData(
    val weightUnit: String,
    val rawText: String
)

@HiltViewModel
class OcrEntryViewModel @Inject constructor() : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // M6.2: Regex to match standard weights (e.g., "500g", "1.5 kg", "200 ml")
    private val weightRegex = Regex("""\d+(\.\d+)?\s*(mg|g|kg|ml|l|oz)""", RegexOption.IGNORE_CASE)

    private val _extractedData = MutableStateFlow<ExtractedOcrData?>(null)
    val extractedData: StateFlow<ExtractedOcrData?> = _extractedData.asStateFlow()

    private var isProcessing = false

    @SuppressLint("UnsafeOptInUsageError")
    fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isProcessing) {
            isProcessing = true
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    parseText(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e("OcrViewModel", "OCR Failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    isProcessing = false
                }
        } else {
            imageProxy.close()
        }
    }

    private fun parseText(rawText: String) {
        val matchResult = weightRegex.find(rawText)
        if (matchResult != null) {
            val weightStr = matchResult.value.trim()
            _extractedData.value = ExtractedOcrData(weightStr, rawText)
        }
        
        // Note: For M6.3 AI Vision Api Fallback, if OCR doesn't find a match after N seconds,
        // we would compress the ImageProxy to JPEG Base64 and send it to VisionApiService.
        // Due to strict RAM constraints per architecture, we only hold the image in memory 
        // long enough for the upload stream, then discard it immediately.
    }

    fun reset() {
        _extractedData.value = null
    }
}
