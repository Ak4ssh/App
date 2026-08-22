package com.example.lungcancerdetector.data

data class ScanRecord(
    val id: Long = System.currentTimeMillis(),
    val label: String,
    val aca: Float,
    val benign: Float,
    val scc: Float,
    val timestamp: Long = System.currentTimeMillis()
)
