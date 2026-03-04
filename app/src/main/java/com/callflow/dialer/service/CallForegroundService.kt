package com.callflow.dialer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telecom.Call
import com.callflow.dialer.R
import com.callflow.dialer.data.ActiveCallState
import com.callflow.dialer.ui.call.CallActivity

class CallForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = intent?.getStringExtra(EXTRA_STATE) ?: STATE_ONGOING
        val notification = buildNotification(this, state)
        startForeground(NOTIF_ID, notification)
        return START_STICKY
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "CallFlow Calls", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    companion object {
        private const val CHANNEL_ID = "callflow_calls"
        private const val NOTIF_ID = 2001
        private const val EXTRA_STATE = "state"
        private const val STATE_ONGOING = "ongoing"

        fun sync(context: Context, calls: List<ActiveCallState>) {
            val shouldRun = calls.any { it.state == Call.STATE_ACTIVE || it.state == Call.STATE_RINGING }
            if (shouldRun) {
                val state = if (calls.any { it.state == Call.STATE_RINGING }) "incoming" else "ongoing"
                context.startForegroundService(
                    Intent(context, CallForegroundService::class.java).putExtra(EXTRA_STATE, state)
                )
            } else {
                context.stopService(Intent(context, CallForegroundService::class.java))
            }
        }

        private fun buildNotification(context: Context, state: String): Notification {
            val launchIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, CallActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val hangupIntent = PendingIntent.getBroadcast(
                context,
                1,
                Intent(context, CallActionReceiver::class.java).putExtra(CallActionReceiver.EXTRA_ACTION, CallActionReceiver.ACTION_HANGUP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val answerIntent = PendingIntent.getBroadcast(
                context,
                2,
                Intent(context, CallActionReceiver::class.java).putExtra(CallActionReceiver.EXTRA_ACTION, CallActionReceiver.ACTION_ANSWER),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val declineIntent = PendingIntent.getBroadcast(
                context,
                3,
                Intent(context, CallActionReceiver::class.java).putExtra(CallActionReceiver.EXTRA_ACTION, CallActionReceiver.ACTION_DECLINE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val person = Person.Builder().setName("Call").build()
            val style = if (state == "incoming") {
                Notification.CallStyle.forIncomingCall(person, declineIntent, answerIntent)
            } else {
                Notification.CallStyle.forOngoingCall(person, hangupIntent)
            }

            return Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle(if (state == "incoming") "Incoming call" else "Ongoing call")
                .setContentIntent(launchIntent)
                .setStyle(style)
                .setOngoing(true)
                .build()
        }
    }
}
