package com.android.syrenapass.presentation.receivers
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import com.android.syrenapass.domain.usecases.bruteforce.OnRightPasswordUseCase
import com.android.syrenapass.domain.usecases.bruteforce.OnWrongPasswordUseCase
import com.android.syrenapass.domain.usecases.permissions.SetAdminActiveUseCase
import com.android.syrenapass.presentation.services.MyJobIntentService
import com.android.syrenapass.presentation.services.MyWorkService
import com.android.syrenapass.presentation.services.ServicesLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeviceAdminReceiver: DeviceAdminReceiver() {

    @Inject
    lateinit var onRightPasswordUseCase: OnRightPasswordUseCase

    @Inject
    lateinit var onWrongPasswordUseCase: OnWrongPasswordUseCase

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var servicesLauncher: ServicesLauncher

    @Inject
    lateinit var setAdminActiveUseCase: SetAdminActiveUseCase

    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordFailed(context, intent, user)
        coroutineScope.launch {
            if (onWrongPasswordUseCase()) {
                if (context.getSystemService(UserManager::class.java).isUserUnlocked) {
                    MyWorkService.start(context)
                } else {
                    MyJobIntentService.start(context)
                }
            }
        }
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordSucceeded(context, intent, user)
        coroutineScope.launch {
            onRightPasswordUseCase()
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        coroutineScope.launch {
            setAdminActiveUseCase(true)
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        coroutineScope.launch {
            setAdminActiveUseCase(false)
        }
        super.onDisabled(context, intent)
    }
}
