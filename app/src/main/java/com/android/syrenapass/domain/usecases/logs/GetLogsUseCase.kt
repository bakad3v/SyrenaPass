package com.android.syrenapass.domain.usecases.logs

import com.android.syrenapass.domain.repositories.LogsRepository
import javax.inject.Inject

class GetLogsUseCase @Inject constructor(private val repository: LogsRepository) {
  operator fun invoke() = repository.getLogsText()

}
