package com.android.syrenapass.domain.usecases.filesDatabase

import android.net.Uri
import com.android.syrenapass.domain.repositories.FilesRepository
import javax.inject.Inject

class ChangeFilePriorityUseCase @Inject constructor(private val repository: FilesRepository) {
  suspend operator fun invoke(priority: Int, uri: Uri) {
    repository.changeFilePriority(priority, uri)
  }
}
