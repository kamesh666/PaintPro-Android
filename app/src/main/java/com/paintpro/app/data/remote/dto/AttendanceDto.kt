package com.paintpro.app.data.remote.dto

import com.paintpro.app.data.local.entity.AttendanceEntity
import com.paintpro.app.data.repository.SyncState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the snake_case columns of Supabase's `attendance` table. */
@Serializable
data class AttendanceDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("labour_id") val labourId: String,
    val date: String,
    @SerialName("site_id") val siteId: String? = null,
    @SerialName("shift_count") val shiftCount: Double,
    @SerialName("wage_for_day") val wageForDay: Double,
    @SerialName("is_casual_work") val isCasualWork: Boolean = false,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

fun AttendanceDto.toEntity(syncState: String = SyncState.SYNCED): AttendanceEntity = AttendanceEntity(
    id = id,
    userId = userId,
    orgId = orgId,
    labourId = labourId,
    date = date,
    siteId = siteId,
    shiftCount = shiftCount,
    wageForDay = wageForDay,
    isCasualWork = isCasualWork,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncState = syncState,
)

fun AttendanceEntity.toDto(): AttendanceDto = AttendanceDto(
    id = id,
    userId = userId,
    orgId = orgId,
    labourId = labourId,
    date = date,
    siteId = siteId,
    shiftCount = shiftCount,
    wageForDay = wageForDay,
    isCasualWork = isCasualWork,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
