package com.callflow.dialer.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.callflow.dialer.databinding.FragmentSettingsBinding
import com.callflow.dialer.util.ThemeManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val themeManager = ThemeManager(requireContext())
        binding.systemTheme.setOnClickListener { lifecycleScope.launch { themeManager.setTheme(ThemeManager.SYSTEM) } }
        binding.lightTheme.setOnClickListener { lifecycleScope.launch { themeManager.setTheme(ThemeManager.LIGHT) } }
        binding.darkTheme.setOnClickListener { lifecycleScope.launch { themeManager.setTheme(ThemeManager.DARK) } }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
