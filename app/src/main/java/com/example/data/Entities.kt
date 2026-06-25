package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class Parcel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parcelId: String,
    val customerName: String,
    val customerMobile: String,
    val fullAddress: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val codAmount: Double = 0.0,
    val deliveryNotes: String = "",
    val status: String = "Pending", // "Pending", "Delivered", "Failed"
    val deliverySequence: Int = 999,
    val company: String = "Amazon", // "Amazon", "Flipkart", "Delhivery", "Ekart", etc.
    val photoProof: String? = null, // URI of photo
    val signaturePath: String? = null, // URI or vector points of signature
    val failedReason: String? = null
)

@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val area: String,
    val landmark: String,
    val notes: String,
    val preferredTime: String = "Anytime",
    val customerAvailability: Double = 0.9, // probability 0.0 to 1.0
    val deliveredCount: Int = 1,
    val failedCount: Int = 0
)

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Ramesh Kumar",
    val averageSpeedKmh: Double = 24.5,
    val dailyCapacity: Int = 90,
    val bestWorkingHours: String = "10 AM - 5 PM",
    val fuelSavedLiters: Double = 12.8,
    val timeSavedMinutes: Double = 185.0,
    val distanceTravelledKm: Double = 98.4,
    val aiEfficiencyScore: Double = 94.5
)

@Entity(tableName = "saved_landmarks")
data class SavedLandmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val historicalFact: String,
    val latitude: Double,
    val longitude: Double,
    val colorHex: String = "#F59E0B" // Amber
)

