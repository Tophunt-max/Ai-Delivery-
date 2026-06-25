package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_memories")
data class CustomerMemory(
    @PrimaryKey val customerName: String,
    val deliveryFrequency: Int = 1,
    val preferredDeliveryTime: String = "Anytime",
    val commonInstructions: String = "",
    val gateInformation: String = "No gate restrictions",
    val floorNumber: Int = 1,
    val liftAvailability: Boolean = true,
    val landmark: String = "",
    val deliverySuccessHistory: String = "Success", // e.g. "S,S,S"
    val otpReadiness: String = "Ready", // "Ready", "Slow", "Delay"
    val averageWaitingTimeMinutes: Double = 3.5,
    val confidenceScore: Double = 90.0,
    val usageCount: Int = 1,
    val successRate: Double = 100.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "building_memories")
data class BuildingMemory(
    @PrimaryKey val buildingName: String,
    val entranceLocation: String = "Front main gate",
    val parkingLocation: String = "Delivery bikes parking inside boundary",
    val securityProcess: String = "No entry without register log",
    val visitorRegistration: Boolean = true,
    val liftSpeed: String = "Medium",
    val stairAvailability: Boolean = true,
    val exitLocation: String = "Same as main entrance",
    val averageDeliveryTimeMinutes: Double = 6.0,
    val bestEntrance: String = "North gate",
    val deliveryDifficultyScore: Int = 2, // 1 (Easy) to 5 (Extremely Hard)
    val confidenceScore: Double = 85.0,
    val usageCount: Int = 1,
    val successRate: Double = 100.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "area_intelligence")
data class AreaIntelligence(
    @PrimaryKey val areaName: String,
    val fastRoads: String = "Main Bypass",
    val slowRoads: String = "Panchayat school lane",
    val smallShortcuts: String = "Dirt lane behind Shiv Mandir",
    val oneWayRoads: String = "None",
    val trafficTiming: String = "Heavy during 5PM - 7PM",
    val schoolTiming: String = "8:00 AM - 1:30 PM",
    val marketTiming: String = "Weekly Thursday village haat bazaar",
    val railwayCrossingTiming: String = "Gate closes every 45 mins",
    val festivalTraffic: String = "Chhath Puja/Durga Puja rush",
    val rainEffect: String = "Waterlogging near railway crossing lane",
    val roadConstruction: String = "None active",
    val roadClosures: String = "None",
    val accidentProneAreas: String = "Bypass turning intersection",
    val confidenceScore: Double = 88.0,
    val usageCount: Int = 1,
    val successRate: Double = 100.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "route_memories")
data class RouteMemory(
    @PrimaryKey val routeKey: String, // e.g., "Rampur-Pipri"
    val suggestedRoutePoints: String = "", // comma-separated lat,lng nodes
    val actualRoutePoints: String = "",
    val suggestedTimeMinutes: Double = 15.0,
    val actualTimeMinutes: Double = 12.0,
    val suggestedDistanceKm: Double = 4.2,
    val actualDistanceKm: Double = 3.6,
    val suggestedFuelLiters: Double = 0.25,
    val actualFuelLiters: Double = 0.18,
    val confidenceScore: Double = 95.0,
    val usageCount: Int = 1,
    val successRate: Double = 100.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
