package org.android.learning.minipaint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

private const val STROKE_WIDTH = 12f // Has to be float

class MyCanvasView(context: Context) : View(context) {
    private val backgroundColor = ResourcesCompat
        .getColor(resources, R.color.orange_material_a_700, null)
    private val drawColor = ResourcesCompat
        .getColor(resources, R.color.yellow_material_600, null)
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: hairline width (really thin)
    }

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private var path = Path()
    private var extraCanvas: Canvas? = null
    private var extraBitmap: Bitmap? = null

    // Path representing the drawing so far
    private val drawing = Path()

    // Path representing what's currently being drawn
    private val curPath = Path()

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1, y1), and ending at (x2, y2).
            path.quadTo(
                currentX, currentY,
                (motionTouchEventX + currentX) / 2,
                (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in extra bitmap to cache it.
            extraCanvas?.drawPath(path, paint)
        }
        invalidate()
    }

    private fun touchEnd() {
        // Add the current path to the drawing so far
        drawing.addPath(curPath)
        // Rewind the current path for the next touch
        curPath.reset()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        extraBitmap?.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap!!).apply { drawColor(backgroundColor) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            // Draw the drawing so far
            drawPath(drawing, paint)
            drawPath(curPath, paint)
            drawBitmap(checkNotNull(extraBitmap), 0f, 0f, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchEnd()
        }
        return true
    }

}