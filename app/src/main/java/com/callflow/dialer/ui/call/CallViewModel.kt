package com.callflow.dialer.ui.call

import androidx.lifecycle.ViewModel
import com.callflow.dialer.service.CurrentCallStore

class CallViewModel : ViewModel() {
    fun answer() = CurrentCallStore.answer()
    fun reject() = CurrentCallStore.reject()
    fun end() = CurrentCallStore.disconnect()
}
