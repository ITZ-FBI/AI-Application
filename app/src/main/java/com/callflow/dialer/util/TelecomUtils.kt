package com.callflow.dialer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager

object TelecomUtils {
    private val emergencyRegex = Regex("^(112|911|999|000|110|108)$")

    fun isEmergencyNumber(number: String): Boolean = emergencyRegex.matches(number.trim())

    fun routeEmergencyToSystemDialer(context: Context, number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun placeCall(context: Context, number: String, subscriptionId: Int? = null) {
        val telecomManager = context.getSystemService(TelecomManager::class.java) ?: return
        val extras = Bundle().apply {
            subscriptionId?.let { putInt("android.telecom.extra.SUBSCRIPTION_ID", it) }
        }
        telecomManager.placeCall(Uri.fromParts("tel", number, null), extras)
    }
}
