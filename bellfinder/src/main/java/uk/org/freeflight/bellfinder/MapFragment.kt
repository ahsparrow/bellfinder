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
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class MapFragment : SearchableFragment(), LocationListener {
    companion object {
        const val NUM_MARKERS = 100

        const val DEFAULT_LAT = 51.04F
        const val DEFAULT_LON = -1.58F
        const val DEFAULT_ZOOM = 10.0F

        const val MAX_INITIAL_AGE = 30000L
        const val LOCATION_UPDATE_INTERVAL = 5000L
        const val LOCATiON_UPDATE_DIST = 50.0F
    }

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var mapView: MapView
    private var markers = listOf<CustomMarker>()
    private lateinit var infoWindow: CustomInfoWindow
    private var nearbyDialog: AlertDialog? = null

    private lateinit var sharedPrefs: SharedPreferences

    private var locationManager: LocationManager? = null
    private var lastLocation: Location? = null
    private lateinit var locationMarker: Marker
    private var locationTracking: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            val ctx = it.applicationContext
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            Configuration.getInstance().load(ctx, sharedPrefs)
        }

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false) ?: return null

        mapView = view.findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // Necessary to work with view pager - see onDestroy()
        mapView.setDestroyMode(false)

        // Toggle location tracking
        view.findViewById<FloatingActionButton>(R.id.button_location).setOnClickListener {
            setTracking(!locationTracking)
        }

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure zoom, set max. level to reduce load on tile server
        mapView.isTilesScaledToDpi = true
        mapView.setMultiTouchControls(true)
        mapView.zoomController?.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapView.maxZoomLevel = 13.5

        // Copyright
        val copyrightOverlay = CopyrightOverlay(context)
        copyrightOverlay.setTextSize(11)
        mapView.overlays.add(copyrightOverlay)

        // Create marker pop-up window
        infoWindow = CustomInfoWindow(mapView)

        // Set location marker
        locationMarker = Marker(mapView).apply {
            icon = ResourcesCompat.getDrawable(mapView.resources, R.drawable.person, null)
            setInfoWindow(null)
            setPanToView(false)
        }
        mapView.overlays.add(locationMarker)

        // Disable location tracking if any user interaction
        mapView.setOnTouchListener {_, _ ->
            infoWindow.close()
            setTracking(false)
            false
        }

        mapView.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(p: ScrollEvent): Boolean {
                lifecycleScope.launch {
                    whenResumed {
                        // Get surrounding towers
                        val nearbyTowers = withContext(Dispatchers.IO) {
                            val location = Location("").apply {
                                latitude = mapView.mapCenter.latitude
                                longitude = mapView.mapCenter.longitude
                            }

                            viewModel.getTowersByLocation(location)
                        }

                        // Limit number of towers to be displayed
                        val towers = nearbyTowers.slice(0 until min(NUM_MARKERS, nearbyTowers.size))
                        val ids = towers.map { it.towerId }

                        // Old markers to remove
                        val oldMarkers = markers.filter { it.towerId !in ids }

                        // Existing markers to keep
                        val existingMarkers = markers.filter { it.towerId in ids }
                        val existingIds = existingMarkers.map { it.towerId }

                        // New markers to add
                        val newTowers = towers.filter { it.towerId !in existingIds }
                        val newMarkers = newTowers.map { tower ->
                            val marker = CustomMarker(tower.towerId, tower.bells, infoWindow, mapView).apply {
                                position = GeoPoint(tower.latitude, tower.longitude)
                                title = tower.placeCountyList ?: tower.place

                                // Tenor weight
                                val weight = round(tower.weight / 112.0).toInt()
                                snippet = "$weight cwt"

                                val resid = if (tower.unringable) {
                                    R.drawable.tower_unringable
                                } else when (tower.bells) {
                                    1, 2, 3 -> R.drawable.tower3
                                    4 -> R.drawable.tower4
                                    5 -> R.drawable.tower5
                                    6, 7 -> R.drawable.tower6
                                    8, 9 -> R.drawable.tower8
                                    10, 11 -> R.drawable.tower10
                                    else -> R.drawable.tower12
                                }
                                icon = ResourcesCompat.getDrawable(mapView.resources, resid, null)
                            }
                            marker
                        }

                        // Update marker overlays and list of markers
                        mapView.overlays.removeAll(oldMarkers)
                        mapView.overlays.addAll(newMarkers)
                        markers = existingMarkers + newMarkers

                        // Close the info window if open on an old marker
                        if (infoWindow.isOpen) {
                            val marker = infoWindow.markerReference as CustomMarker
                            if (marker.towerId !in existingIds)
                                marker.closeInfoWindow()
                        }

                        // Redraw
                        mapView.invalidate()
                    }
                }
                return false
            }

            override fun onZoom(z: ZoomEvent): Boolean {
                return false
            }
        }, 200))
    }

    // Pause activity
    override fun onPause() {
        // Stop location requests
        locationManager?.removeUpdates(this)

        // Save settings
        sharedPrefs.edit {
            val center = mapView.mapCenter
            putFloat("map_center_latitude", center.latitude.toFloat())
            putFloat("map_center_longitude", center.longitude.toFloat())
            putFloat("map_zoom_level", mapView.zoomLevelDouble.toFloat())
            putBoolean("location_tracking", locationTracking)
            commit()
        }

        // Dismiss nearby dialog if showing
        nearbyDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        mapView.onPause()
        super.onPause()
    }

    // Resume activity
    override fun onResume() {
        super.onResume()
        mapView.onResume()

        restorePreferences()

        // Start location requests for GPS and NETWORK and set last location
        val gpsLocation = requestLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = requestLocation(LocationManager.NETWORK_PROVIDER)

        lastLocation = gpsLocation ?: networkLocation
        lastLocation?.let {
            locationMarker.position = GeoPoint(lastLocation)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Detach map manually
        mapView.onDetach()
    }

    private fun restorePreferences() {
        val lat: Double
        val lon: Double
        val zoom: Double
        val animate: Boolean

        // Get center/zoom from view model (if set) otherwise shared preferences
        val pos = viewModel.towerPosition
        if (pos == null) {
            lat = sharedPrefs.getFloat("map_center_latitude", DEFAULT_LAT).toDouble()
            lon = sharedPrefs.getFloat("map_center_longitude", DEFAULT_LON).toDouble()
            zoom = sharedPrefs.getFloat("map_zoom_level", DEFAULT_ZOOM).toDouble()
            animate = false

            setTracking(sharedPrefs.getBoolean("location_tracking", false))
        } else {
            lat = pos.latitude
            lon = pos.longitude
            zoom = 12.0
            animate = true

            setTracking(false)
            viewModel.towerPosition = null
        }
        val center = GeoPoint(lat, lon)

        // Must be zoom first, then center
        mapView.controller.setZoom(zoom)
        if (animate) {
            mapView.controller.animateTo(center)
        } else {
            mapView.controller.setCenter(center)
        }
    }

    // Dummy search function
    override fun search(pattern: String) {}

    // Custom marker with towerId property
    inner class CustomMarker(val towerId: Long, val bells: Int, infoWindow: CustomInfoWindow, mapView: MapView): Marker(mapView) {
        init {
            this.infoWindow = infoWindow
        }

        override fun onMarkerClickDefault(marker: Marker?, mapView: MapView ?) : Boolean {
            if ((marker != null) && (mapView != null)) {
                // Get list of markers closer than threshold distance
                val thresholdDistance = 3000000.0 / 2.0.pow(mapView.zoomLevelDouble)
                val nearby = markers.filter { m ->
                    marker.position.distanceToAsDouble(m.position) < thresholdDistance
                }

                if (nearby.size == 1) {
                    // Only one candiate, so simply show the info window
                    marker.showInfoWindow()
                    mapView.controller.animateTo(marker.position)

                } else {
                    // Sort markers by distance, and restrict list size to 5
                    val nearMarkers = nearby.sortedBy {
                        marker.position.distanceToAsDouble(it.position)
                    }.subList(0, min(5, nearby.size))

                    activity?.let { act ->
                        // Create dialog
                        val builder = AlertDialog.Builder(act).apply {
                            setTitle("Which tower?")
                            setItems(nearMarkers.map {it.title + " (" + it.bells + ")"}.toTypedArray()) { _, which ->
                                // Show the info window of the selected marker
                                val mrk = nearMarkers[which]
                                mrk.showInfoWindow()
                                mapView.controller.animateTo(mrk.position)
                            }
                        }

                        nearbyDialog = builder.create()
                        nearbyDialog?.show()
                    }
                }
            }

            return true
        }
    }

    // Pop-up window for custom marker
    class CustomInfoWindow(mapView: MapView):
        MarkerInfoWindow(R.layout.bubble, mapView) {

        override fun onOpen(item: Any?) {
            super.onOpen(item)

            val infoButton: Button = view.findViewById(R.id.bubble_moreinfo)
            infoButton.setOnClickListener {
                val intent = Intent(view.context, TowerInfoActivity::class.java)
                val marker = markerReference as CustomMarker
                intent.putExtra("TOWER_ID", marker.towerId)
                startActivity(view.context, intent, null)
                close()
            }

            val cancelButton: Button = view.findViewById(R.id.bubble_cancel)
            cancelButton.setOnClickListener {
                close()
            }
        }
    }

    // Enable/disable location tracking
    private fun setTracking(enabled: Boolean) {
        locationTracking = enabled

        context?.let { ctx->
            view?.let {
                val button = it.findViewById<FloatingActionButton>(R.id.button_location)
                button.setImageDrawable(ContextCompat.getDrawable(ctx, 
                    if (enabled) R.drawable.location_active else R.drawable.location_inactive))
            }
        }

        if (enabled) {
            lastLocation?.let {
                mapView.controller.animateTo(GeoPoint(it))
                mapView.invalidate()
            }
        }
    }

    // Location changed callback
    override fun onLocationChanged(location: Location) {
        // Ignore network location once GPS location have been received
        lastLocation?.let {
            if ((it.provider == LocationManager.GPS_PROVIDER) && (location.provider == LocationManager.NETWORK_PROVIDER))
                return
        }
        lastLocation = location

        locationMarker.position = GeoPoint(lastLocation)
        if (locationTracking)
            mapView.controller.animateTo(GeoPoint(location))

        mapView.invalidate()
    }

    // Shouldn't really need these next three, but seeing a crash (in Google console)
    // in LocationManager._handleMessage (Android v8) that looks they might be missing
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // Start location requests for given progider
    private fun requestLocation(provider: String): Location? {
        val permission = if (provider == LocationManager.GPS_PROVIDER) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            Manifest.permission.ACCESS_COARSE_LOCATION
        }

        val ctx = context
        return if ((ctx != null) && (ContextCompat.checkSelfPermission(
                ctx,
                permission
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            locationManager?.requestLocationUpdates(provider, LOCATION_UPDATE_INTERVAL, LOCATiON_UPDATE_DIST, this)

            var lastLocation: Location? = null
            try {
                lastLocation = locationManager?.getLastKnownLocation(provider)
            } catch (e: SecurityException) {
                Log.w(TAG, "Unexpected security exception getting find locoation")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unknown fine location provider")
            }

            if (lastLocation != null) {
                if ((System.currentTimeMillis() - lastLocation.time) > MAX_INITIAL_AGE) {
                    lastLocation = null
                }
            }
            lastLocation
        } else {
            null
        }
    }
}
