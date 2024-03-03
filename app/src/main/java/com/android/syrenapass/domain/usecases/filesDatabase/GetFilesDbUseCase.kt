package com.android.syrenapass.domain.usecases.filesDatabase

import com.android.syrenapass.domain.repositories.FilesRepository
import javax.inject.Inject

class GetFilesDbUseCase @Inject constructor(private val repository: FilesRepository){
  operator fun invoke() = repository.getFilesDb()
}
