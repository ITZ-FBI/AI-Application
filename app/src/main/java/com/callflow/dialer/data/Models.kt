package com.callflow.dialer.data

import android.telecom.Call

data class SimInfo(
    val subscriptionId: Int,
    val carrierName: String,
    val displayName: String,
    val phoneNumber: String?
)

data class CallHistoryItem(
    val id: Long,
    val name: String?,
    val number: String,
    val duration: Long,
    val date: Long,
    val type: Int,
    val simLabel: String?
)

data class ActiveCallState(
    val telecomCall: Call,
    val state: Int,
    val canHold: Boolean,
    val canMerge: Boolean,
    val canSwap: Boolean
)
