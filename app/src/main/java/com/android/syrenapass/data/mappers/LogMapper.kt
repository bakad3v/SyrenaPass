package com.android.syrenapass.data.mappers

import com.android.syrenapass.TopLevelFunctions.formatTime
import com.android.syrenapass.data.entities.LogList
import com.android.syrenapass.domain.entities.LogEntity
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import javax.inject.Inject

class LogMapper @Inject constructor() {
  fun mapDataStoreToDt(logs: LogList, day: Long): LogEntity {
    val logsFormatted = logs.list.joinToString("\n") {
      val time =  Instant.fromEpochMilliseconds(it.date).toLocalDateTime(
        TimeZone.currentSystemDefault()
      ).formatTime()
      "<span style=\"color: %s;\">$time</span> <span style=\"color: %s;\">${it.entry}</span><br>"
    }
    return LogEntity(LocalDateTime.of(LocalDate.ofEpochDay(day), LocalTime.now()), logsFormatted)
  }
}
