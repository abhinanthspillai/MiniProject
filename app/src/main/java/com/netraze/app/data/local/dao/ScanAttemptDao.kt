package com.netraze.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.netraze.app.data.local.entity.ScanAttemptEntity
import java.util.UUID

@Dao
interface ScanAttemptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertScanAttempt(scanAttempt: ScanAttemptEntity)

    @Query("SELECT * FROM scan_attempts WHERE id = :id")
    suspend fun getScanAttemptById(id: UUID): ScanAttemptEntity?

    @Query("UPDATE scan_attempts SET status = :status WHERE id = :id")
    suspend fun updateScanAttemptStatus(id: UUID, status: String)
}
