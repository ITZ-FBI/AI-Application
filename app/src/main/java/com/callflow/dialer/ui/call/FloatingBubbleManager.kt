package com.callflow.dialer.ui.call

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.callflow.dialer.databinding.ViewCallBubbleBinding
import kotlin.math.abs

class FloatingBubbleManager(private val context: Context) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var binding: ViewCallBubbleBinding? = null
    private var params: WindowManager.LayoutParams? = null

    fun show(onExpand: () -> Unit) {
        if (!Settings.canDrawOverlays(context) || binding != null) return

        binding = ViewCallBubbleBinding.inflate(LayoutInflater.from(context))
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = 200
        }

        val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onExpand()
                return true
            }
        })

        binding?.root?.setOnTouchListener(DragTouchListener(detector))
        windowManager.addView(binding?.root, params)
    }

    fun dismiss() {
        binding?.root?.let { runCatching { windowManager.removeView(it) } }
        binding = null
        params = null
    }

    private inner class DragTouchListener(
        private val detector: GestureDetector
    ) : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            detector.onTouchEvent(event)
            val p = params ?: return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = p.x
                    initialY = p.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    p.x = initialX + (event.rawX - initialTouchX).toInt()
                    p.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(binding?.root, p)
                }
                MotionEvent.ACTION_UP -> {
                    val displayWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        context.display?.width ?: 1080
                    } else 1080
                    p.x = if (p.x + (binding?.root?.width ?: 0) / 2 < displayWidth / 2) 0 else displayWidth
                    if (abs(event.rawX - initialTouchX) > 8 || abs(event.rawY - initialTouchY) > 8) {
                        windowManager.updateViewLayout(binding?.root, p)
                    }
                }
            }
            return true
        }
    }
}
