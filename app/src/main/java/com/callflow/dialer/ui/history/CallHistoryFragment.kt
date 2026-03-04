package com.callflow.dialer.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.callflow.dialer.databinding.FragmentCallHistoryBinding
import kotlinx.coroutines.launch

class CallHistoryFragment : Fragment() {
    private var _binding: FragmentCallHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(this, Factory(requireActivity().contentResolver))[CallHistoryViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCallHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = CallHistoryAdapter { viewModel.deleteOne(it.id) }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.clearAll.setOnClickListener { viewModel.clearAll() }
        binding.searchInput.doAfterTextChangedCompat { viewModel.load(it) }

        lifecycleScope.launch {
            viewModel.items.collect(adapter::submitList)
        }
        viewModel.load()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    class Factory(private val resolver: android.content.ContentResolver) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CallHistoryViewModel(resolver) as T
    }
}

private fun com.google.android.material.textfield.TextInputEditText.doAfterTextChangedCompat(cb: (String?) -> Unit) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: android.text.Editable?) = cb(s?.toString())
    })
}
