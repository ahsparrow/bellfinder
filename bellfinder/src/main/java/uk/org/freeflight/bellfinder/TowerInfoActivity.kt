/*
Bell Finder - A directory of English style bell towers

Copyright (C) 2020  Alan Sparrow

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package uk.org.freeflight.bellfinder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TowerInfoActivity : AppCompatActivity() {
    private val viewModel: ViewModel by viewModels()

    private var towerId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tower_info)

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar_towerinfo))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        towerId = intent.extras!!.get("TOWER_ID") as Long

        viewModel.liveTowerVisits(towerId).observe(this) { visits ->
            if (visits.isNotEmpty()) {
                val txt = getString(R.string.date_format_long).format(visits[0].date)
                findViewById<TextView>(R.id.textview_towerinfo_visit).text = txt
            }
        }

        lifecycleScope.launch {
            val tower = withContext(Dispatchers.IO) {
                viewModel.getTower(towerId)
            }

            // Tower place
            var place = tower.place
            if (tower.unringable) {
                place = "$place (Unringable)"
            }
            findViewById<TextView>(R.id.textview_towerinfo_place).text = place

            // Tower place details
            val place2 = tower.dedication
            findViewById<TextView>(R.id.textview_towerinfo_place2).text = place2

            // County
            findViewById<TextView>(R.id.textview_towerinfo_county).text = tower.county

            // Number of bells
            findViewById<TextView>(R.id.textview_towerinfo_bells).text = tower.bells.toString()

            // Tenor weight
            val weight = tower.weight
            val cwt = weight / 112
            val qtr = (weight - cwt * 112) / 28
            val lbs = weight % 28
            val tenor = "$cwt-$qtr-$lbs"
            findViewById<TextView>(R.id.textview_towerinfo_tenor).text = tenor

            // Practice night
            findViewById<TextView>(R.id.textview_towerinfo_practice).text = tower.practice

            // Goto Dove web page
            findViewById<TextView>(R.id.textview_towerinfo_dovelink).setOnClickListener {
                val url = "https://dove.cccbr.org.uk/detail.php?TowerBase=${tower.towerId}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                
                // Re-use existing browswer tab
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
                startActivity(intent)
            }

            // Start a new visit
            findViewById<FloatingActionButton>(R.id.button_add_visit).setOnClickListener {
                startNewVisit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tower_info_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_map -> {
                showMap()
                true
            }

            R.id.action_add_visit -> {
                startNewVisit()
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showMap() {
        lifecycleScope.launch {
            val tower = withContext(Dispatchers.IO) {
                viewModel.getTower(towerId)
            }

            // Display labelled marker at tower position
            val uriStr = "geo:${tower.latitude},${tower.longitude}" +
                    "?z=8&" +
                    "q=${tower.latitude},${tower.longitude}(${Uri.encode(tower.place)})"

            val gmmIntentUri = Uri.parse(uriStr)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            }
        }
    }

    private fun startNewVisit() {
        val intent = Intent(this, VisitEditActivity::class.java)
        intent.putExtra("TOWER_ID", towerId)
        startActivity(intent)
    }
}
