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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.OnItemActivatedListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearbyListFragment : ListFragment(), LocationListener {
    companion object {
        const val MAX_INITIAL_AGE = 30000L
        const val MAX_GPS_AGE = 30000L
    }

    private var locationManager: LocationManager? = null
    private var lastGpsLocation: Location? = null

    private var snackbar: Snackbar? = null

    // Selected item listener
    inner class ItemListener: OnItemActivatedListener<Long> {
        override fun onItemActivated(item: ItemDetailsLookup.ItemDetails<Long>, e: MotionEvent) :
                Boolean {

            val selectionKey = item.selectionKey
            return if (selectionKey != null) {
                // Start new activity to display selected tower
                val intent = Intent(activity, TowerInfoActivity::class.java)
                intent.putExtra("TOWER_ID", selectionKey)
                startActivity(intent)
                true
            }  else {
                false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nearbyAdapter = NearbyListAdapter()
        adapter = nearbyAdapter

        // Set tower information in list adapter
        val viewModel: ViewModel by activityViewModels()
        viewModel.liveTowers.observe(this, { towers ->
            towers?.let { nearbyAdapter.setTowers(it) }
        })

        // Set visited towers in list adapter
        viewModel.liveVisitedTowerIds.observe(this, { towerIds ->
            towerIds?.let { nearbyAdapter.setVisitedTowers(towerIds)}
        })

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Selection tracker
        selectionBuilder.withOnItemActivatedListener(ItemListener()).build()

        return view
    }

    override fun onResume() {
        super.onResume()

        var goodLocation = false

        // First try for fine location
        if (startLocation(LocationManager.GPS_PROVIDER)) {
            try {
                val lastLocation =
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    if (locationAge(lastLocation) < MAX_INITIAL_AGE) {
                        sort(lastLocation)
                        goodLocation = true
                    }
                }
            }
            catch (e: SecurityException) {
                Log.w(TAG, "Unexpected security exception getting find locoation")
            }
            catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unknown fine location provider")
            }
        }

        // We didn't get a fine location so give coarse location a go
        if (!goodLocation) {
            if (startLocation(LocationManager.NETWORK_PROVIDER)) {
                try {
                    val lastLocation =
                        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (lastLocation != null) {
                        if (locationAge(lastLocation) < MAX_INITIAL_AGE) {
                            goodLocation = true
                        }

                        // Initialise the list with location irrespective of age
                        sort(lastLocation)
                    }
                }
                catch (e: SecurityException) {
                    Log.w(TAG, "Unexpected security exception getting coarse location")
                }
                catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Unknown coarse location provider")
                }
            }
        }

        if (!goodLocation) {
            val view: View? = activity?.findViewById(android.R.id.content)
            view?.let {
                snackbar = Snackbar.make(it, "Waiting for location update",
                    Snackbar.LENGTH_INDEFINITE).apply {
                        setAction("DISMISS") {}
                        show()
                    }
            }
        }
    }

    override fun onPause() {
        locationManager?.removeUpdates(this)
        hideSnackBar()

        super.onPause()
    }

    override fun onLocationChanged(location: Location) {
        hideSnackBar()

        // Ignore network locations if we've receive a recent GPS location
        if (location.provider == LocationManager.NETWORK_PROVIDER) {
            lastGpsLocation?.let {loc ->
                if (locationAge(loc) < MAX_GPS_AGE)
                    return
            }
        } else {
            lastGpsLocation = location
        }

        // Update tower list
        sort(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    // Start location requests
    private fun startLocation(provider: String): Boolean {
        val permission = if (provider == LocationManager.GPS_PROVIDER) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            Manifest.permission.ACCESS_COARSE_LOCATION
        }

        val ctx = context
        return if ((ctx != null) && (ContextCompat.checkSelfPermission( ctx,  permission) == PackageManager.PERMISSION_GRANTED)) {
            locationManager?.requestLocationUpdates(provider, 30000, 100.0f, this)
            true
        } else {
            false
        }
    }

    // Get rid of the snackBar (if displayed)
    private fun hideSnackBar() {
        snackbar?.dismiss()
        snackbar = null
    }

    // Return age of location in milliseconds
    private fun locationAge(location: Location) : Long {
        return System.currentTimeMillis() - location.time
    }

    private fun sort(location: Location) {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                val nearbyAdapter = adapter as NearbyListAdapter
                nearbyAdapter.sort(location)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun search(pattern: String) {}
}