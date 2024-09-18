package com.android.syrenapass.presentation.services

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
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
class MyWorkService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val runnerAFU: AFUActivitiesRunner,
    private val runnerBFU: BFUActivitiesRunner
) :
    CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {
        runnerBFU.runTask()
        runnerAFU.runTask()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "my_worker"
        fun start(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<MyWorkService>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            )
        }

    }
}
