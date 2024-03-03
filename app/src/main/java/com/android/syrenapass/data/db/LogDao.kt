package com.android.syrenapass.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
/**
 * DAO for log entries
 */
@Dao
interface LogDao {

  @Query("SELECT DISTINCT day from LogDbModel")
  fun getAvailableDays() : Flow<List<Long>>

  @Query("SELECT * FROM LogDbModel WHERE day=:day")
  fun getLogsForDay(day: Long) : Flow<List<LogDbModel>>

  @Query("DELETE FROM LogDbModel WHERE day IN (:days)")
  suspend fun deleteLogsForDays(days: List<Long>)

  @Insert
  suspend fun insertLogEntry(entry: LogDbModel)

}
