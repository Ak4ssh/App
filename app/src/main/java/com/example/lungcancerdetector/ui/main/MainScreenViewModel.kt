package com.example.lungcancerdetector.ui.main

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.lungcancerdetector.data.Classifier
import com.example.lungcancerdetector.data.ScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PredictionResult(
    val label: String,
    val acaConfidence: Float,
    val benignConfidence: Float,
    val sccConfidence: Float,
    val maxConfidence: Float = maxOf(acaConfidence, benignConfidence, sccConfidence)
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
  private val classifier = Classifier(application)
  
  private val _result = MutableStateFlow<PredictionResult?>(null)
  val result: StateFlow<PredictionResult?> = _result

  private val _loading = MutableStateFlow(false)
  val loading: StateFlow<Boolean> = _loading

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error

  private val _history = MutableStateFlow<List<ScanRecord>>(emptyList())
  val history: StateFlow<List<ScanRecord>> = _history

  fun analyzeImage(bitmap: Bitmap) {
    _loading.value = true
    _error.value = null
    try {
    val out = classifier.analyze(bitmap)
    val pACA = out[0]
    val pBenign = out[1]
    val pSCC = out[2]
    
    var maxIdx = 0
    var maxVal = out[0]
    for (i in 1..2) {
        if (out[i] > maxVal) {
            maxVal = out[i]
            maxIdx = i
        }
    }
    
    val label = when (maxIdx) {
        0 -> "Adenocarcinoma"
        1 -> "Benign"
        2 -> "Squamous Cell Carcinoma"
        else -> "Unknown"
    }
    
    _result.value = PredictionResult(
        label = label,
        acaConfidence = pACA,
        benignConfidence = pBenign,
        sccConfidence = pSCC
    )
    _history.value = _history.value + ScanRecord(
        label = label,
        aca = pACA,
        benign = pBenign,
        scc = pSCC
    )
    } catch (e: Exception) {
        _error.value = "Failed to analyze: ${e.message}"
    }
    _loading.value = false
  }

  override fun onCleared() {
    super.onCleared()
    classifier.close()
  }
}
