package com.android.syrenapass.domain.repositories

import com.android.syrenapass.domain.entities.LogEntity
import com.android.syrenapass.domain.entities.LogsData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface LogsRepository {

  suspend fun clearLogsForDay(day: String)
  suspend fun changeAutoDeletionTimeOut(timeout: Int)
  suspend fun writeToLogs(string: String)
  suspend fun lookLogsForDay(localDateTime: LocalDateTime)
  suspend fun changeLogsEnabled()
  fun getLogsText(): Flow<LogEntity>
  fun getLogsData(): Flow<LogsData>
  suspend fun init()
  fun getAvailableDays(): Flow<Set<Long>>
}
