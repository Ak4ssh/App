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
    val benign = out[0]
    val malignant = out[1]
    
    _result.value = if (malignant > benign) {
        "Prediction: Malignant (${(malignant * 100).toInt()}%)"
    } else {
        "Prediction: Benign (${(benign * 100).toInt()}%)"
    }
  }
}
