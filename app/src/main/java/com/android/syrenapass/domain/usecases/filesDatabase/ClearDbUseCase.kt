package com.android.syrenapass.domain.usecases.filesDatabase
import com.android.syrenapass.domain.repositories.FilesRepository
import javax.inject.Inject

class ClearDbUseCase @Inject constructor(private val repository: FilesRepository){
  suspend operator fun invoke() {
    repository.clearDb()
  }
}
