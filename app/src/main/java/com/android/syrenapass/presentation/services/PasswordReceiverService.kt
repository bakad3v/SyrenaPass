package com.android.syrenapass.presentation.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.UserManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.android.syrenapass.domain.usecases.logs.WriteToLogsUseCase
import com.android.syrenapass.domain.usecases.passwordManager.CheckPasswordUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.domain.usecases.settings.SetRunOnBootUseCase
import com.android.syrenapass.domain.usecases.settings.SetServiceStatusUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PasswordReceiverService : AccessibilityService() {
    private var keyguardManager: KeyguardManager? = null
    private var password = mutableListOf<Char>()

    @Inject
    lateinit var checkPasswordUseCase: CheckPasswordUseCase

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var setServiceStatusUseCase: SetServiceStatusUseCase

    @Inject
    lateinit var setRunOnBootUseCase: SetRunOnBootUseCase

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    @Inject
    lateinit var runnerBFU: BFUActivitiesRunner

    @Inject
    lateinit var runnerAFU: AFUActivitiesRunner

    @Inject
    lateinit var writeToLogsUseCase: WriteToLogsUseCase

    override fun onCreate() {
        super.onCreate()
        coroutineScope.launch {
            setServiceStatusUseCase(true)
        }
        val intentFilter1 = IntentFilter(Intent.ACTION_USER_UNLOCKED)
        val receiver1 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                coroutineScope.launch {
                    if (getSettingsUseCase().first().runOnBoot) {
                        runnerAFU.runTask()
                        setRunOnBootUseCase(false)
                    }
                }
            }
        }
        registerReceiver(receiver1, intentFilter1)
        val intentFilter2 = IntentFilter(Intent.ACTION_USER_PRESENT)
        val receiver2 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.w("receiver", "USER_PRESENT 2")
            }
        }
        registerReceiver(receiver2, intentFilter2)
        val intentFilter3 = IntentFilter("android.hardware.usb.action.USB_STATE").apply { addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) }.apply { addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED) }
        val receiver3 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED || intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    coroutineScope.launch {
                        runActions()
                    }
                    return
                }
                val manager = getSystemService(USB_SERVICE) as UsbManager
                if (manager.deviceList?.size != 0 || manager.accessoryList?.size !=0) {
                    Log.w("work", "launcher")
                    coroutineScope.launch {
                        runActions()
                    }
                }
            }
        }
        registerReceiver(receiver3, intentFilter3)
        keyguardManager = getSystemService(KeyguardManager::class.java)
    }

    private fun checkPassword(password: CharArray) {
        Log.w("work_passwordChecking", password.joinToString { "" })
        coroutineScope.launch {
            if (checkPasswordUseCase(password)) {
                runActions()
            }
        }
    }

    private suspend fun runActions() {
        runnerBFU.runTask()
        if (applicationContext.getSystemService(UserManager::class.java).isUserUnlocked) {
            runnerAFU.runTask()
        } else
            setRunOnBootUseCase(true)
    }

    private fun updatePassword(text: String) {
        Log.w("work_password_text", password.joinToString(""))
        Log.w("work_password_current", text)
        val ignoreChars = text.count { it == IGNORE_CHAR }
        if (ignoreChars == 0 && text.length > 1) {
            checkPassword(password.toCharArray())
            password = mutableListOf()
            return
        }
        if (password.size > text.length) {
            password = password.subList(0, text.length)
        }
        if (ignoreChars == text.length)
            return
        val index = text.indexOfFirst { it != IGNORE_CHAR }
        if (index == password.size) {
            password.add(text[index])
        } else {
            password[index] = text[index]
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (keyguardManager?.isDeviceLocked != true ||
            event?.isEnabled != true
        ) return
        updatePassword(event.text.joinToString(""))
    }

    override fun onInterrupt() {
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        runBlocking {
           setServiceStatusUseCase(false)
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val IGNORE_CHAR = '•'
    }
}
