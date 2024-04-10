package com.momo.builder.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.momo.builder.message

class HomeEventBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra("reason")
            if (reason == "homekey" || reason == "recentapps") {
                message.value = "click_homekey"
            }
        }
    }

}