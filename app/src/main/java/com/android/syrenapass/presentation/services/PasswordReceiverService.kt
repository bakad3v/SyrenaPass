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
import com.android.syrenapass.domain.usecases.passwordManager.CheckPasswordUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.domain.usecases.settings.SetRunOnBootUseCase
import com.android.syrenapass.domain.usecases.settings.SetServiceStatusUseCase
import com.android.syrenapass.domain.usecases.usb.GetUsbSettingsUseCase
import com.android.syrenapass.superuser.superuser.SuperUserManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    lateinit var superUserManager: SuperUserManager

    @Inject
    lateinit var getUsbSettingsUseCase: GetUsbSettingsUseCase

    override fun onCreate() {
        super.onCreate()
        coroutineScope.launch {
            setServiceStatusUseCase(true)
            val settings = getSettingsUseCase().first()
            if (settings.stopLogdOnBoot) {
                try {
                    superUserManager.getSuperUser().stopLogd()
                } catch (e: Exception) {

                }
            }
        }
        val intentFilter1 = IntentFilter(Intent.ACTION_USER_UNLOCKED)
        val receiver1 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.w("awaken","awaken0")
                coroutineScope.launch {
                    Log.w("awaken","awaken")
                    if (getSettingsUseCase().first().runOnBoot) {
                        Log.w("awaken","start")
                        withContext(Dispatchers.IO) {
                            runnerAFU.runTask()
                        }
                        setRunOnBootUseCase(false)
                    }
                }
            }
        }
        registerReceiver(receiver1, intentFilter1)
        Log.w("awaken","setup")
        val intentFilter3 = IntentFilter("android.hardware.usb.action.USB_STATE").apply { addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) }.apply { addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED) }
        val receiver3 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED || intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    Log.w("work1", "launcher")
                    coroutineScope.launch {
                        val settings = getUsbSettingsUseCase().first()
                        if (settings.runOnConnection)
                            runActions()
                    }
                    return
                }
                val manager = getSystemService(USB_SERVICE) as UsbManager
                if (manager.deviceList?.size != 0 || manager.accessoryList?.size !=0) {
                    Log.w("work", "launcher")
                    coroutineScope.launch {
                        val settings = getUsbSettingsUseCase().first()
                        if (settings.runOnConnection) {
                            runActions()
                        }
                    }
                }
            }
        }
        registerReceiver(receiver3, intentFilter3)
        keyguardManager = getSystemService(KeyguardManager::class.java)
    }

    private fun checkPassword(pass: CharArray) {
        Log.w("work_passwordChecking", pass.joinToString (""))
        coroutineScope.launch {
            if (checkPasswordUseCase(pass)) {
                runActions()
            }
            password = mutableListOf()
        }
    }

    private suspend fun runActions() {
        withContext(Dispatchers.IO) {
            runnerBFU.runTask()
            if (applicationContext.getSystemService(UserManager::class.java).isUserUnlocked) {
                runnerAFU.runTask()
            } else
                setRunOnBootUseCase(true)
        }
    }

    private fun updatePassword(text: String) {
        Log.w("work_password_text", password.joinToString(""))
        Log.w("work_password_current", text)
        val ignoreChars = text.count { it == IGNORE_CHAR }
        if (ignoreChars == 0 && text.length != 1) {
            checkPassword(password.toCharArray())
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
            try {
                password[index] = text[index]
            } catch (e: java.lang.IndexOutOfBoundsException) {

            }
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
