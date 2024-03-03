package com.android.syrenapass.domain.usecases.filesDatabase

import com.android.syrenapass.domain.entities.FilesSortOrder
import com.android.syrenapass.domain.repositories.FilesRepository
import javax.inject.Inject

class ChangeSortOrderUseCase @Inject constructor(private val repository: FilesRepository){
  suspend operator fun invoke(sortOrder: FilesSortOrder) {
    repository.changeSortOrder(sortOrder)
  }
}
