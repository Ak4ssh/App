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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
  val error by viewModel.error.collectAsStateWithLifecycle()
  val history by viewModel.history.collectAsStateWithLifecycle()
  var tab by remember { mutableIntStateOf(0) }
  
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
          CenterAlignedTopAppBar(
              title = { Text(com.example.lungcancerdetector.AppConfig.APP_NAME, fontWeight = FontWeight.Bold) },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
          )
      },
      bottomBar = {
          NavigationBar {
              NavigationBarItem(
                  selected = tab == 0,
                  onClick = { tab = 0 },
                  icon = { Text("\uD83D\uDD2C") },
                  label = { Text("Scan") }
              )
              NavigationBarItem(
                  selected = tab == 1,
                  onClick = { tab = 1 },
                  icon = {
                      if (history.isNotEmpty()) {
                          BadgedBox(badge = { Badge { Text("${history.size}") } }) {
                              Text("\uD83D\uDCCB")
                          }
                      } else {
                          Text("\uD83D\uDCCB")
                      }
                  },
                  label = { Text("History") }
              )
              NavigationBarItem(
                  selected = tab == 2,
                  onClick = { tab = 2 },
                  icon = { Text("\u2139\uFE0F") },
                  label = { Text("About") }
              )
          }
      }
  ) { paddingValues ->
      if (tab == 1) {
          com.example.lungcancerdetector.ui.history.HistoryScreen(records = history)
          return@Scaffold
      }
      if (tab == 2) {
          com.example.lungcancerdetector.ui.about.AboutScreen()
          return@Scaffold
      }
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
              elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
              if (imageUri == null) {
                  androidx.compose.animation.AnimatedVisibility(
                      visible = true,
                      enter = androidx.compose.animation.fadeIn()
                  ) {
                      Column(
                          modifier = Modifier
                              .fillMaxSize()
                              .padding(16.dp)
                              .drawBehind {
                                  drawRoundRect(
                                      color = androidx.compose.ui.graphics.Color.Gray,
                                      style = androidx.compose.ui.graphics.drawscope.Stroke(
                                          width = 2.dp.toPx(),
                                          pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                                      ),
                                      cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                                  )
                              },
                          verticalArrangement = Arrangement.Center,
                          horizontalAlignment = Alignment.CenterHorizontally
                      ) {
                          androidx.compose.material3.Icon(
                              imageVector = androidx.compose.material.icons.Icons.Default.AddCircle,
                              contentDescription = "Upload",
                              modifier = Modifier.size(64.dp),
                              tint = MaterialTheme.colorScheme.primary
                          )
                          Spacer(modifier = Modifier.height(16.dp))
                          Text("Tap to upload histological slide", color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                  }
              } else {
                  AsyncImage(
                      model = imageUri,
                      contentDescription = "Selected Image",
                      modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                      contentScale = ContentScale.Crop
                  )
              }
          }
          
          Spacer(modifier = Modifier.height(32.dp))

          if (loading) {
              CircularProgressIndicator(modifier = Modifier.padding(16.dp))
              Spacer(modifier = Modifier.height(16.dp))
          }

          error?.let { msg ->
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors = CardDefaults.cardColors(containerColor = com.example.lungcancerdetector.theme.DangerRed.copy(alpha = 0.1f)),
                  shape = RoundedCornerShape(12.dp)
              ) {
                  Text(msg, modifier = Modifier.padding(16.dp), color = com.example.lungcancerdetector.theme.DangerRed)
              }
              Spacer(modifier = Modifier.height(16.dp))
          }
          
          androidx.compose.animation.AnimatedVisibility(
              visible = result != null,
              enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn()
          ) {
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
                          val isBenign = prediction.label == "Benign"
                          val badgeColor = if (isBenign) com.example.lungcancerdetector.theme.SafeGreen else com.example.lungcancerdetector.theme.DangerRed
                          val badgeText = if (isBenign) "Safe" else "Malignant"
                          Text("${(prediction.maxConfidence * 100).toInt()}% confidence", style = MaterialTheme.typography.bodyMedium, color = badgeColor, fontWeight = FontWeight.Bold)
                          Spacer(modifier = Modifier.height(8.dp))
                          Surface(
                              color = badgeColor.copy(alpha = 0.15f),
                              shape = RoundedCornerShape(8.dp)
                          ) {
                              Text(
                                  badgeText,
                                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                  color = badgeColor,
                                  fontWeight = FontWeight.Bold,
                                  style = MaterialTheme.typography.labelMedium
                              )
                          }
                          
                          Spacer(modifier = Modifier.height(24.dp))
                          Text("Confidence Breakdown", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
                          Spacer(modifier = Modifier.height(16.dp))
                          
                          ConfidenceBar("Adenocarcinoma (ACA)", prediction.acaConfidence, MaterialTheme.colorScheme.error)
                          Spacer(modifier = Modifier.height(12.dp))
                          ConfidenceBar("Benign Tissue", prediction.benignConfidence, com.example.lungcancerdetector.theme.SafeGreen)
                          Spacer(modifier = Modifier.height(12.dp))
                          ConfidenceBar("Squamous Cell (SCC)", prediction.sccConfidence, com.example.lungcancerdetector.theme.WarnOrange)
                      }
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
fun ConfidenceBar(label: String, confidence: Float, barColor: androidx.compose.ui.graphics.Color) {
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
                .height(10.dp)
                .clip(RoundedCornerShape(50.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f)
        )
    }
}
