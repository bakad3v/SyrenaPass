package com.android.syrenapass.domain.usecases.logs

import com.android.syrenapass.domain.repositories.LogsRepository
import javax.inject.Inject

class WriteToLogsUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke(string: String) {
    repository.writeToLogs(string)
  }

}
