package com.callflow.dialer.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.callflow.dialer.R
import com.callflow.dialer.databinding.ActivityMainBinding
import com.callflow.dialer.ui.dialpad.DialPadFragment
import com.callflow.dialer.ui.history.CallHistoryFragment
import com.callflow.dialer.ui.settings.SettingsFragment
import com.callflow.dialer.util.PermissionManager
import com.callflow.dialer.util.RoleManagerHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var roleHelper: RoleManagerHelper

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)
        roleHelper = RoleManagerHelper(this)

        binding.bottomNav.setOnItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.menu_history -> CallHistoryFragment()
                R.id.menu_settings -> SettingsFragment()
                else -> DialPadFragment()
            }
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
            true
        }

        if (savedInstanceState == null) binding.bottomNav.selectedItemId = R.id.menu_dial
        ensureDialerRole()
        requestCorePermissions()
    }

    override fun onResume() {
        super.onResume()
        if (!roleHelper.isDialerRoleHeld()) ensureDialerRole()
    }

    private fun ensureDialerRole() {
        if (!roleHelper.isDialerRoleHeld()) {
            roleHelper.createRoleIntent()?.let { startActivity(it) } ?: AlertDialog.Builder(this)
                .setMessage("CallFlow needs dialer role to place and receive calls.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun requestCorePermissions() {
        permissionManager.request(
            permissionLauncher,
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CONTACTS
            )
        )
    }
}
