package com.example.lungcancerdetector.ui.main

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.lungcancerdetector.data.Classifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
  private val classifier = Classifier(application)
  
  private val _result = MutableStateFlow("")
  val result: StateFlow<String> = _result

  fun analyzeImage(bitmap: Bitmap) {
    val out = classifier.analyze(bitmap)
    val p1 = (out[0] * 100).toInt()
    val p2 = (out[1] * 100).toInt()
    val p3 = (out[2] * 100).toInt()
    
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
    
    _result.value = "Prediction: $label\nACA: $p1% | Benign: $p2% | SCC: $p3%"
  }
}
