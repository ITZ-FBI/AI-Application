package com.callflow.dialer.ui.history

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callflow.dialer.data.CallHistoryItem
import com.callflow.dialer.data.CallHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallHistoryViewModel(private val resolver: ContentResolver) : ViewModel() {
    private val repository = CallHistoryRepository(resolver)

    private val _items = MutableStateFlow<List<CallHistoryItem>>(emptyList())
    val items: StateFlow<List<CallHistoryItem>> = _items.asStateFlow()

    fun load(query: String? = null, page: Int = 0, pageSize: Int = 50) {
        viewModelScope.launch {
            _items.value = repository.loadHistory(query, pageSize, page * pageSize)
        }
    }

    fun deleteOne(id: Long) {
        viewModelScope.launch {
            repository.deleteOne(id)
            load()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _items.value = emptyList()
        }
    }
}
