package com.android.syrenapass.data.repositories

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.android.syrenapass.data.db.FileDao
import com.android.syrenapass.data.db.FileDbModel
import com.android.syrenapass.data.mappers.FileMapper
import com.android.syrenapass.domain.entities.FileDomain
import com.android.syrenapass.domain.entities.FileType
import com.android.syrenapass.domain.entities.FilesSortOrder
import com.android.syrenapass.domain.repositories.FilesRepository
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.mimeType
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Repository for usual files
 */
class FilesRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fileDaoLazy: Lazy<FileDao>,
  private val mapper: FileMapper,
  private val sortOrderFlow: MutableStateFlow<FilesSortOrder>
) : FilesRepository {

  private lateinit var fileDao: FileDao

  override fun getSortOrder() = sortOrderFlow.asStateFlow()


  /**
   * Function for getting sorted flow of files
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getFilesDb() : Flow<List<FileDomain>> = sortOrderFlow.flatMapLatest {
    val filesFlow = when (it) {
      FilesSortOrder.NAME_ASC -> fileDao.getDataSortedByPathAsc()
      FilesSortOrder.NAME_DESC -> fileDao.getDataSortedByPathDesc()
      FilesSortOrder.SIZE_ASC -> fileDao.getDataSortedBySizeAsc()
      FilesSortOrder.SIZE_DESC -> fileDao.getDataSortedBySizeDesc()
      FilesSortOrder.PRIORITY_ASC -> fileDao.getDataSortedByPriorityAsc()
      FilesSortOrder.PRIORITY_DESC -> fileDao.getDataSortedByPriorityDesc()
    }
    filesFlow.map { files -> mapper.mapDbListToDtList(files) }
  }

  /**
   * Function for DAO initialization after database unlocking
   */
  override fun init() {
    fileDao = fileDaoLazy.get()
  }

  /**
   * Function to clear database
   */
  override suspend fun clearDb() {
    fileDao.clearDb()
  }

  /**
   * Function to change priority of file.
   */
  override suspend fun changeFilePriority(priority: Int, uri: Uri) {
    fileDao.changePriority(priority, uri.toString())
  }

  /**
   * Function to change files sort order
   */
  override suspend fun changeSortOrder(sortOrder: FilesSortOrder) {
    sortOrderFlow.emit(sortOrder)
  }

  /**
   * Function for getting size of folder. Allows to get size of large folder rapidly using du command, can be buggy on latest versions of Android
   */
  private fun getFileSize(path: String): Long {
    val process = Runtime.getRuntime().exec(arrayOf("du", "-s", path))
    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
    bufferedReader.use {
      val line = it.readLine()
      val result = try {
        line.split("\t")[0].toLong()
      } catch (e: Exception) {
        0
      }
      return result
    }
  }

  /**
   * Function for converting file size in bytes to human-readable format.
   */
  private fun Long.convertToHumanFormat(): String {
    var number = this
    val names = listOf("B","KB", "MB", "GB", "TB")
    var i = 0
    while (number > 1023) {
      number /= 1024
      i++
    }
    return "${number.toInt()} ${names[i]}"
  }

  /**
   * Function for file inserting. Gets size of file and converts it to human format, sets default priority to 0, determines the file type.
   */
  override suspend fun insertMyFile(uri: Uri, isDirectory: Boolean) {
    val df = if(isDirectory) {
      DocumentFile.fromTreeUri(context,uri)
    } else {
      DocumentFile.fromSingleUri(context,uri)
    }?: throw RuntimeException("Can't get file or directory for uri $uri")
    val size = if (isDirectory) {
      getFileSize(df.getAbsolutePath(context)) * 1024
    } else {
      df.length()
    }
    val fileType = if (isDirectory) {
      FileType.DIRECTORY
    } else {
      if (df.mimeType?.startsWith("image/")==true) {
        FileType.IMAGE
      } else {
        FileType.USUAL_FILE
      }
    }
    fileDao.upsert(FileDbModel(uri = uri.toString(), name = df.name?:"No name",size = size, priority = 0, fileType = fileType, sizeFormatted = size.convertToHumanFormat()))
  }

  /**
   * Function to delete file
   */
  override suspend fun deleteMyFile(uri: Uri) {
    fileDao.delete(uri.toString())
  }

}
