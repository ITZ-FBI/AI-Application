package com.localaiassistant.domain

enum class PermissionDecision { GRANTED, DENIED }

data class PermissionRequest(
    val resource: String,
    val rationale: String,
    val requestedAt: Long = System.currentTimeMillis()
)
