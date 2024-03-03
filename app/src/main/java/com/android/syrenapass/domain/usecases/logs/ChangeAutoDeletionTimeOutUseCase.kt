package com.android.syrenapass.domain.usecases.logs

import com.android.syrenapass.domain.repositories.LogsRepository
import javax.inject.Inject

class ChangeAutoDeletionTimeOutUseCase @Inject constructor(private val repository: LogsRepository) {
  suspend operator fun invoke(timeout: Int) {
    repository.changeAutoDeletionTimeOut(timeout)
  }
}
