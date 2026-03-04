package com.callflow.dialer.data

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class CallRepository {
    private val callMap = ConcurrentHashMap<String, ActiveCallState>()
    private val _activeCalls = MutableStateFlow<List<ActiveCallState>>(emptyList())
    val activeCalls: StateFlow<List<ActiveCallState>> = _activeCalls.asStateFlow()

    fun addOrUpdateCall(call: Call) {
        val details = call.details
        val state = ActiveCallState(
            telecomCall = call,
            state = call.state,
            canHold = details.can(Call.Details.CAPABILITY_HOLD),
            canMerge = details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE),
            canSwap = details.can(Call.Details.CAPABILITY_SWAP_CONFERENCE)
        )
        callMap[call.hashCode().toString()] = state
        publish()
    }

    fun removeCall(call: Call) {
        callMap.remove(call.hashCode().toString())
        publish()
    }

    private fun publish() {
        _activeCalls.value = callMap.values.toList()
    }

    fun clear() {
        callMap.clear()
        publish()
    }
}

private fun Call.Details.can(capability: Int): Boolean =
    capabilities and capability == capability
