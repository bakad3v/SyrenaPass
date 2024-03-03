package com.android.syrenapass.domain.usecases.filesDatabase

import com.android.syrenapass.domain.entities.FilesSortOrder
import com.android.syrenapass.domain.repositories.FilesRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetSortOrderUseCase @Inject constructor(private val repository: FilesRepository) {
  operator fun invoke(): StateFlow<FilesSortOrder> {
    return repository.getSortOrder()
  }
}
