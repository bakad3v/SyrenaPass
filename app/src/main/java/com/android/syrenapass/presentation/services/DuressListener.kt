package com.android.syrenapass.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Class for receiving broadcast from Duress
 */
class DuressListener: BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent!!.action==ACTION) {
      DeleteFilesService.start(context!!)
    }
  }

  companion object {
    const val ACTION = "com.android.syrenapass.action.TRIGGER"
  }
}
