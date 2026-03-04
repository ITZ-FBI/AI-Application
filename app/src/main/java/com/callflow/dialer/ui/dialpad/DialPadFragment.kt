package com.callflow.dialer.ui.dialpad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.callflow.dialer.data.SubscriptionRepository
import com.callflow.dialer.databinding.FragmentDialpadBinding
import com.callflow.dialer.util.TelecomUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class DialPadFragment : Fragment() {
    private var _binding: FragmentDialpadBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<DialPadViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDialpadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val digits = listOf(
            binding.key0, binding.key1, binding.key2, binding.key3, binding.key4,
            binding.key5, binding.key6, binding.key7, binding.key8, binding.key9
        )
        digits.forEachIndexed { index, textView -> textView.setOnClickListener { viewModel.append(index.toString()) } }
        binding.keyStar.setOnClickListener { viewModel.append("*") }
        binding.keyHash.setOnClickListener { viewModel.append("#") }
        binding.backspace.setOnClickListener { viewModel.backspace() }
        binding.callButton.setOnClickListener { placeCall() }

        lifecycleScope.launch {
            viewModel.number.collect { binding.numberField.text = it }
        }
    }

    private fun placeCall() {
        val number = binding.numberField.text.toString().trim()
        if (number.isBlank()) return
        if (TelecomUtils.isEmergencyNumber(number)) {
            TelecomUtils.routeEmergencyToSystemDialer(requireContext(), number)
            return
        }

        val sims = SubscriptionRepository(requireContext()).getActiveSims()
        when {
            sims.size <= 1 -> TelecomUtils.placeCall(requireContext(), number, sims.firstOrNull()?.subscriptionId)
            else -> showSimSheet(number, sims.map { it.subscriptionId to it.displayName })
        }
    }

    private fun showSimSheet(number: String, sims: List<Pair<Int, String>>) {
        BottomSheetDialog(requireContext()).apply {
            val labels = sims.joinToString("\n") { "• ${it.second}" }
            setContentView(android.widget.TextView(context).apply {
                setPadding(40, 40, 40, 40)
                text = "Select SIM:\n$labels"
                setOnClickListener {
                    TelecomUtils.placeCall(requireContext(), number, sims.first().first)
                    dismiss()
                }
            })
            show()
        }
        Toast.makeText(requireContext(), "Tap selector to place call", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
