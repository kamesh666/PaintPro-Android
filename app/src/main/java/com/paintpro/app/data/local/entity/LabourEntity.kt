package com.paintpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "labours",
    indices = [Index("orgId"), Index("syncState")],
)
data class LabourEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val orgId: String,
    val name: String,
    val phoneNumber: String,
    val role: String, // Painter | Helper | Supervisor | Master Painter | Polisher | Spray Specialist
    val dailyWage: Double,
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val joinedDate: String,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val syncState: String = "SYNCED",
)
