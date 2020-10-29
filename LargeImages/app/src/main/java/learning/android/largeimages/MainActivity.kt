package learning.android.largeimages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    private var toggle = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun changeImage(view: View) {
        if (toggle == 0) {
            view.setBackgroundResource(R.drawable.dinosaur_medium)
            toggle = 1
        } else if (toggle == 1){
            view.setBackgroundResource(R.drawable.dinosaur_large)
            toggle = 2
        } else {
            try {
                Thread.sleep(32)  // Long work simulation
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            view.setBackgroundResource(R.drawable.ankylo);
            toggle = 0
        }
    }
}