package com.android.syrenapass.presentation.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

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
                .build()
            )
        }

    }
}
