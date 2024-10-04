package com.android.syrenapass.presentation.services

import android.content.Context
import android.content.Intent
import android.os.UserManager
import androidx.core.app.JobIntentService
import com.android.syrenapass.domain.usecases.settings.SetRunOnBootUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MyJobIntentService: JobIntentService() {

    @Inject lateinit var coroutineScope: CoroutineScope
    @Inject lateinit var runnerBFU: BFUActivitiesRunner
    @Inject lateinit var runnerAFU: AFUActivitiesRunner
    @Inject lateinit var setRunOnBootUseCase: SetRunOnBootUseCase
    @Inject lateinit var userManager: UserManager
    companion object {
        private const val JOB_ID = 1
        fun start(context: Context) {
            enqueueWork(context, MyJobIntentService::class.java, JOB_ID, Intent())
        }
    }
    override fun onHandleWork(intent: Intent) {
        runBlocking {
            runnerBFU.runTask()
            if (userManager.isUserUnlocked) {
                runnerAFU.runTask()
            } else
                setRunOnBootUseCase(true)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}