package com.localaiassistant.integration

import com.localaiassistant.domain.PermissionManager

class ActionGate(private val permissionManager: PermissionManager) {
    fun performAction(resource: String, action: () -> Unit): Result<Unit> {
        if (!permissionManager.canAccess(resource)) {
            return Result.failure(IllegalStateException("Action blocked: explicit permission required for $resource"))
        }
        action()
        return Result.success(Unit)
    }
}
