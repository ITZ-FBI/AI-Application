package com.callflow.dialer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.getStringExtra(EXTRA_ACTION)) {
            ACTION_ANSWER -> CurrentCallStore.answer()
            ACTION_DECLINE -> CurrentCallStore.reject()
            ACTION_HANGUP -> CurrentCallStore.disconnect()
        }
    }

    companion object {
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_ANSWER = "answer"
        const val ACTION_DECLINE = "decline"
        const val ACTION_HANGUP = "hangup"
    }
}
