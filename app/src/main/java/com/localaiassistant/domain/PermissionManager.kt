package com.localaiassistant.domain

import com.localaiassistant.data.PermissionLogDao
import com.localaiassistant.data.PermissionLogEntity

class PermissionManager(private val permissionLogDao: PermissionLogDao) {
    private val activePermissions = mutableSetOf<String>()
    var safeMode: Boolean = true
        private set

    suspend fun requestPermission(request: PermissionRequest, decision: PermissionDecision) {
        if (decision == PermissionDecision.GRANTED) {
            activePermissions.add(request.resource)
        }
        permissionLogDao.insert(
            PermissionLogEntity(
                resource = request.resource,
                rationale = request.rationale,
                decision = decision.name,
                timestamp = request.requestedAt
            )
        )
    }

    fun revokePermission(resource: String) {
        activePermissions.remove(resource)
    }

    fun enableSafeMode(enabled: Boolean) {
        safeMode = enabled
        if (enabled) activePermissions.clear()
    }

    fun canAccess(resource: String): Boolean = !safeMode && activePermissions.contains(resource)
}
