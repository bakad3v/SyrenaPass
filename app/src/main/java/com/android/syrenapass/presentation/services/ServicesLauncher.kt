package com.android.syrenapass.presentation.services

import android.content.Context
import android.os.UserManager
import com.android.syrenapass.domain.usecases.settings.SetRunOnBootUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServicesLauncher @Inject constructor(private val setRunOnBootUseCase: SetRunOnBootUseCase, @ApplicationContext private val context: Context){
    suspend operator fun invoke() {
        MyJobIntentService.start(context)
        if (context.getSystemService(UserManager::class.java).isUserUnlocked) {
            MyWorkService.start(context)
        } else
            setRunOnBootUseCase(true)
    }
}