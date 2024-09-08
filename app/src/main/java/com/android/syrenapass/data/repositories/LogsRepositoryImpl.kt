package com.android.syrenapass.data.repositories


import android.content.Context
import com.android.syrenapass.TopLevelFunctions.getEpochDays
import com.android.syrenapass.TopLevelFunctions.getMillis
import com.android.syrenapass.data.entities.LogDatastore
import com.android.syrenapass.data.mappers.LogMapper
import com.android.syrenapass.data.serializers.LogsDataSerializer
import com.android.syrenapass.data.serializers.LogsSerializer
import com.android.syrenapass.domain.entities.LogEntity
import com.android.syrenapass.domain.entities.LogsData
import com.android.syrenapass.domain.repositories.LogsRepository
import com.android.syrenapass.datastoreDBA.dataStoreDirectBootAware
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
  private val dayFlow: MutableStateFlow<Long>,
  private val mapper: LogMapper,
  logsDataSerializer: LogsDataSerializer,
  logsSerializer: LogsSerializer
) : LogsRepository {

  private val Context.logsDataStore by dataStoreDirectBootAware(ENTRIES_DATASTORE_NAME, logsSerializer)

  private val Context.logsDataDataStore by dataStoreDirectBootAware(DATASTORE_NAME, logsDataSerializer)

  /**
   * Function to get logs text
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getLogsText(): Flow<LogEntity> = dayFlow.flatMapLatest { day ->
    context.logsDataStore.data.map { it.getLogsForDay(day) }.map {  mapper.mapDataStoreToDt(it,dayFlow.value) }
  }

  /**
   * Function to get data about logs (are logs enabled, period of automatically clearing)
   */
  override fun getLogsData(): Flow<LogsData> = context.logsDataDataStore.data

  /**
   * Function to get days for which logs are available
   */
  override fun getAvailableDays() = context.logsDataStore.data.map { it.getAvailableDays() }

  /**
   * Function for initializing logs DAO and clearing old logs after unlocking database.
   */
  override suspend fun init() {
      val availableDays = context.logsDataStore.data.first().getAvailableDays()
      val period = context.logsDataDataStore.data.first().logsAutoRemovePeriod
      val currentDay = dayFlow.value
      val toDelete = availableDays.filter { it < currentDay - period }
      context.logsDataStore.updateData { it.deleteLogsForDays(toDelete) }
  }

  /**
   * Function to clear logs for selected day
   */
  override suspend fun clearLogsForDay(day: String) {
    context.logsDataStore.updateData { it.deleteLogsForDays(listOf(LocalDate.parse(day).toEpochDay())) }
  }

  /**
   * Function to change logs auto deletion timeout
   */
  override suspend fun changeAutoDeletionTimeOut(timeout: Int) {
    context.logsDataDataStore.updateData {
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
    context.logsDataDataStore.updateData {
      it.copy(logsEnabled = !it.logsEnabled)
    }
  }

  override suspend fun writeToLogs(string: String) {
    if (!context.logsDataDataStore.data.first().logsEnabled) {
      return
    }
    val dateTime = LocalDateTime.now()
    val date = dateTime.getMillis()
    val day = dateTime.getEpochDays()
    context.logsDataStore.updateData {
     it.insertLogEntry(LogDatastore(id = it.list.size,date=date, day = day, entry = string))
    }
    if (day!=dayFlow.value) {
      dayFlow.emit(day)
    }
  }

  class LogsDAONotInitialized: Exception("logsDAO not initialized")

  companion object {
    private const val DATASTORE_NAME = "logs_datastore.json"
    private const val ENTRIES_DATASTORE_NAME = "logs_entries_datastore.json"
  }
}
