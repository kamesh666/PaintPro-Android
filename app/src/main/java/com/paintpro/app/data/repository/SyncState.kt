package com.paintpro.app.data.repository

/**
 * Offline-first sync bookkeeping used by [com.paintpro.app.data.local.entity.SiteEntity],
 * [com.paintpro.app.data.local.entity.LabourEntity] and
 * [com.paintpro.app.data.local.entity.AttendanceEntity].
 *
 * Every local write lands in Room immediately with `syncState = PENDING_UPSERT` (or
 * `PENDING_DELETE` for a soft-delete). A repository then tries to push it to Supabase; on
 * success the row is flipped to `SYNCED`, and on failure (offline, server error) it is simply
 * left as-is so it gets retried the next time a sync pass runs. This is the deliberate
 * improvement over PaintPro-Web's fire-and-forget dual-write: nothing typed while offline is
 * ever silently lost, it just waits for connectivity.
 */
object SyncState {
    const val SYNCED = "SYNCED"
    const val PENDING_UPSERT = "PENDING_UPSERT"
    const val PENDING_DELETE = "PENDING_DELETE"
}
