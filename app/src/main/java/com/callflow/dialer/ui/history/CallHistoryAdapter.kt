package com.callflow.dialer.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.callflow.dialer.data.CallHistoryItem
import com.callflow.dialer.databinding.ItemCallHistoryBinding
import java.text.DateFormat
import java.util.Date

class CallHistoryAdapter(
    private val onDelete: (CallHistoryItem) -> Unit
) : ListAdapter<CallHistoryItem, CallHistoryAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemCallHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemCallHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CallHistoryItem) {
            binding.name.text = item.name ?: "Unknown"
            binding.number.text = item.number
            binding.meta.text = "${DateFormat.getDateTimeInstance().format(Date(item.date))} • ${item.duration}s • ${item.simLabel ?: "SIM"}"
            binding.delete.setOnClickListener { onDelete(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<CallHistoryItem>() {
        override fun areItemsTheSame(oldItem: CallHistoryItem, newItem: CallHistoryItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CallHistoryItem, newItem: CallHistoryItem): Boolean = oldItem == newItem
    }
}
