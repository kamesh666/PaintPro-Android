package com.paintpro.app.data.remote.dto

import com.paintpro.app.data.local.entity.ProfileEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the snake_case columns of Supabase's `profiles` table. */
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("business_name") val businessName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val role: String,
    @SerialName("org_id") val orgId: String,
    val currency: String = "₹",
    @SerialName("created_at") val createdAt: String,
)

fun ProfileDto.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    fullName = fullName,
    businessName = businessName,
    phoneNumber = phoneNumber,
    role = role,
    orgId = orgId,
    currency = currency,
    createdAt = createdAt,
)

fun ProfileEntity.toDto(): ProfileDto = ProfileDto(
    id = id,
    fullName = fullName,
    businessName = businessName,
    phoneNumber = phoneNumber,
    role = role,
    orgId = orgId,
    currency = currency,
    createdAt = createdAt,
)
