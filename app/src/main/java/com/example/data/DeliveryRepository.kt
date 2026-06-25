package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class DeliveryRepository(private val db: DeliveryDatabase) {
    val allParcels: Flow<List<Parcel>> = db.parcelDao().getAllParcels()
    val allLearningRecords: Flow<List<LearningRecord>> = db.learningDao().getAllLearningRecords()
    val profileFlow: Flow<Profile?> = db.profileDao().getProfileFlow()
    val allSavedLandmarks: Flow<List<SavedLandmark>> = db.savedLandmarkDao().getAllSavedLandmarks()

    // Self-Learning AI Memory Flows
    val allCustomerMemories: Flow<List<CustomerMemory>> = db.selfLearningDao().getAllCustomerMemoriesFlow()
    val allBuildingMemories: Flow<List<BuildingMemory>> = db.selfLearningDao().getAllBuildingMemoriesFlow()
    val allAreaIntelligence: Flow<List<AreaIntelligence>> = db.selfLearningDao().getAllAreaIntelligenceFlow()
    val allRouteMemories: Flow<List<RouteMemory>> = db.selfLearningDao().getAllRouteMemoriesFlow()

    // Customer Memory Methods
    suspend fun getCustomerMemory(name: String): CustomerMemory? = db.selfLearningDao().getCustomerMemory(name)
    suspend fun insertCustomerMemory(memory: CustomerMemory) = db.selfLearningDao().insertCustomerMemory(memory)

    // Building Memory Methods
    suspend fun getBuildingMemory(name: String): BuildingMemory? = db.selfLearningDao().getBuildingMemory(name)
    suspend fun insertBuildingMemory(memory: BuildingMemory) = db.selfLearningDao().insertBuildingMemory(memory)

    // Area Intelligence Methods
    suspend fun getAreaIntelligence(name: String): AreaIntelligence? = db.selfLearningDao().getAreaIntelligence(name)
    suspend fun insertAreaIntelligence(intelligence: AreaIntelligence) = db.selfLearningDao().insertAreaIntelligence(intelligence)

    // Route Memory Methods
    suspend fun getRouteMemory(key: String): RouteMemory? = db.selfLearningDao().getRouteMemory(key)
    suspend fun insertRouteMemory(memory: RouteMemory) = db.selfLearningDao().insertRouteMemory(memory)

    // Clear Memories
    suspend fun clearAllMemories() {
        db.selfLearningDao().clearCustomerMemories()
        db.selfLearningDao().clearBuildingMemories()
        db.selfLearningDao().clearAreaIntelligence()
        db.selfLearningDao().clearRouteMemories()
    }

    suspend fun insertSavedLandmark(landmark: SavedLandmark): Long {
        return db.savedLandmarkDao().insertSavedLandmark(landmark)
    }

    suspend fun deleteSavedLandmark(id: Int) {
        db.savedLandmarkDao().deleteSavedLandmark(id)
    }

    suspend fun deleteAllSavedLandmarks() {
        db.savedLandmarkDao().deleteAllSavedLandmarks()
    }

    suspend fun getParcelById(id: Int): Parcel? {
        return db.parcelDao().getParcelById(id)
    }

    suspend fun insertParcel(parcel: Parcel): Long {
        return db.parcelDao().insertParcel(parcel)
    }

    suspend fun insertParcels(parcels: List<Parcel>) {
        db.parcelDao().insertParcels(parcels)
    }

    suspend fun updateParcel(parcel: Parcel) {
        db.parcelDao().updateParcel(parcel)
    }

    suspend fun deleteParcelById(id: Int) {
        db.parcelDao().deleteParcelById(id)
    }

    suspend fun deleteAllParcels() {
        db.parcelDao().deleteAllParcels()
    }

    suspend fun insertLearningRecord(record: LearningRecord) {
        db.learningDao().insertLearningRecord(record)
    }

    suspend fun getProfile(): Profile {
        return db.profileDao().getProfile() ?: Profile().also {
            db.profileDao().insertProfile(it)
        }
    }

    suspend fun updateProfile(profile: Profile) {
        db.profileDao().insertProfile(profile)
    }

    suspend fun prepopulateDatabase() {
        // Prepopulate profile if not exists
        val currentProfile = db.profileDao().getProfile()
        if (currentProfile == null) {
            db.profileDao().insertProfile(Profile())
        }

        // Prepopulate learning intelligence (AI village shortcuts and landmarks)
        val learningList = db.learningDao().getAllLearningRecords().firstOrNull() ?: emptyList()
        if (learningList.isEmpty()) {
            val mockLearning = listOf(
                LearningRecord(
                    area = "Village Rampur",
                    landmark = "Shiv Temple",
                    notes = "Take the narrow dirt path behind the Shiv Temple; fits bikes only. House is blue on the left.",
                    preferredTime = "4:00 PM - 7:00 PM",
                    customerAvailability = 0.95,
                    deliveredCount = 12,
                    failedCount = 0
                ),
                LearningRecord(
                    area = "Village Chhota Harishpur",
                    landmark = "Panchayat Office",
                    notes = "Located 100m south of the Panchayat Office near the big banyan tree. Road is unpaved, prone to waterlogging.",
                    preferredTime = "10:00 AM - 1:00 PM",
                    customerAvailability = 0.85,
                    deliveredCount = 8,
                    failedCount = 1
                ),
                LearningRecord(
                    area = "Village Pipri",
                    landmark = "Yellow Water Tank",
                    notes = "Take the lane opposite the Yellow Water Tank. Deliveries here are best in the morning since they work in fields.",
                    preferredTime = "7:00 AM - 11:00 AM",
                    customerAvailability = 0.90,
                    deliveredCount = 15,
                    failedCount = 1
                ),
                LearningRecord(
                    area = "Sector B Housing",
                    landmark = "Primary School",
                    notes = "Right next to the primary school playground. Guard at gate allows delivery bikes inside.",
                    preferredTime = "12:00 PM - 3:00 PM",
                    customerAvailability = 0.75,
                    deliveredCount = 20,
                    failedCount = 3
                )
            )
            for (rec in mockLearning) {
                db.learningDao().insertLearningRecord(rec)
            }
        }

        // Prepopulate standard mock parcels if database has no parcels
        val parcelList = db.parcelDao().getAllParcels().firstOrNull() ?: emptyList()
        if (parcelList.isEmpty()) {
            val mockParcels = listOf(
                Parcel(
                    parcelId = "PRC-8012",
                    customerName = "Satish Kumar",
                    customerMobile = "+91 98765 43210",
                    fullAddress = "Near Shiv Temple, Village Rampur, Ward No. 3",
                    latitude = 25.6124,
                    longitude = 85.1432,
                    codAmount = 450.00,
                    deliveryNotes = "Call Satish when near temple; he will walk down.",
                    company = "Delhivery",
                    deliverySequence = 1
                ),
                Parcel(
                    parcelId = "PRC-1025",
                    customerName = "Anjali Sharma",
                    customerMobile = "+91 87654 32109",
                    fullAddress = "Sector B Housing, House No. B-42, near Primary School",
                    latitude = 25.6155,
                    longitude = 85.1488,
                    codAmount = 0.00, // Prepaid
                    deliveryNotes = "Deliver to guard if not available.",
                    company = "Amazon",
                    deliverySequence = 2
                ),
                Parcel(
                    parcelId = "PRC-9043",
                    customerName = "Dipak Das",
                    customerMobile = "+91 76543 21098",
                    fullAddress = "Village Chhota Harishpur, near Panchayat Office",
                    latitude = 25.6091,
                    longitude = 85.1385,
                    codAmount = 1200.00,
                    deliveryNotes = "Wants COD. Keep change ready.",
                    company = "Flipkart",
                    deliverySequence = 3
                ),
                Parcel(
                    parcelId = "PRC-2071",
                    customerName = "Rajesh Maurya",
                    customerMobile = "+91 91234 56789",
                    fullAddress = "Village Pipri, opposite Yellow Water Tank",
                    latitude = 25.6012,
                    longitude = 85.1311,
                    codAmount = 180.00,
                    deliveryNotes = "Prefer morning delivery.",
                    company = "Ekart",
                    deliverySequence = 4
                )
            )
            db.parcelDao().insertParcels(mockParcels)
        }

        // Prepopulate Self-Learning Memories
        val existingCustMemories = db.selfLearningDao().getCustomerMemory("Satish Kumar")
        if (existingCustMemories == null) {
            // Seed Customer Memories
            db.selfLearningDao().insertCustomerMemory(
                CustomerMemory(
                    customerName = "Satish Kumar",
                    deliveryFrequency = 14,
                    preferredDeliveryTime = "8:00 AM - 11:30 AM",
                    commonInstructions = "Walk down to main temple. Do not ride up to front door due to children playing.",
                    gateInformation = "No main gate security, accessible directly",
                    floorNumber = 1,
                    liftAvailability = false,
                    landmark = "Shiv Temple Rampur",
                    deliverySuccessHistory = "S,S,S,S,S,F,S,S",
                    otpReadiness = "Ready",
                    averageWaitingTimeMinutes = 2.4,
                    confidenceScore = 98.0,
                    usageCount = 14,
                    successRate = 92.8
                )
            )
            db.selfLearningDao().insertCustomerMemory(
                CustomerMemory(
                    customerName = "Anjali Sharma",
                    deliveryFrequency = 22,
                    preferredDeliveryTime = "12:00 PM - 4:00 PM",
                    commonInstructions = "Leave with primary school security desk if unreachable.",
                    gateInformation = "Requires visitor logbook entry",
                    floorNumber = 3,
                    liftAvailability = true,
                    landmark = "Primary School Sector B",
                    deliverySuccessHistory = "S,S,S,S,S,S,S,S",
                    otpReadiness = "Slow",
                    averageWaitingTimeMinutes = 5.8,
                    confidenceScore = 96.0,
                    usageCount = 22,
                    successRate = 100.0
                )
            )
            db.selfLearningDao().insertCustomerMemory(
                CustomerMemory(
                    customerName = "Dipak Das",
                    deliveryFrequency = 8,
                    preferredDeliveryTime = "2:00 PM - 6:00 PM",
                    commonInstructions = "Prefers Cash on Delivery. Keep ₹500 change ready in wallet.",
                    gateInformation = "Gate unlocked, keep inside the gate box",
                    floorNumber = 1,
                    liftAvailability = false,
                    landmark = "Panchayat Office Chhota Harishpur",
                    deliverySuccessHistory = "S,S,F,S,S,S",
                    otpReadiness = "Ready",
                    averageWaitingTimeMinutes = 4.0,
                    confidenceScore = 91.0,
                    usageCount = 8,
                    successRate = 87.5
                )
            )

            // Seed Building Memories
            db.selfLearningDao().insertBuildingMemory(
                BuildingMemory(
                    buildingName = "Sector B Housing Complex",
                    entranceLocation = "Main East Gate adjacent to school",
                    parkingLocation = "Delivery lane parking slot 3B",
                    securityProcess = "Security scan QR code or manual entry",
                    visitorRegistration = true,
                    liftSpeed = "Fast",
                    stairAvailability = true,
                    exitLocation = "West exit bypass lane",
                    averageDeliveryTimeMinutes = 5.2,
                    bestEntrance = "East Gate",
                    deliveryDifficultyScore = 2,
                    confidenceScore = 94.0,
                    usageCount = 28,
                    successRate = 98.2
                )
            )
            db.selfLearningDao().insertBuildingMemory(
                BuildingMemory(
                    buildingName = "Panchayat Office Area Complex",
                    entranceLocation = "Front metal sliding gate",
                    parkingLocation = "Open dirt ground adjacent to entrance",
                    securityProcess = "Open gate, inform receptionist",
                    visitorRegistration = false,
                    liftSpeed = "None",
                    stairAvailability = true,
                    exitLocation = "Front gate only",
                    averageDeliveryTimeMinutes = 3.5,
                    bestEntrance = "Front gate",
                    deliveryDifficultyScore = 1,
                    confidenceScore = 96.0,
                    usageCount = 12,
                    successRate = 100.0
                )
            )

            // Seed Area Intelligence
            db.selfLearningDao().insertAreaIntelligence(
                AreaIntelligence(
                    areaName = "Village Rampur",
                    fastRoads = "Bypass Highway NH-22",
                    slowRoads = "Primary school lane (heavy school bus traffic)",
                    smallShortcuts = "Narrow temple mud track behind pond",
                    oneWayRoads = "None",
                    trafficTiming = "Peak traffic 8:30 AM - 10:00 AM (school drop-off)",
                    schoolTiming = "8:00 AM - 1:30 PM",
                    marketTiming = "Thursday evening weekly bazaar",
                    railwayCrossingTiming = "Crossing close duration 10 mins",
                    festivalTraffic = "Durga Puja mela bypass block",
                    rainEffect = "Temple lane prone to clay muddy slipping",
                    roadConstruction = "Drain repair active near Main chowk",
                    roadClosures = "None",
                    accidentProneAreas = "Highway NH-22 intersection turn",
                    confidenceScore = 97.0,
                    usageCount = 45,
                    successRate = 96.8
                )
            )
            db.selfLearningDao().insertAreaIntelligence(
                AreaIntelligence(
                    areaName = "Village Pipri",
                    fastRoads = "Canal Embankment Road",
                    slowRoads = "Village internal brick paved lane",
                    smallShortcuts = "Tube-well walking lane bypassing high school",
                    oneWayRoads = "None",
                    trafficTiming = "Afternoon field movement 1:00 PM - 3:00 PM",
                    schoolTiming = "9:00 AM - 3:00 PM",
                    marketTiming = "None",
                    railwayCrossingTiming = "None",
                    festivalTraffic = "Harvest festival temple street block",
                    rainEffect = "Water logging 2 feet deep opposite water tank",
                    roadConstruction = "Paving blocks under progress in ward 2",
                    roadClosures = "None",
                    accidentProneAreas = "Canal sharp slide curve",
                    confidenceScore = 92.0,
                    usageCount = 30,
                    successRate = 95.0
                )
            )

            // Seed Route Memories
            db.selfLearningDao().insertRouteMemory(
                RouteMemory(
                    routeKey = "Hub -> Rampur (Temple Shortcut)",
                    suggestedRoutePoints = "25.602,85.132;25.610,85.140;25.6124,85.1432",
                    actualRoutePoints = "25.602,85.132;25.608,85.138;25.6124,85.1432",
                    suggestedTimeMinutes = 11.5,
                    actualTimeMinutes = 8.2, // Faster actual route learned!
                    suggestedDistanceKm = 3.8,
                    actualDistanceKm = 3.1, // Shorter distance learned!
                    suggestedFuelLiters = 0.22,
                    actualFuelLiters = 0.15, // Better fuel efficiency!
                    confidenceScore = 97.0,
                    usageCount = 18,
                    successRate = 100.0
                )
            )
        }
    }
}
