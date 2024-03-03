package com.android.syrenapass.domain.usecases.logs

import com.android.syrenapass.domain.repositories.LogsRepository
import javax.inject.Inject

class ChangeLogsEnabledUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke() {
    repository.changeLogsEnabled()
  }
}
