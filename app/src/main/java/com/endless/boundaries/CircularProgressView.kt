package com.endless.boundaries

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        color = ContextCompat.getColor(context, R.color.progress_bg)
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        color = ContextCompat.getColor(context, R.color.progress_yellow)
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = ContextCompat.getColor(context, R.color.white)
    }

    private val oval = RectF()
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 40f
        val size = minOf(width, height) - padding * 2

        oval.set(
            (width - size) / 2,
            (height - size) / 2,
            (width + size) / 2,
            (height + size) / 2
        )

        // Draw background arc (3/4 circle = 270 degrees)
        canvas.drawArc(oval, 135f, 270f, false, backgroundPaint)

        // Draw progress arc
        val sweepAngle = (progress / 100f) * 270f
        canvas.drawArc(oval, 135f, sweepAngle, false, progressPaint)

        // Draw white indicator circle at the end of progress
        if (progress > 0) {
            val angle = Math.toRadians((135f + sweepAngle).toDouble())
            val radius = size / 2
            val centerX = width / 2
            val centerY = height / 2
            val indicatorX = centerX + (radius * Math.cos(angle)).toFloat()
            val indicatorY = centerY + (radius * Math.sin(angle)).toFloat()
            canvas.drawCircle(indicatorX, indicatorY, 8f, indicatorPaint)
        }
    }
}
