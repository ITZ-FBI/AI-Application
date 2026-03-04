package com.callflow.dialer.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build

class RoleManagerHelper(private val context: Context) {
    private val roleManager = context.getSystemService(RoleManager::class.java)

    fun isDialerRoleHeld(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
    }

    fun createRoleIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || roleManager == null) return null
        return roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
    }

    fun requestDialerRole(activity: Activity, requestCode: Int) {
        createRoleIntent()?.let { activity.startActivityForResult(it, requestCode) }
    }
}
