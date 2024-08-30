package com.android.syrenapass.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.syrenapass.presentation.services.DeleteFilesService.Companion.PASSWORD

/**
 * Class for receiving broadcast from Duress
 */
class DuressListener: BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent!!.action==ACTION) {
      //Log
      val password = intent.getStringExtra(PASSWORD) ?: return
      //Log
      DeleteFilesService.start(context!!,password,false)
    }
  }

  companion object {
    const val ACTION = "com.android.syrenapass.action.TRIGGER"
  }
}
