package com.paintpro.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paintpro.app.data.local.dao.AttendanceDao
import com.paintpro.app.data.local.dao.LabourDao
import com.paintpro.app.data.local.dao.ProfileDao
import com.paintpro.app.data.local.dao.SiteDao
import com.paintpro.app.data.local.entity.AttendanceEntity
import com.paintpro.app.data.local.entity.LabourEntity
import com.paintpro.app.data.local.entity.ProfileEntity
import com.paintpro.app.data.local.entity.SiteEntity

@Database(
    entities = [
        ProfileEntity::class,
        SiteEntity::class,
        LabourEntity::class,
        AttendanceEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun siteDao(): SiteDao
    abstract fun labourDao(): LabourDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paintpro.db",
                ).build().also { instance = it }
            }
    }
}
