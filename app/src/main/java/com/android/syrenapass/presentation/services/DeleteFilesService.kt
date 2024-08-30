package com.android.syrenapass.presentation.services

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.FileDomain
import com.android.syrenapass.domain.entities.FileType
import com.android.syrenapass.domain.usecases.filesDatabase.DeleteMyFileUseCase
import com.android.syrenapass.domain.usecases.filesDatabase.GetFilesDbUseCase
import com.android.syrenapass.domain.usecases.logs.WriteToLogsUseCase
import com.android.syrenapass.domain.usecases.passwordManager.CheckPasswordUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * Service for deleting selected files
 */
@HiltWorker
class DeleteFilesService @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted workerParams: WorkerParameters,
  private val writeToLogsUseCase: WriteToLogsUseCase,
  private val getFilesDbUseCase: GetFilesDbUseCase,
  private val deleteMyFileUseCase: DeleteMyFileUseCase,
  private val checkPasswordUseCase: CheckPasswordUseCase,
  private val getSettingsUseCase: GetSettingsUseCase
) :
  CoroutineWorker(context, workerParams) {
  private var filesList = listOf<FileDomain>()

  override suspend fun doWork(): Result {
    val password = inputData.getString(PASSWORD) ?: return Result.failure()
    val checked = inputData.getBoolean(CHECKED,false)
    if (!checked && !getSettingsUseCase().first().active) {
      return Result.failure()
    } //checking is deletion activated
    if (!checkPasswordUseCase(password.toCharArray())) {
      return Result.failure() //checking password
    }
    filesList = getFilesDbUseCase().first() //getting list of files
    writeToLogsUseCase(context.getString(R.string.deletion_started))
    removeAll()
    return Result.success()
  }

  private fun FileDomain.toDocumentFile(): DocumentFile? {
    return if (fileType == FileType.DIRECTORY) {
      DocumentFile.fromTreeUri(context, uri)
    } else {
      DocumentFile.fromSingleUri(context, uri)
    }
  }

  /**
   * Removing all files
   */
  private suspend fun removeAll() {
    coroutineScope {
      filesList.sortedByDescending { it.priority }.groupBy { it.priority }.forEach { it1 ->
        val jobs: List<Job> = it1.value.map {
          removeFile(this, it)
        }
        jobs.joinAll()
      } //sorting and grouping files by priority
    }
  }

  /**
   * Preprocessing and carrying out file or folder removal and analyzing results
   */
  private suspend fun removeFile(coroutineScope: CoroutineScope, file: FileDomain): Job {
    return coroutineScope.launch(Dispatchers.IO) {
      val name = file.name
      val isDirectory = file.fileType == FileType.DIRECTORY
      val id = if (isDirectory) {
        R.string.deletion_folder
      } else {
        R.string.deletion_file
      }
      writeToLogsUseCase(
        context.getString(id, name)
      )
      val df = try {
        file.toDocumentFile() ?: throw RuntimeException()
      } catch (e: Exception) {
        writeAboutDeletionError(isDirectory, name, context.getString(R.string.access_error))
        return@launch
      }
      val result: Pair<Int, Int> = deleteFile(df, file.name, isDirectory)
      processDeletionResults(result, isDirectory, file)
    }
  }

  /**
   * Deleting file or folder
   */
  private suspend fun deleteFile(
    df: DocumentFile,
    path: String,
    isDirectory: Boolean
  ): Pair<Int, Int> {
    if (isDirectory) {
      val resultFiles = mutableListOf<Pair<Int, Int>>()
      val resultDirs = mutableListOf<Deferred<Pair<Int, Int>>>()
      df.listFiles().forEach {
        if (it.isDirectory) {
          resultDirs += coroutineScope {
            async(Dispatchers.IO) {
              deleteFile(
                it,
                it.name ?: "Unknown",
                true
              )
            }
          }
        } else {
          resultFiles += deleteFile(it, it.name ?: "Unknown", false)
        }
      }
      val result = resultFiles + resultDirs.awaitAll()
      var (success, all) = listOf(0, 0)
      result.forEach { success += it.first; all += it.second }
      if (all==0 || success / all > 0.5) {
        if (!df.delete()) {
          writeAboutDeletionError(
            true, path,
            "File not deleted"
          )
        }
      }
      return Pair(success, all)
    }
    if (!df.delete()) {
      writeAboutDeletionError(
        false, path,
        "Directory not deleted"
      )
      return Pair(0, 1)
    }
    return Pair(1, 1)
  }

  /**
   * Writing about deletion errors
   */
  private suspend fun writeAboutDeletionError(isDirectory: Boolean, name: String, error: String) {
    val id1 = if (isDirectory) {
      R.string.folder_deletion_error
    } else {
      R.string.file_deletion_error
    }
    writeToLogsUseCase(
      context.getString(
        id1,
        name, error
      )
    )
  }

  /**
   * Processing results of file deletion and writing to logs
   */
  private suspend fun processDeletionResults(
    result: Pair<Int, Int>,
    isDirectory: Boolean,
    file: FileDomain
  ) {
    if (isDirectory) {
      if (result.second == 0) {
        deleteMyFileUseCase(file.uri)
        writeToLogsUseCase(
          context.getString(
            R.string.folder_deletion_success,
            file.name,
            100
          )
        )
        return
      }
      val percent = result.first / result.second
      if (percent > 0.5) {
        deleteMyFileUseCase(file.uri)
        writeToLogsUseCase(
          context.getString(
            R.string.folder_deletion_success,
            file.name,
            percent * 100
          )
        )
        return
      }
      writeToLogsUseCase(
        context.getString(
          R.string.folder_deletion_failed,
          file.name,
          percent * 100
        )
      )
      return
    }
    if (result.first == 1) {
      deleteMyFileUseCase(file.uri)
      writeToLogsUseCase(
        context.getString(
          R.string.deletion_success,
          file.name
        )
      )
      return
    }
    writeToLogsUseCase(
      context.getString(
        R.string.deletion_failed,
        file.name
      )
    )
  }

  companion object {
    private const val WORK_NAME = "delete_files_service"
    const val PASSWORD = "password"
    const val CHECKED = "checked"
    fun start(context: Context, password: String,checked: Boolean) {
      val workManager = WorkManager.getInstance(context)
      workManager.enqueueUniqueWork(
        WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<DeleteFilesService>().setInputData(
          workDataOf(PASSWORD to password,CHECKED to checked)
        ).build()
      )
    }

  }
}
