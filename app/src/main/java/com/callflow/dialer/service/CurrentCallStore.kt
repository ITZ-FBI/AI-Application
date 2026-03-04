package com.callflow.dialer.service

import android.telecom.Call
import java.util.concurrent.CopyOnWriteArrayList

object CurrentCallStore {
    private val calls = CopyOnWriteArrayList<Call>()

    fun setCalls(newCalls: List<Call>) {
        calls.clear()
        calls.addAll(newCalls)
    }

    fun answer() { calls.firstOrNull { it.state == Call.STATE_RINGING }?.answer(0) }
    fun reject() { calls.firstOrNull { it.state == Call.STATE_RINGING }?.reject(false, null) }
    fun disconnect() { calls.firstOrNull { it.state == Call.STATE_ACTIVE }?.disconnect() }
}
