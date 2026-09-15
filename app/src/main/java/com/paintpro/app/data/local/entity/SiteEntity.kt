package com.paintpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mirrors the core columns of PaintPro-Web's `sites` table. Pipeline/warranty/share-link
 * columns that the web app added later are intentionally left out of this MVP - the real
 * Supabase table already has them as nullable, so leaving them unset here does no harm and
 * they can be added to this entity in a later pass without any migration on the server side.
 */
@Entity(
    tableName = "sites",
    indices = [Index("orgId"), Index("syncState")],
)
data class SiteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val orgId: String,
    val siteName: String,
    val clientName: String,
    val address: String,
    val siteType: String, // "Contract" | "Labour-Based"
    val clientRatePerShift: Double?,
    val totalBudget: Double,
    val startDate: String, // YYYY-MM-DD
    val endDate: String?,
    val status: String, // "Ongoing" | "Completed" | "On Hold"
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean = false,
    /** Offline-first sync bookkeeping - see [com.paintpro.app.data.repository.SyncState]. */
    val syncState: String = "SYNCED",
)
