package com.callflow.dialer.ui.dialpad

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DialPadViewModel : ViewModel() {
    private val _number = MutableStateFlow("")
    val number: StateFlow<String> = _number.asStateFlow()

    fun append(digit: String) { _number.value += digit }
    fun backspace() { _number.value = _number.value.dropLast(1) }
    fun clear() { _number.value = "" }
}
