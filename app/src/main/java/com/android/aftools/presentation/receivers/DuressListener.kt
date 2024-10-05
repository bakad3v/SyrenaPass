package com.android.aftools.presentation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Class for receiving broadcast from Duress
 */
class DuressListener: BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent!!.action== ACTION) {
    }
  }

  companion object {
    const val ACTION = "com.android.aftools.action.TRIGGER"
  }
}
