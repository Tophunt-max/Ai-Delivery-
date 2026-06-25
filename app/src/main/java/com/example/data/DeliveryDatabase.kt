package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Parcel::class, 
        LearningRecord::class, 
        Profile::class, 
        SavedLandmark::class,
        CustomerMemory::class,
        BuildingMemory::class,
        AreaIntelligence::class,
        RouteMemory::class
    ], 
    version = 3, 
    exportSchema = false
)
abstract class DeliveryDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun learningDao(): LearningDao
    abstract fun profileDao(): ProfileDao
    abstract fun savedLandmarkDao(): SavedLandmarkDao
    abstract fun selfLearningDao(): SelfLearningDao

    companion object {
        @Volatile
        private var INSTANCE: DeliveryDatabase? = null

        fun getDatabase(context: Context): DeliveryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeliveryDatabase::class.java,
                    "delivery_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
