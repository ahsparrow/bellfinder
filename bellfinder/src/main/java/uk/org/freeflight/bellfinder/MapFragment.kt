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
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.preference.PreferenceManager
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

class MapFragment : SearchableFragment() {
    companion object {
        const val NUM_MARKERS = 100

        const val DEFAULT_LAT = 51.04F
        const val DEFAULT_LON = -1.58F
        const val DEFAULT_ZOOM = 10.0F
    }

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var mapView: MapView
    private var markers = listOf<CustomMarker>()
    private lateinit var infoWindow: CustomInfoWindow

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            val ctx = it.applicationContext
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            Configuration.getInstance().load(ctx, sharedPrefs)
        }
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set centre and zoom
        restorePreferences()

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

        mapView.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(p: ScrollEvent): Boolean {
                lifecycleScope.launch {
                    whenResumed {
                        // Get surrounding towers
                        val nearbyTowers = withContext(Dispatchers.IO) {
                            val centre = GeoPoint(mapView.mapCenter)
                            val location = Location("").apply {
                                latitude = centre.latitude
                                longitude = centre.longitude
                            }

                            viewModel.getTowersByLocation(location)
                        }

                        // Limit number of towers to be displayed
                        val towers = nearbyTowers.slice(0 until NUM_MARKERS)
                        val ids = towers.map { it.towerId }

                        // Old markers to remove
                        val oldMarkers = markers.filter { it.towerId !in ids }

                        // Existing markers to keep
                        val existingMarkers = markers.filter { it.towerId in ids }
                        val existingIds = existingMarkers.map { it.towerId }

                        // New markers to add
                        val newTowers = towers.filter { it.towerId !in existingIds }
                        val newMarkers = newTowers.map { tower ->
                            val marker = CustomMarker(tower.towerId, infoWindow, mapView).apply {
                                position = GeoPoint(tower.latitude, tower.longitude)
                                title = if (tower.place2 != "") {
                                    tower.place + ", " + tower.place2
                                } else {
                                    tower.place
                                }
                                snippet = tower.dedication

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
                                icon = ResourcesCompat.getDrawable(view.resources, resid, null)
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

    override fun onPause() {
        super.onPause()

        sharedPrefs.edit {
            val center = mapView.mapCenter
            putFloat("map_center_latitude", center.latitude.toFloat())
            putFloat("map_center_longitude", center.longitude.toFloat())
            putFloat("map_zoom_level", mapView.zoomLevelDouble.toFloat())
            commit()
        }

        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        restorePreferences()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Detach map manually
        mapView.onDetach()
    }

    private fun restorePreferences() {
        val lat = sharedPrefs.getFloat("map_center_latitude", DEFAULT_LAT).toDouble()
        val lon = sharedPrefs.getFloat("map_center_longitude", DEFAULT_LON).toDouble()
        val zoom = sharedPrefs.getFloat("map_zoom_level", DEFAULT_ZOOM).toDouble()
        mapView.controller.setCenter(GeoPoint(lat, lon))
        mapView.controller.setZoom(zoom)
    }

    override fun search(pattern: String) {}

    class CustomMarker(val towerId: Long, infoWindow: CustomInfoWindow, mapView: MapView): Marker(mapView) {
        init {
            this.infoWindow = infoWindow
        }
    }

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
            }

            val cancelButton: Button = view.findViewById(R.id.bubble_cancel)
            cancelButton.setOnClickListener {
                markerReference.closeInfoWindow()
            }
        }
    }
}
