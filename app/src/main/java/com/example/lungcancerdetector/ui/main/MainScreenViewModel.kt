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
        0 -> "Type 1"
        1 -> "Type 2"
        2 -> "Type 3"
        else -> "Unknown"
    }
    
    _result.value = "Prediction: $label\nT1: $p1% | T2: $p2% | T3: $p3%"
  }
}
