package org.android.learning.minipaint

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myCanvasView = MyCanvasView(this).apply {
            contentDescription = getString(R.string.canvas_content_description)
        }
        setContentView(myCanvasView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        else
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}