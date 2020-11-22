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

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.freeflight.bellfinder.db.TowerBaseIds
import uk.org.freeflight.bellfinder.db.Visit
import java.io.InputStream
import java.io.OutputStream
import java.util.*

const val TAG = "BellFinder"
const val LOCATION_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private var optionMenu: Menu? = null

    private lateinit var getImport: ActivityResultLauncher<String>
    private lateinit var getExport: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load data from Dove file
        val sharedPref = this.getPreferences(MODE_PRIVATE)
        val initialisedVersionCode = sharedPref.getInt(getString(R.string.initialised_version_code), 0)
        if (BuildConfig.VERSION_CODE != initialisedVersionCode) {
            with (sharedPref.edit()) {
                putInt(getString(R.string.initialised_version_code), BuildConfig.VERSION_CODE)
                apply()
            }

            // Delete old database
            val viewModel: ViewModel by viewModels()
            viewModel.deleteTowers()

            // Get new database
            viewModel.parseDove(assets.open("dove.txt"))
        }

        // Set up view pager for towers/nearby/visits
        viewPager = findViewById(R.id.pager)
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        // Setup search menu on page change
        viewPager.registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback () {
            override fun onPageSelected(position: Int) {
                // Reset fragment search
                getPagerFragment(position)?.search("")

                // Set up search menu/widget
                val searchMenuItem: MenuItem? = optionMenu?.findItem(R.id.search_menuitem)
                searchMenuItem?.actionView?.clearFocus()
                searchMenuItem?.isVisible = ViewPagerAdapter.searchable[position]
                searchMenuItem?.collapseActionView()

                // Enable/disable swipes
                viewPager.isUserInputEnabled = ViewPagerAdapter.swipeable[position]
            }
        })

        // Add tab layout to view pager
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = ViewPagerAdapter.tabNames[position]
        }.attach()

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar_main))

        // Visit import/export handlers
        getImport = registerForActivityResult(ActivityResultContracts.GetContent()) {
            importHandler(it)
        }

        getExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            exportHandler(it)
        }

        // Ask for location permissions
        requestLocationPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search_menuitem)
        searchMenuItem?.let {
            (it.actionView as SearchView).apply {
                setSearchableInfo(searchManager.getSearchableInfo(componentName))
            }.apply {
                // Reset tower list when search widget is closed
                setOnCloseListener {
                    setFilter("")
                    false
                }
            }.apply {
                // Listen for updated search data
                setOnQueryTextListener(QueryTextListener())
            }
        }

        optionMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.export_menuitem -> {
                getExport.launch("visits.csv")
                true
            }

            R.id.import_menuitem -> {
                // text/csv would be better, but it doesn't seem to work
                getImport.launch("*/*")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Select a view page
    fun setViewPage(name: String, smoothScroll: Boolean) {
        val idx = ViewPagerAdapter.tabNames.indexOf(name)
        viewPager.setCurrentItem(idx, smoothScroll)
    }

    // Set filter in fragment list adapter
    private fun setFilter(pattern: String?) {
        val frag: SearchableFragment? = getPagerFragment(viewPager.currentItem)

        pattern?.let {
            frag?.search(it)
        }
    }

    private fun getPagerFragment(position: Int) : SearchableFragment? {
        // ViewPager2 fragments are tagged as "f" + position
        return  supportFragmentManager.findFragmentByTag("f$position") as SearchableFragment?
    }

    // Search test listener
    inner class QueryTextListener: SearchView.OnQueryTextListener {
        override fun onQueryTextChange(query: String?): Boolean {
            setFilter(query)
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }
    }

    // Start the nearby towers activity
    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check/request location permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE)
            }
        }
    }

    // Request permission callback
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (!((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                    AlertDialog.Builder(this)
                    .setMessage("Unable to find nearby towers without location permission")
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .create()
                        .show()
                }
            }
        }
    }

    // Export the visits database
    private fun exportHandler(uri: Uri?) {
        // Uri is null if request is aborted by user
        if (uri == null)
            return

        contentResolver.openOutputStream(uri)?.let<OutputStream, Unit> { output ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val viewModel: ViewModel by viewModels()
                    val visits = viewModel.getVisitViews()

                    // CSV header (place is for info only, not for backup)
                    val header = listOf(listOf(
                        "VisitId", "TowerBase", "Date", "Notes", "Peal", "Quarter", "Place"))

                    val rows = visits.map { visit ->
                        val date = getString(R.string.date_format_iso).format(visit.date)
                        val place =
                            visit.place + ", " + if (visit.place2 != "") visit.place2 else visit.dedication

                        listOf(
                            visit.visitId.toString(),
                            visit.towerId.toString(),
                            date,
                            visit.notes,
                            if (visit.peal) "Y" else "",
                            if (visit.quarter) "Y" else "",
                            place
                        )
                    }
                    csvWriter().writeAll(header + rows, output)
                }
            }
        }
    }

    // Import to the visits database
    private fun importHandler(uri: Uri?) {
        // Uri is null if import request is aborted by user
        if (uri == null)
            return

        contentResolver.openInputStream(uri)?.let<InputStream, Unit> { input ->
            try {
                val viewModel: ViewModel by viewModels()

                // Read data from CSV file
                val data = csvReader().readAllWithHeader(input)

                if (data[0].containsKey("VisitId")) {
                    // If import data contains the VisitId field then it is previously exported
                    // data, and so will overwrite existing visits in the database

                    // Convert into list of visits
                    val visits = data.map { visit ->
                        // Parse ISO date
                        val s = visit.getValue("Date").split("-")
                        val date = GregorianCalendar(s[0].toInt(), s[1].toInt() - 1, s[2].toInt())

                        val towerId = if (data[0].containsKey("TowerId")) {
                            // Convert old tower id to TowerBase
                            TowerBaseIds[visit.getValue("TowerId").toInt()].toLong()
                        } else {
                            visit.getValue("TowerBase").toLong()
                        }

                        Visit(
                            visit.getValue("VisitId").toLong(),
                            towerId,
                            date,
                            visit.getValue("Notes"),
                            visit.getValue("Peal") == "Y",
                            visit.getValue("Quarter") == "Y"
                        )
                    }

                    // Write to database
                    viewModel.insertVisits(visits)

                } else {
                    // The import data doesn't have a VisitId field so it's "foreign" data and
                    // will be appended to existing visits

                    for (d in data) {
                        val s = d.getValue("Date").split("-")
                        val date = GregorianCalendar(s[0].toInt(), s[1].toInt() - 1, s[2].toInt())

                        viewModel.insertVisit(
                            d.getValue("TowerBase").toLong(),
                            date,
                            d.getValue("Notes"),
                            d.getValue("Peal") == "Y",
                            d.getValue("Quarter") == "Y"
                        )
                    }
                }

            } catch (e: RuntimeException) {
                Toast.makeText(
                    this,
                    "Error reading backup file\n\n" + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
