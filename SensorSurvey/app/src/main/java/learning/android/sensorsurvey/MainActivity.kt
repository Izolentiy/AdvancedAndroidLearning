package learning.android.sensorsurvey

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private var isDetailDisplayed: Boolean = false

    private val listFragment = ListFragment.newInstance()
    private val detailFragment = DetailFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add the list fragment to activity
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, listFragment)
            .addToBackStack(null).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_screen_action -> {
                if (!isDetailDisplayed) {
                    changeFragment(detailFragment)
                    isDetailDisplayed = true
                    item.setTitle(R.string.sensors_list)
                } else {
                    changeFragment(listFragment)
                    isDetailDisplayed = false
                    item.setTitle(R.string.light_and_proximity)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeFragment(fragment: Fragment) {
        // Replace current fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }
}