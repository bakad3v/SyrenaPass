package com.android.syrenapass.domain.usecases.logs

import com.android.syrenapass.domain.repositories.LogsRepository
import java.time.LocalDateTime
import javax.inject.Inject

class LookLogsForDayUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke(localDateTime: LocalDateTime) {
    repository.lookLogsForDay(localDateTime)
  }

}
