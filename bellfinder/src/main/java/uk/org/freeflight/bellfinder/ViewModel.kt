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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import uk.org.freeflight.bellfinder.db.*
import java.util.*

class ViewModel (application: Application) : AndroidViewModel(application) {
    private val dao: BellFinderDao = BellFinderDatabase.getDatabase(application).bellFinderDao()

    // List of towers
    val getTowers: Flow<List<Tower>> = dao.getTowers()

    // Get single tower
    suspend fun getTower(towerId: Long): Tower = dao.getTower(towerId)

    // Get towers in area
    suspend fun getTowersByArea(boundingBox: BoundingBox): List<Tower> {
        val towers = dao.getTowersX()

        return towers.filter { boundingBox.contains(GeoPoint(it.latitude, it.longitude)) }
    }

    // Insert towers
    suspend fun insertTowers(towers: List<Tower>) = dao.insertTowers(towers)

    // Delete all towers
    suspend fun deleteTowers() = dao.deleteTowers()

    // Live list (unordered) of visited tower IDs
    val liveVisitedTowerIds: LiveData<List<Long>> = dao.liveVisitedTowerIds()

    // Live visit views order by date
    val liveVisitViews: LiveData<List<VisitView>> = dao.liveVisitViews()

    // ...non-live version of same
    suspend fun getVisitViews(): List<VisitView> = dao.getVisitViews()

    suspend fun getVisit(visitId: Long): Visit = dao.getVisit(visitId)

    fun liveTowerVisits(towerId: Long): LiveData<List<Visit>> = dao.liveTowerVisits(towerId)

    fun insertVisit(visit: Visit) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertVisit(visit)
    }

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

    fun updateVisit(visit: Visit) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateVisit(visit)
    }

    // Delete a visit
    fun deleteVisit(visitId: Long) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteVisit(visitId)
    }

    // Tower position to pass to map
    var towerPosition: Position? = null

    class Position(val latitude: Double, val longitude: Double)

    val getPreferences: Flow<Preferences> = dao.getPreferences()

    fun updatePreferences(unringable: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dao.updatePreferences(unringable)
    }
}
