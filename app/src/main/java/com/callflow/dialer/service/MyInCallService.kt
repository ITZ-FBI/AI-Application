package com.callflow.dialer.service

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.callflow.dialer.data.CallRepository

class MyInCallService : InCallService() {
    private val callRepository = CallRepository()
    private val callbackMap = mutableMapOf<String, Call.Callback>()

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callRepository.addOrUpdateCall(call)
        val callback = object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                callRepository.addOrUpdateCall(call)
                        CurrentCallStore.setCalls(callRepository.activeCalls.value.map { it.telecomCall })
                CallForegroundService.sync(this@MyInCallService, callRepository.activeCalls.value)
            }

            override fun onDetailsChanged(call: Call, details: Call.Details) {
                callRepository.addOrUpdateCall(call)
            }
        }
        callbackMap[call.hashCode().toString()] = callback
        call.registerCallback(callback)
        CurrentCallStore.setCalls(callRepository.activeCalls.value.map { it.telecomCall })
        CallForegroundService.sync(this, callRepository.activeCalls.value)

        startActivity(
            Intent(this, com.callflow.dialer.ui.call.CallActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun onCallRemoved(call: Call) {
        callbackMap.remove(call.hashCode().toString())?.let(call::unregisterCallback)
        callRepository.removeCall(call)
        CurrentCallStore.setCalls(callRepository.activeCalls.value.map { it.telecomCall })
        CallForegroundService.sync(this, callRepository.activeCalls.value)
        super.onCallRemoved(call)
    }
}
