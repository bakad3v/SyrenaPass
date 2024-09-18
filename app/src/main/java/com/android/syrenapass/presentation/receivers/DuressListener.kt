package com.android.syrenapass.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Class for receiving broadcast from Duress
 */
class DuressListener: BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent!!.action== ACTION) {
      Log.w("receiver","usb wasted")
    }
  }

  companion object {
    const val ACTION = "com.android.syrenapass.action.TRIGGER"
  }
}
