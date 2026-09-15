package com.paintpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    indices = [Index("orgId"), Index("date"), Index("labourId"), Index("syncState")],
)
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val orgId: String,
    val labourId: String,
    val date: String, // YYYY-MM-DD
    val siteId: String?,
    val shiftCount: Double, // 0.5, 1, 1.5, 2
    val wageForDay: Double,
    val isCasualWork: Boolean,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncState: String = "SYNCED",
)
