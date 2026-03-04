package com.callflow.dialer.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

class SubscriptionRepository(private val context: Context) {
    private val manager = context.getSystemService(SubscriptionManager::class.java)

    fun getActiveSims(): List<SimInfo> {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted || manager == null) {
            return listOf(
                SimInfo(-1, "Carrier", "SIM", null)
            )
        }

        return manager.activeSubscriptionInfoList.orEmpty().mapIndexed { index, info ->
            SimInfo(
                subscriptionId = info.subscriptionId,
                carrierName = info.carrierName?.toString().orEmpty().ifBlank { "Carrier" },
                displayName = info.displayName?.toString().ifNullOrBlank { "SIM ${index + 1}" },
                phoneNumber = info.number
            )
        }
    }
}

private fun String?.ifNullOrBlank(default: () -> String): String =
    if (this.isNullOrBlank()) default() else this
