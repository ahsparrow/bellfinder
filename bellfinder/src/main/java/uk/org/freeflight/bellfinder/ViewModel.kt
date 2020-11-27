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

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.org.freeflight.bellfinder.db.*
import java.io.InputStream
import java.util.*

class ViewModel (application: Application) : AndroidViewModel(application) {
    private val dao: BellFinderDao = BellFinderDatabase.getDatabase(application).bellFinderDao()

    // Live list (ordered) of all towers
    val liveTowers: LiveData<List<Tower>> = dao.liveTowers()

    // Get single tower
    suspend fun getTower(towerId: Long): Tower = dao.getTower(towerId)

    // Sorted list of towers
    suspend fun getTowersByLocation(location: Location): List<Tower> {
        val towers = dao.getTowers()

        val distances = towers.map {
            val loc = Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            }

            it.towerId to location.distanceTo(loc).toDouble()
        }.toMap()

        return towers.sortedBy { distances[it.towerId] }
    }

    // Delete all towers
    fun deleteTowers() = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteTowers()
    }

    // Live list (unordered) of visited tower IDs
    val liveVisitedTowerIds: LiveData<List<Long>> = dao.liveVisitedTowerIds()

    // Live visit views order by date
    val liveVisitViews: LiveData<List<VisitView>> = dao.liveVisitViews()

    // ...non-live version of same
    suspend fun getVisitViews(): List<VisitView> = dao.getVisitViews()

    suspend fun getVisit(visitId: Long): Visit = dao.getVisit(visitId)

    suspend fun getTowerVisits(towerId: Long): List<Visit> = dao.getTowerVisits(towerId)

    suspend fun insertVisit(visit: Visit) = dao.insertVisit(visit)

    fun insertVisit(towerId: Long,
                    date: GregorianCalendar,
                    notes: String,
                    peal: Boolean,
                    quarter: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertVisit(towerId, date, notes, peal, quarter)
    }

    fun insertVisits(visits: List<Visit>) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertVisits(visits)
    }

    suspend fun updateVisit(visit: Visit) = dao.updateVisit(visit)

    // Delete a visit
    fun deleteVisit(visitId: Long) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteVisit(visitId)
    }

    // Read data from dove file asset into database
    fun parseDove(stream: InputStream) = viewModelScope.launch(Dispatchers.IO) {
        // Load from asset file
        val data = stream.bufferedReader().use { it.readLines() }
        dao.insertTowers(parseDove(data))
    }

    // Tower position to pass to map
    var towerPosition: Position? = null

    class Position(val latitude: Double, val longitude: Double)
}
