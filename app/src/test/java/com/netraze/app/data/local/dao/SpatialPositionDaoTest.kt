package com.netraze.app.data.local.dao

import com.netraze.app.data.local.entity.SpatialPositionEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SpatialPositionDaoTest {

    @Test
    fun testLocationFixAtomicityValidWhenAllFieldsPresent() {
        val pos = SpatialPositionEntity(
            id = UUID.randomUUID(),
            surveyId = UUID.randomUUID(),
            label = "Point A",
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 3.5,
            capturedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        assertTrue(pos.hasValidLocationFix())
    }

    @Test
    fun testLocationFixAtomicityValidWhenAllFieldsNull() {
        // D077: Location Survey mode valid with 0 LocationFix fields
        val pos = SpatialPositionEntity(
            id = UUID.randomUUID(),
            surveyId = UUID.randomUUID(),
            label = "Point B (No GPS Fix)",
            latitude = null,
            longitude = null,
            accuracyMeters = null,
            capturedAt = null,
            createdAt = System.currentTimeMillis()
        )
        assertTrue(pos.hasValidLocationFix())
    }

    @Test
    fun testLocationFixAtomicityInvalidWhenPartialFieldsPresent() {
        val posPartial = SpatialPositionEntity(
            id = UUID.randomUUID(),
            surveyId = UUID.randomUUID(),
            label = "Invalid Fix",
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = null, // Missing accuracy and timestamp
            capturedAt = null,
            createdAt = System.currentTimeMillis()
        )
        assertFalse(posPartial.hasValidLocationFix())
    }

    @Test
    fun testFloorPlanCoordinatesAtomicity() {
        val pos = SpatialPositionEntity(
            id = UUID.randomUUID(),
            surveyId = UUID.randomUUID(),
            label = "Desk 1",
            floorPlanX = 0.45,
            floorPlanY = 0.60,
            createdAt = System.currentTimeMillis()
        )
        assertTrue(pos.floorPlanX != null && pos.floorPlanY != null)
    }
}
