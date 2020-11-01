package learning.android.stackedviews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.view1).alpha = 0.3f
        findViewById<TextView>(R.id.view2).alpha = 0.6f
        findViewById<TextView>(R.id.view3).alpha = 0.9f
    }
}