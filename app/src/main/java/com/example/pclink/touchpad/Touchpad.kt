package com.example.pclink.touchpad

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.ImageView
import com.example.pclink.NetworkLink
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class Touchpad(val imageView: ImageView, val net: NetworkLink) {

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f
    private var lastAvgX = 0f
    private var lastAvgY = 0f
    private var threshold = 2f
    private var thresholdClick = 10f
    private var lastScrollTime = 0L
    private val scrollIntervalMs = 50L

    private var scrollVelocityX = 0f
    private var scrollVelocityY = 0f
    private var scrollTimer: Timer? = null
    private var wasTwoFingerScroll = false

    @SuppressLint("ClickableViewAccessibility")
    fun touchPad() {
        imageView.setOnTouchListener { _, event ->
            val pointerCount = event.pointerCount
            val currentTime = System.currentTimeMillis()

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (pointerCount == 1) {
                        lastX = event.getX(0)
                        lastY = event.getY(0)
                        downX = lastX
                        downY = lastY

                        if (wasTwoFingerScroll) {
                            // сброс после скролла — чтобы не было скачка мыши
                            wasTwoFingerScroll = false
                            return@setOnTouchListener true
                        }

                        stopScrollInertia()
                    } else if (pointerCount == 2) {
                        val x0 = event.getX(0)
                        val x1 = event.getX(1)
                        val y0 = event.getY(0)
                        val y1 = event.getY(1)
                        lastAvgX = (x0 + x1) / 2
                        lastAvgY = (y0 + y1) / 2

                        stopScrollInertia()
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (pointerCount == 1 && !wasTwoFingerScroll) {
                        val dx = event.getX(0) - lastX
                        val dy = event.getY(0) - lastY

                        if (abs(dx) > threshold || abs(dy) > threshold) {
                            net.sendCommand(2, "MOVE:${dx.toInt()},${dy.toInt()}")
                            lastX = event.getX(0)
                            lastY = event.getY(0)
                        }
                    } else if (pointerCount == 2) {
                        wasTwoFingerScroll = true

                        val x0 = event.getX(0)
                        val x1 = event.getX(1)
                        val y0 = event.getY(0)
                        val y1 = event.getY(1)

                        val avgX = (x0 + x1) / 2
                        val avgY = (y0 + y1) / 2
                        val deltaX = avgX - lastAvgX
                        val deltaY = avgY - lastAvgY

                        if (currentTime - lastScrollTime > scrollIntervalMs) {
                            if (abs(deltaX) > threshold) {
                                net.sendCommand(4, "HSCROLL:${(-deltaX).toInt()}")
                            }
                            if (abs(deltaY) > threshold) {
                                net.sendCommand(3, "SCROLL:${(-deltaY).toInt()}")
                            }
                            // Запоминаем дельты для инерции
                            scrollVelocityX = deltaX
                            scrollVelocityY = deltaY

                            lastAvgX = avgX
                            lastAvgY = avgY
                            lastScrollTime = currentTime
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    if (wasTwoFingerScroll && pointerCount <= 2) {
                        startScrollInertia()
                    }

                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (!wasTwoFingerScroll && abs(dx) < thresholdClick && abs(dy) < thresholdClick) {
                        net.sendCommand(5, "CLICK")
                    }
                }
            }
            true
        }
    }
    private fun startScrollInertia() {
        scrollTimer?.cancel()
        scrollTimer = Timer()
        scrollTimer?.schedule(object : TimerTask() {
            override fun run() {
                scrollVelocityX *= 0.9f
                scrollVelocityY *= 0.9f

                if (abs(scrollVelocityX) < 1f && abs(scrollVelocityY) < 1f) {
                    stopScrollInertia()
                    return
                }

                if (abs(scrollVelocityX) >= 1f) {
                    net.sendCommand(4, "HSCROLL:${(-scrollVelocityX).toInt()}")
                }
                if (abs(scrollVelocityY) >= 1f) {
                    net.sendCommand(3, "SCROLL:${(-scrollVelocityY).toInt()}")
                }
            }
        }, 0, scrollIntervalMs)
    }

    private fun stopScrollInertia() {
        scrollTimer?.cancel()
        scrollTimer = null
    }
}