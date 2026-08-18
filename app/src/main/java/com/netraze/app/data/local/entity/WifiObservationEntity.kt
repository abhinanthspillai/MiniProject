package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "wifi_observations",
    foreignKeys = [
        ForeignKey(
            entity = ScanCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanCycleId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("scanCycleId"), Index("bssid")]
)
data class WifiObservationEntity(
    @PrimaryKey val id: UUID, // Canonical UUID generated on Android
    val scanCycleId: UUID,
    val ssid: String?,
    val bssid: String,
    val rssiDbm: Int,
    val frequencyMhz: Int,
    val channel: Int?,
    val channelSource: String,
    val capabilities: String?,
    val syncState: String = "pending" // "pending", "synced"
)
