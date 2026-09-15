package com.paintpro.app.data.remote.dto

import com.paintpro.app.data.local.entity.LabourEntity
import com.paintpro.app.data.repository.SyncState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the snake_case columns of Supabase's `labours` table. */
@Serializable
data class LabourDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("org_id") val orgId: String,
    val name: String,
    @SerialName("phone_number") val phoneNumber: String,
    val role: String,
    @SerialName("daily_wage") val dailyWage: Double,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("joined_date") val joinedDate: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

fun LabourDto.toEntity(syncState: String = SyncState.SYNCED): LabourEntity = LabourEntity(
    id = id,
    userId = userId,
    orgId = orgId,
    name = name,
    phoneNumber = phoneNumber,
    role = role,
    dailyWage = dailyWage,
    isActive = isActive,
    isDeleted = isDeleted,
    joinedDate = joinedDate,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncState = syncState,
)

fun LabourEntity.toDto(): LabourDto = LabourDto(
    id = id,
    userId = userId,
    orgId = orgId,
    name = name,
    phoneNumber = phoneNumber,
    role = role,
    dailyWage = dailyWage,
    isActive = isActive,
    isDeleted = isDeleted,
    joinedDate = joinedDate,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
