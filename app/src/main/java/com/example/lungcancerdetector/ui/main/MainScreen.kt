package com.example.lungcancerdetector.ui.main

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel(),
) {
  val context = LocalContext.current
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  val result by viewModel.result.collectAsStateWithLifecycle()
  val loading by viewModel.loading.collectAsStateWithLifecycle()
  
  val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    if (uri != null) {
        imageUri = uri
        // Auto analyze when image is picked for a seamless experience
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        viewModel.analyzeImage(bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true))
    }
  }

  Scaffold(
      topBar = {
          TopAppBar(
              title = { Text("LungScan AI", fontWeight = FontWeight.Bold) },
              colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
          )
      }
  ) { paddingValues ->
      Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
          // Upload / Image Card
          Card(
              modifier = Modifier
                  .fillMaxWidth()
                  .height(300.dp)
                  .clickable { picker.launch("image/*") },
              shape = RoundedCornerShape(16.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
              if (imageUri == null) {
                  Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.Center,
                      horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                      Text(
                          text = "+",
                          style = MaterialTheme.typography.displayLarge,
                          color = MaterialTheme.colorScheme.primary
                      )
                      Spacer(modifier = Modifier.height(16.dp))
                      Text("Tap to upload histological slide", color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
              } else {
                  AsyncImage(
                      model = imageUri,
                      contentDescription = "Selected Image",
                      modifier = Modifier.fillMaxSize(),
                      contentScale = ContentScale.Crop
                  )
              }
          }
          
          Spacer(modifier = Modifier.height(32.dp))

          if (loading) {
              CircularProgressIndicator(modifier = Modifier.padding(16.dp))
              Spacer(modifier = Modifier.height(16.dp))
          }
          
          result?.let { prediction ->
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                  Column(
                      modifier = Modifier.padding(24.dp)
                  ) {
                      Text("Primary Diagnosis", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(prediction.label, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                      
                      Spacer(modifier = Modifier.height(24.dp))
                      Text("Confidence Breakdown", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
                      Spacer(modifier = Modifier.height(16.dp))
                      
                      ConfidenceBar("Adenocarcinoma (ACA)", prediction.acaConfidence)
                      Spacer(modifier = Modifier.height(12.dp))
                      ConfidenceBar("Benign Tissue", prediction.benignConfidence)
                      Spacer(modifier = Modifier.height(12.dp))
                      ConfidenceBar("Squamous Cell (SCC)", prediction.sccConfidence)
                  }
              }
          }
          
          Spacer(modifier = Modifier.weight(1f))
          
          // Action Button
          Button(
              onClick = { picker.launch("image/*") },
              modifier = Modifier
                  .fillMaxWidth()
                  .height(56.dp),
              shape = RoundedCornerShape(12.dp)
          ) {
              Text("Select New Image", fontSize = MaterialTheme.typography.titleMedium.fontSize)
          }
      }
  }
}

@Composable
fun ConfidenceBar(label: String, confidence: Float) {
    val animatedProgress by animateFloatAsState(targetValue = confidence, label = "progress")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(confidence * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
    }
}
