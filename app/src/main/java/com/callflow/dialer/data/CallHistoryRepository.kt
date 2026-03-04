package com.callflow.dialer.data

import android.content.ContentResolver
import android.provider.CallLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallHistoryRepository(private val resolver: ContentResolver) {
    suspend fun loadHistory(query: String?, limit: Int, offset: Int): List<CallHistoryItem> =
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
                CallLog.Calls.PHONE_ACCOUNT_LABEL
            )
            val selection = if (query.isNullOrBlank()) null else "${CallLog.Calls.NUMBER} LIKE ? OR ${CallLog.Calls.CACHED_NAME} LIKE ?"
            val args = if (query.isNullOrBlank()) null else arrayOf("%$query%", "%$query%")
            val sort = "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"

            resolver.query(CallLog.Calls.CONTENT_URI, projection, selection, args, sort)?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            CallHistoryItem(
                                id = cursor.getLong(0),
                                name = cursor.getString(1),
                                number = cursor.getString(2) ?: "Unknown",
                                duration = cursor.getLong(3),
                                date = cursor.getLong(4),
                                type = cursor.getInt(5),
                                simLabel = cursor.getString(6)
                            )
                        )
                    }
                }
            }.orEmpty()
        }

    suspend fun deleteOne(id: Long) = withContext(Dispatchers.IO) {
        resolver.delete(CallLog.Calls.CONTENT_URI, "${CallLog.Calls._ID}=?", arrayOf(id.toString()))
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        resolver.delete(CallLog.Calls.CONTENT_URI, null, null)
    }
}
