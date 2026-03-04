package com.callflow.dialer

import android.app.Application
import com.callflow.dialer.util.ThemeManager

class CallFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager(this).applyTheme()
    }
}
