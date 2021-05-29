package org.android.learning.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 40
private const val RADIUS_OFFSET_INDICATOR = -50

class FanControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0.0f           // Radius of the circle
    private var fanSpeed = FanSpeed.OFF // The active selection

    // Position variable which will be used to draw label and indicator circle position
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    // Paint is used to draw view
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private var lowSpeedColor = 0
    private var mediumSpeedColor = 0
    private var highSpeedColor = 0

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.FanControllerView) {
            lowSpeedColor = getColor(R.styleable.FanControllerView_fan_color_low, 0)
            mediumSpeedColor = getColor(R.styleable.FanControllerView_fan_color_medium, 0)
            highSpeedColor = getColor(R.styleable.FanControllerView_fan_color_high, 0)
        }
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Set dial background color to green if selection is not off
        paint.color = when(fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> lowSpeedColor
            FanSpeed.MEDIUM -> mediumSpeedColor
            FanSpeed.HIGH -> highSpeedColor
        }

        // Draw the dial
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // Draw the indicator circle
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        // Draw the text labels
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (fanSpeed in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(fanSpeed, labelRadius)
            val label = resources.getString(fanSpeed.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }
}