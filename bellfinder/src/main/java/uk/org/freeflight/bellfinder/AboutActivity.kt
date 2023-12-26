package uk.org.freeflight.bellfinder

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AboutActivity : AppCompatActivity() {
    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar_about))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<TextView>(R.id.textview_app_version).text = BuildConfig.VERSION_NAME
        findViewById<TextView>(R.id.textview_dove_info).text = getString(R.string.dove_release_date, BuildConfig.DOVE_DATE)

        lifecycle.coroutineScope.launch {
            viewModel.getTotalTowers().first {total ->
                findViewById<TextView>(R.id.textview_total_dove_towers).text = getString(R.string.total_towers, total)
                true
            }
        }

        lifecycle.coroutineScope.launch {
            viewModel.getNumTowersVisited().first {visits ->
                findViewById<TextView>(R.id.textview_num_towers_visited).text = getString(R.string.towers_visited, visits)
                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}