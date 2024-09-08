package com.android.syrenapass.domain.repositories

import android.net.Uri
import com.android.syrenapass.domain.entities.FileDomain
import com.android.syrenapass.domain.entities.FilesSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FilesRepository {
  suspend fun clearDb()
  suspend fun changeSortOrder(sortOrder: FilesSortOrder)
  suspend fun changeFilePriority(priority: Int, uri: Uri)
  suspend fun insertMyFile(uri: Uri, isDirectory: Boolean)
  suspend fun deleteMyFile(uri: Uri)
  fun getFilesDb(): Flow<List<FileDomain>>
  fun getSortOrder(): StateFlow<FilesSortOrder>
}
