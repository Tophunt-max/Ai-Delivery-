package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.Parcel
import com.example.data.LearningRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object DirectionsApiClient {
    private const val TAG = "DirectionsApiClient"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GOOGLE_MAPS_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Optimizes delivery order of parcels using Google Maps Directions API.
     * Returns a Pair containing:
     * 1. The optimized List of Parcels with updated deliverySequence numbers.
     * 2. A string status representing the outcome source (e.g., API success or local fallback reason).
     */
    suspend fun optimizeRouteWithGoogleMaps(
        parcels: List<Parcel>,
        learningRecords: List<LearningRecord> = emptyList(),
        originLat: Double = 25.602,
        originLng: Double = 85.132
    ): Pair<List<Parcel>, String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "YOUR_GOOGLE_MAPS_API_KEY") {
            Log.w(TAG, "Google Maps API Key is placeholder or missing. Falling back to local optimization.")
            val baseOptimized = localRouteOptimization(parcels, originLat, originLng)
            val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(baseOptimized, learningRecords)
            return@withContext Pair(
                selfLearnedOptimized,
                "Local Heuristics$adjustmentsMsg"
            )
        }

        if (parcels.isEmpty()) {
            return@withContext Pair(emptyList(), "No parcels to optimize.")
        }

        val originStr = "$originLat,$originLng"
        val destinationStr = originStr

        val waypointsList = parcels.map { "${it.latitude ?: 25.61},${it.longitude ?: 85.14}" }
        val waypointsStr = "optimize:true|" + waypointsList.joinToString("|")

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=$originStr" +
                "&destination=$destinationStr" +
                "&waypoints=$waypointsStr" +
                "&key=$apiKey"

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = "Directions API error code: ${response.code}"
                    Log.e(TAG, errorMsg)
                    val baseOptimized = localRouteOptimization(parcels, originLat, originLng)
                    val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(baseOptimized, learningRecords)
                    return@withContext Pair(
                        selfLearnedOptimized,
                        "Local Fallback$adjustmentsMsg"
                    )
                }

                val bodyString = response.body?.string() ?: ""
                val jsonObject = JSONObject(bodyString)
                val status = jsonObject.optString("status")

                if (status != "OK") {
                    val errorMsg = "Directions API Status: $status"
                    Log.w(TAG, errorMsg)
                    val baseOptimized = localRouteOptimization(parcels, originLat, originLng)
                    val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(baseOptimized, learningRecords)
                    val displayStatus = when (status) {
                        "REQUEST_DENIED" -> "Local Fallback$adjustmentsMsg (Request Denied)"
                        "OVER_QUERY_LIMIT" -> "Local Fallback$adjustmentsMsg (Query Limit)"
                        else -> "Local Fallback$adjustmentsMsg (Status: $status)"
                    }
                    return@withContext Pair(
                        selfLearnedOptimized,
                        displayStatus
                    )
                }

                val routes = jsonObject.optJSONArray("routes")
                if (routes == null || routes.length() == 0) {
                    val baseOptimized = localRouteOptimization(parcels, originLat, originLng)
                    val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(baseOptimized, learningRecords)
                    return@withContext Pair(
                        selfLearnedOptimized,
                        "Local Fallback$adjustmentsMsg (No route)"
                    )
                }

                val route = routes.getJSONObject(0)
                val waypointOrder = route.optJSONArray("waypoint_order")

                if (waypointOrder == null || waypointOrder.length() == 0) {
                    val baseOptimized = localRouteOptimization(parcels, originLat, originLng)
                    val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(baseOptimized, learningRecords)
                    return@withContext Pair(
                        selfLearnedOptimized,
                        "Local Fallback$adjustmentsMsg (Too few waypoints)"
                    )
                }

                val orderList = mutableListOf<Int>()
                for (i in 0 until waypointOrder.length()) {
                    orderList.add(waypointOrder.getInt(i))
                }

                val optimizedList = ArrayList<Parcel>()
                orderList.forEachIndexed { seq, originalIdx ->
                    if (originalIdx in parcels.indices) {
                        optimizedList.add(parcels[originalIdx].copy(deliverySequence = seq + 1))
                    }
                }

                // Add any missing parcels that Google Maps forgot to include or that were out of range
                parcels.forEach { p ->
                    if (optimizedList.none { it.parcelId == p.parcelId }) {
                        optimizedList.add(p.copy(deliverySequence = optimizedList.size + 1))
                    }
                }

                val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(optimizedList, learningRecords)
                return@withContext Pair(selfLearnedOptimized, "Optimized via Google Maps Directions API$adjustmentsMsg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Directions API exception, falling back", e)
            val baseOptimized = localRouteOptimization(parcels, originLat, originLng)
            val (selfLearnedOptimized, adjustmentsMsg) = applySelfLearningAdjustments(baseOptimized, learningRecords)
            return@withContext Pair(
                selfLearnedOptimized,
                "Local Heuristics$adjustmentsMsg (Connection Error)"
            )
        }
    }

    private fun applySelfLearningAdjustments(
        parcels: List<Parcel>,
        learningRecords: List<LearningRecord>
    ): Pair<List<Parcel>, String> {
        if (learningRecords.isEmpty() || parcels.size < 2) return Pair(parcels, "")

        val adjusted = parcels.toMutableList()
        var adjustedCount = 0

        // Search for parcels that match a learning record area/landmark and have specific time constraints
        for (i in adjusted.indices) {
            val p = adjusted[i]
            val record = learningRecords.firstOrNull { rec ->
                p.fullAddress.contains(rec.area, ignoreCase = true) || 
                p.fullAddress.contains(rec.landmark, ignoreCase = true) ||
                p.fullAddress.lowercase().contains(rec.area.lowercase()) ||
                p.fullAddress.lowercase().contains(rec.landmark.lowercase())
            }

            if (record != null) {
                val prefTime = record.preferredTime.lowercase()
                val isLatePreferred = prefTime.contains("evening") || prefTime.contains("after 5") || prefTime.contains("after 4") || prefTime.contains("pm")
                val isEarlyPreferred = prefTime.contains("morning") || prefTime.contains("early") || prefTime.contains("before 12") || prefTime.contains("am")

                // If late is preferred, move towards the end of the delivery queue
                if (isLatePreferred && i < adjusted.size / 2) {
                    // Swap with someone in the back half
                    val targetIdx = adjusted.size - 1 - (i % (adjusted.size / 2))
                    if (targetIdx != i && targetIdx in adjusted.indices) {
                        val temp = adjusted[i]
                        adjusted[i] = adjusted[targetIdx]
                        adjusted[targetIdx] = temp
                        adjustedCount++
                        Log.d("DirectionsApiClient", "Self-Learning: Moved ${temp.customerName} to end because late delivery is preferred.")
                    }
                }
                // If early is preferred, move towards the front of the delivery queue
                else if (isEarlyPreferred && i >= adjusted.size / 2) {
                    // Swap with someone in the front half
                    val targetIdx = i % (adjusted.size / 2)
                    if (targetIdx != i && targetIdx in adjusted.indices) {
                        val temp = adjusted[i]
                        adjusted[i] = adjusted[targetIdx]
                        adjusted[targetIdx] = temp
                        adjustedCount++
                        Log.d("DirectionsApiClient", "Self-Learning: Moved ${temp.customerName} to front because early delivery is preferred.")
                    }
                }
            }
        }

        // Re-assign correct sequences
        val finalParcels = adjusted.mapIndexed { index, p ->
            p.copy(deliverySequence = index + 1)
        }

        val statusText = if (adjustedCount > 0) {
            " [AI Self-Learning Adjusted: $adjustedCount timings aligned]"
        } else {
            ""
        }

        return Pair(finalParcels, statusText)
    }

    private fun localRouteOptimization(parcels: List<Parcel>, startLat: Double, startLng: Double): List<Parcel> {
        if (parcels.isEmpty()) return emptyList()

        val mutableParcels = parcels.toMutableList()
        val optimized = mutableListOf<Parcel>()
        
        var currentLat = startLat
        var currentLong = startLng
        
        var sequence = 1
        while (mutableParcels.isNotEmpty()) {
            var closestIndex = 0
            var minDistance = Double.MAX_VALUE
            
            for (i in mutableParcels.indices) {
                val p = mutableParcels[i]
                val lat = p.latitude ?: 25.61
                val lon = p.longitude ?: 85.14
                val dist = calculateDistance(currentLat, currentLong, lat, lon)
                if (dist < minDistance) {
                    minDistance = dist
                    closestIndex = i
                }
            }
            
            val nearest = mutableParcels.removeAt(closestIndex)
            optimized.add(nearest.copy(deliverySequence = sequence++))
            currentLat = nearest.latitude ?: currentLat
            currentLong = nearest.longitude ?: currentLong
        }
        
        return optimized
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
