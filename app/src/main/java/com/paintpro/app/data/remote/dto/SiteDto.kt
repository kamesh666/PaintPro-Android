package com.paintpro.app.data.remote.dto

import com.paintpro.app.data.local.entity.SiteEntity
import com.paintpro.app.data.repository.SyncState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the core snake_case columns of Supabase's `sites` table (see [SiteEntity] for notes). */
@Serializable
data class SiteDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("site_name") val siteName: String,
    @SerialName("client_name") val clientName: String,
    val address: String,
    @SerialName("site_type") val siteType: String,
    @SerialName("client_rate_per_shift") val clientRatePerShift: Double? = null,
    @SerialName("total_budget") val totalBudget: Double = 0.0,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    val status: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
)

fun SiteDto.toEntity(syncState: String = SyncState.SYNCED): SiteEntity = SiteEntity(
    id = id,
    userId = userId,
    orgId = orgId,
    siteName = siteName,
    clientName = clientName,
    address = address,
    siteType = siteType,
    clientRatePerShift = clientRatePerShift,
    totalBudget = totalBudget,
    startDate = startDate,
    endDate = endDate,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    syncState = syncState,
)

fun SiteEntity.toDto(): SiteDto = SiteDto(
    id = id,
    userId = userId,
    orgId = orgId,
    siteName = siteName,
    clientName = clientName,
    address = address,
    siteType = siteType,
    clientRatePerShift = clientRatePerShift,
    totalBudget = totalBudget,
    startDate = startDate,
    endDate = endDate,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
)
