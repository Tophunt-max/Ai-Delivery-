package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SelfLearningDao {
    // 1. Customer Memory
    @Query("SELECT * FROM customer_memories WHERE customerName = :name LIMIT 1")
    suspend fun getCustomerMemory(name: String): CustomerMemory?

    @Query("SELECT * FROM customer_memories ORDER BY confidenceScore DESC, usageCount DESC")
    fun getAllCustomerMemoriesFlow(): Flow<List<CustomerMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerMemory(memory: CustomerMemory)

    // 2. Building Memory
    @Query("SELECT * FROM building_memories WHERE buildingName = :name LIMIT 1")
    suspend fun getBuildingMemory(name: String): BuildingMemory?

    @Query("SELECT * FROM building_memories ORDER BY confidenceScore DESC, usageCount DESC")
    fun getAllBuildingMemoriesFlow(): Flow<List<BuildingMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingMemory(memory: BuildingMemory)

    // 3. Area Intelligence
    @Query("SELECT * FROM area_intelligence WHERE areaName = :name LIMIT 1")
    suspend fun getAreaIntelligence(name: String): AreaIntelligence?

    @Query("SELECT * FROM area_intelligence ORDER BY confidenceScore DESC, usageCount DESC")
    fun getAllAreaIntelligenceFlow(): Flow<List<AreaIntelligence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreaIntelligence(intelligence: AreaIntelligence)

    // 4. Route Memory
    @Query("SELECT * FROM route_memories WHERE routeKey = :key LIMIT 1")
    suspend fun getRouteMemory(key: String): RouteMemory?

    @Query("SELECT * FROM route_memories ORDER BY confidenceScore DESC, usageCount DESC")
    fun getAllRouteMemoriesFlow(): Flow<List<RouteMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteMemory(memory: RouteMemory)

    // 5. Cleanup
    @Query("DELETE FROM customer_memories")
    suspend fun clearCustomerMemories()

    @Query("DELETE FROM building_memories")
    suspend fun clearBuildingMemories()

    @Query("DELETE FROM area_intelligence")
    suspend fun clearAreaIntelligence()

    @Query("DELETE FROM route_memories")
    suspend fun clearRouteMemories()
}
