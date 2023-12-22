package com.example.accessibilityverifier.axemodels

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class DraggableButton(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isPressed = false

    init {
        paint.color = Color.GRAY
        setOnClickListener {
            // Handle the button click event here
            Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the button background
        if (isPressed) {
            paint.color = Color.DKGRAY
        } else {
            paint.color = Color.GRAY
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 20f, 20f, paint)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                invalidate()
                performClick() // Trigger the click event
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
