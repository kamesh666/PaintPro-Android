package com.paintpro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the `profiles` table PaintPro-Web already syncs to on the same Supabase project,
 * trimmed to the fields the Android MVP actually needs. Extra columns on the real table
 * (gstin, tagline, avatar_url, ...) are simply left untouched by this app.
 */
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val businessName: String?,
    val phoneNumber: String?,
    val role: String, // "Contractor" | "Supervisor"
    val orgId: String,
    val currency: String = "₹",
    val createdAt: String,
)
