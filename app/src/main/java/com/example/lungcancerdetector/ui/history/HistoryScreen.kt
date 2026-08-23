package com.example.lungcancerdetector.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lungcancerdetector.data.ScanRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(records: List<ScanRecord>) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(pad)) {
                Text(
                    "No scans yet",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records.reversed()) { record ->
                    HistoryItem(record)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: ScanRecord) {
    val fmt = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val date = fmt.format(Date(record.timestamp))

    val tint = if (record.isMalignant())
        com.example.lungcancerdetector.theme.DangerRed.copy(alpha = 0.08f)
    else
        com.example.lungcancerdetector.theme.SafeGreen.copy(alpha = 0.08f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = tint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(record.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "ACA: ${(record.aca * 100).toInt()}% | Benign: ${(record.benign * 100).toInt()}% | SCC: ${(record.scc * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
