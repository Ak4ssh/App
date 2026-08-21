package com.example.lungcancerdetector.ui.main

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel(),
) {
  val context = LocalContext.current
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  val result by viewModel.result.collectAsStateWithLifecycle()
  
  val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    imageUri = uri
  }

  Column(
    modifier = modifier.fillMaxSize().padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(onClick = { picker.launch("image/*") }) {
      Text("Pick Image")
    }

    Spacer(modifier = Modifier.height(16.dp))

    imageUri?.let { uri ->
      AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = Modifier.size(300.dp)
      )
      
      Spacer(modifier = Modifier.height(16.dp))
      
      Button(onClick = { 
          val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
              val source = ImageDecoder.createSource(context.contentResolver, uri)
              ImageDecoder.decodeBitmap(source)
          } else {
              MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
          }
          viewModel.analyzeImage(bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true))
      }) {
        Text("Analyze")
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      if (result.isNotEmpty()) {
          Text(text = result)
      }
    }
  }
}

