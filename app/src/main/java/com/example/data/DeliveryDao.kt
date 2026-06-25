package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels ORDER BY deliverySequence ASC, id ASC")
    fun getAllParcels(): Flow<List<Parcel>>

    @Query("SELECT * FROM parcels WHERE id = :id LIMIT 1")
    suspend fun getParcelById(id: Int): Parcel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: Parcel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcels(parcels: List<Parcel>)

    @Update
    suspend fun updateParcel(parcel: Parcel)

    @Query("DELETE FROM parcels")
    suspend fun deleteAllParcels()

    @Query("DELETE FROM parcels WHERE id = :id")
    suspend fun deleteParcelById(id: Int)
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_records ORDER BY id DESC")
    fun getAllLearningRecords(): Flow<List<LearningRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningRecord(record: LearningRecord)

    @Query("SELECT * FROM learning_records WHERE area LIKE '%' || :query || '%' OR landmark LIKE '%' || :query || '%'")
    suspend fun searchLearningRecords(query: String): List<LearningRecord>

    @Query("DELETE FROM learning_records")
    suspend fun deleteAllLearningRecords()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<Profile?>

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)
}

@Dao
interface SavedLandmarkDao {
    @Query("SELECT * FROM saved_landmarks ORDER BY id DESC")
    fun getAllSavedLandmarks(): Flow<List<SavedLandmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedLandmark(landmark: SavedLandmark): Long

    @Query("DELETE FROM saved_landmarks WHERE id = :id")
    suspend fun deleteSavedLandmark(id: Int)

    @Query("DELETE FROM saved_landmarks")
    suspend fun deleteAllSavedLandmarks()
}
