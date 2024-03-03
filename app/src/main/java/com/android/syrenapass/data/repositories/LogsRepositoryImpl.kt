package com.android.syrenapass.data.repositories


import android.content.Context
import androidx.datastore.dataStore
import com.android.syrenapass.TopLevelFunctions.getEpochDays
import com.android.syrenapass.TopLevelFunctions.getMillis
import com.android.syrenapass.data.db.LogDao
import com.android.syrenapass.data.db.LogDbModel
import com.android.syrenapass.data.mappers.LogMapper
import com.android.syrenapass.data.serializers.LogsDataSerializer
import com.android.syrenapass.domain.entities.LogEntity
import com.android.syrenapass.domain.entities.LogsData
import com.android.syrenapass.domain.repositories.LogsRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Repository for logs entries
 */
class LogsRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val logDaoLazy: Lazy<LogDao>,
  private val dayFlow: MutableStateFlow<Long>,
  private val mapper: LogMapper
) : LogsRepository {

  private lateinit var logDao: LogDao

  /**
   * Function to get logs text
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getLogsText(): Flow<LogEntity> = dayFlow.flatMapLatest { day ->
    logDao.getLogsForDay(day).map {  mapper.mapDbToDt(it,dayFlow.value) }
  }

  private val Context.logsDataStore by dataStore(DATASTORE_NAME, LogsDataSerializer)

  /**
   * Function to get data about logs (are logs enabled, period of automatically clearing)
   */
  override fun getLogsData(): Flow<LogsData> = context.logsDataStore.data

  /**
   * Function to get days for which logs are available
   */
  override fun getAvailableDays() = logDao.getAvailableDays()

  /**
   * Function for initializing logs DAO and clearing old logs after unlocking database.
   */
  override suspend fun init() {
      logDao = logDaoLazy.get()
      val availableDays = logDao.getAvailableDays().first()
      val period = context.logsDataStore.data.first().logsAutoRemovePeriod
      val currentDay = dayFlow.value
      val toDelete = availableDays.filter { it < currentDay - period }
      logDao.deleteLogsForDays(toDelete)
  }

  /**
   * Function to clear logs for selected day
   */
  override suspend fun clearLogsForDay(day: String) {
    logDao.deleteLogsForDays(listOf(LocalDate.parse(day).toEpochDay()))
  }

  /**
   * Function to change logs auto deletion timeout
   */
  override suspend fun changeAutoDeletionTimeOut(timeout: Int) {
    context.logsDataStore.updateData {
      it.copy(logsAutoRemovePeriod = timeout)
    }
  }

  /**
   * Function to select logs for specified day
   */
  override suspend fun lookLogsForDay(localDateTime: LocalDateTime) {
    dayFlow.emit(localDateTime.getEpochDays())
  }

  /**
   * Function to disable or enable logging
   */
  override suspend fun changeLogsEnabled() {
    context.logsDataStore.updateData {
      it.copy(logsEnabled = !it.logsEnabled)
    }
  }

  override suspend fun writeToLogs(string: String) {
    if (!context.logsDataStore.data.first().logsEnabled) {
      return
    }
    val dateTime =  LocalDateTime.now()
    val date = dateTime.getMillis()
    val day = dateTime.getEpochDays()
    logDao.insertLogEntry(LogDbModel(date=date, day = day, entry = string))
    if (day!=dayFlow.value) {
      dayFlow.emit(day)
    }
  }

  companion object {
    private const val DATASTORE_NAME = "logs_datastore.json"
  }
}
