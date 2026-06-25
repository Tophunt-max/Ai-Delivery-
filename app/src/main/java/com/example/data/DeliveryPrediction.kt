package com.example.data

data class DeliveryPrediction(
    val customerUnavailableRisk: Boolean = false,
    val trafficDelayMinutes: Double = 0.0,
    val rainDelayMinutes: Double = 0.0,
    val waitingTimeMinutes: Double = 3.0,
    val parkingDifficulty: String = "Easy", // "Easy", "Medium", "Hard"
    val buildingSpeed: String = "Medium", // "Fast", "Medium", "Slow"
    val lateDeliveryRisk: Boolean = false,
    val confidence: Double = 90.0,
    val predictionSource: String = "Database First Cache"
)
