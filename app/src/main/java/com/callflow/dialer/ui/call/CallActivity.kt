package com.callflow.dialer.ui.call

import android.Manifest
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.callflow.dialer.data.RecordingRepository
import com.callflow.dialer.databinding.ActivityCallBinding

class CallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private val viewModel by viewModels<CallViewModel>()
    private lateinit var bubbleManager: FloatingBubbleManager
    private lateinit var recordingRepository: RecordingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        dismissKeyguard()

        bubbleManager = FloatingBubbleManager(this)
        recordingRepository = RecordingRepository(this)

        binding.answer.setOnClickListener { viewModel.answer() }
        binding.reject.setOnClickListener { viewModel.reject() }
        binding.hangup.setOnClickListener { viewModel.end(); finish() }
        binding.minimize.setOnClickListener {
            bubbleManager.show { finish() }
            moveTaskToBack(true)
        }
        binding.record.setOnCheckedChangeListener { _, checked ->
            if (checked) startRecording() else stopRecording()
        }
    }

    private fun dismissKeyguard() {
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguard.requestDismissKeyguard(this, null)
        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            binding.record.isChecked = false
            return
        }
        recordingRepository.startMicrophoneRecording()
        binding.recordIndicator.text = "Recording"
    }

    private fun stopRecording() {
        recordingRepository.stopRecording()
        binding.recordIndicator.text = "Not recording"
    }

    override fun onDestroy() {
        stopRecording()
        bubbleManager.dismiss()
        super.onDestroy()
    }
}
