package com.android.syrenapass.domain.usecases.logs

import com.android.syrenapass.domain.repositories.LogsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableDaysUseCase @Inject constructor(private val repository: LogsRepository) {
  operator fun invoke(): Flow<List<Long>> {
    return repository.getAvailableDays()
  }
}
