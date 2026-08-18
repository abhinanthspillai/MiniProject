package com.netraze.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Authoritative Room SpatialPosition entity (LOCAL_PERSISTENCE_MODEL §7).
 * Uses a single table with mode-scoped nullable spatial fields.
 */
@Entity(
    tableName = "spatial_positions",
    foreignKeys = [
        ForeignKey(
            entity = SurveyEntity::class,
            parentColumns = ["id"],
            childColumns = ["surveyId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("surveyId")]
)
data class SpatialPositionEntity(
    @PrimaryKey val id: UUID, // Canonical UUID generated on Android
    val surveyId: UUID,
    val label: String?,

    // Floor Plan Mode coordinates [0, 1]
    val floorPlanX: Double? = null,
    val floorPlanY: Double? = null,

    // Simple Map Mode coordinates [0, 1]
    val simpleMapX: Double? = null,
    val simpleMapY: Double? = null,

    // Location Survey Mode atomic fix
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Double? = null,
    val capturedAt: Long? = null,

    val createdAt: Long,
    val syncState: String = "pending"
) {
    /**
     * Enforces atomic location fix rule:
     * Either all 4 location fix fields are present or all 4 are null.
     */
    fun hasValidLocationFix(): Boolean {
        val nonNullCount = listOfNotNull(latitude, longitude, accuracyMeters, capturedAt).size
        return nonNullCount == 0 || nonNullCount == 4
    }
}
