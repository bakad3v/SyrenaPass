package com.android.syrenapass.presentation.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.android.syrenapass.domain.usecases.passwordManager.CheckPasswordUseCase
import com.android.syrenapass.domain.usecases.settings.SetServiceStatusUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

  override fun onCreate() {
    super.onCreate()
    coroutineScope.launch {
      setServiceStatusUseCase(true)
    }
    keyguardManager = getSystemService(KeyguardManager::class.java)
  }

  private fun checkPassword(password: CharArray) {
    Log.w("passwordChecking",password.toString())
    coroutineScope.launch {
      if (checkPasswordUseCase(password)) {
        DeleteFilesService.start(applicationContext, password.toString(), true)
      }
    }
  }
  private fun updatePassword(text: String) {
    Log.w("password_text",password.joinToString(""))
    Log.w("password_current",text)
    val ignoreChars = text.count { it == IGNORE_CHAR }
    if (ignoreChars == 0 && text.length > 1) {
      checkPassword(password.toCharArray())
      password = mutableListOf()
      return
    }
    if (password.size > text.length) {
      password = password.subList(0,text.length)
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

  override fun onServiceConnected() {
    super.onServiceConnected()
    serviceInfo = serviceInfo.apply {
      eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
        AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
      flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
    }
  }

  override fun onInterrupt() {}

  override fun onDestroy() {
    super.onDestroy()
    coroutineScope.launch {
      setServiceStatusUseCase(false)
    }
    coroutineScope.cancel()
  }

  companion object {
    private const val IGNORE_CHAR = '•'
  }
}