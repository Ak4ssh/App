package com.example.lungcancerdetector.ui.main

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.lungcancerdetector.data.Classifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PredictionResult(
    val label: String,
    val acaConfidence: Float,
    val benignConfidence: Float,
    val sccConfidence: Float
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
  private val classifier = Classifier(application)
  
  private val _result = MutableStateFlow<PredictionResult?>(null)
  val result: StateFlow<PredictionResult?> = _result

  private val _loading = MutableStateFlow(false)
  val loading: StateFlow<Boolean> = _loading

  fun analyzeImage(bitmap: Bitmap) {
    _loading.value = true
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
    _loading.value = false
  }
}
