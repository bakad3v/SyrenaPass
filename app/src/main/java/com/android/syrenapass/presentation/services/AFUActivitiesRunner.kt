package com.android.syrenapass.presentation.services

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.FileDomain
import com.android.syrenapass.domain.entities.FileType
import com.android.syrenapass.domain.usecases.filesDatabase.DeleteMyFileUseCase
import com.android.syrenapass.domain.usecases.filesDatabase.GetFilesDbUseCase
import com.android.syrenapass.domain.usecases.logs.GetLogsDataUseCase
import com.android.syrenapass.domain.usecases.logs.WriteToLogsUseCase
import com.android.syrenapass.domain.usecases.permissions.GetPermissionsUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.superuser.superuser.SuperUserManager
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AFUActivitiesRunner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val writeToLogsUseCase: WriteToLogsUseCase,
    private val getFilesDbUseCase: GetFilesDbUseCase,
    private val deleteMyFileUseCase: DeleteMyFileUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val getPermissionsUseCase: GetPermissionsUseCase,
    private val superUserManager: SuperUserManager,
    private val getLogsDataUseCase: GetLogsDataUseCase
) {

    private val mutex = Mutex()
    private var isRunning = false
    private var logsAllowed: Boolean? = null

    suspend fun runTask() {
        mutex.withLock {
            if (isRunning) {
                return
            }
            isRunning = true
        }
        withContext(Dispatchers.IO) {
            runAFUActivity()
            mutex.withLock {
                isRunning = false
            }
        }
    }

    private suspend fun runAFUActivity() {
        if (!getSettingsUseCase().first().deleteFiles) {
            return
        }
        try {
            logsAllowed = getLogsDataUseCase().first().logsEnabled
            writeToLogs(R.string.deletion_started)
        } catch (e: Exception) {
            return
        }
        val (settings, files) = try {
            Pair(getSettingsUseCase().first(),
                getFilesDbUseCase().first())
        } catch (e: Exception) {
            writeToLogs(R.string.getting_data_error, e.stackTraceToString())
            return
        }
        removeAll(files)
        writeToLogs(R.string.deletion_completed)
        val permissions = getPermissionsUseCase().first()
        if (settings.removeItself && (permissions.isRoot || permissions.isOwner)) {
            try {
                superUserManager.getSuperUser().uninstallApp(context.packageName)
            } catch (e: Exception) {
                writeToLogsUseCase(context.getString(R.string.uninstallation_failed,e.stackTraceToString()))
            }
        }
    }

    private suspend fun writeToLogs(rId: Int, vararg obj: Any) {
        if (logsAllowed == true)
            writeToLogsUseCase(context.getString(rId, obj))
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
    private suspend fun removeAll(filesList: List<FileDomain>) {
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
            writeToLogs(id, name)
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
            if (all == 0 || success / all > 0.5) {
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
        writeToLogs(id1, name, error)
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
                writeToLogs(
                    R.string.folder_deletion_success,
                    file.name,
                    100
                )
                return
            }
            val percent = result.first / result.second
            if (percent > 0.5) {
                deleteMyFileUseCase(file.uri)
                writeToLogs(
                    R.string.folder_deletion_success,
                    file.name,
                    percent * 100
                )
                return
            }
            writeToLogs(
                R.string.folder_deletion_failed,
                file.name,
                percent * 100
            )
            return
        }
        if (result.first == 1) {
            deleteMyFileUseCase(file.uri)
            writeToLogs(
                R.string.deletion_success,
                file.name
            )
            return
        }
        writeToLogs(
            R.string.deletion_failed,
            file.name
        )
    }
}