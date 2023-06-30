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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import uk.org.freeflight.bellfinder.db.BellFinderDao
import uk.org.freeflight.bellfinder.db.BellFinderDatabase
import uk.org.freeflight.bellfinder.db.Preferences
import uk.org.freeflight.bellfinder.db.Tower
import uk.org.freeflight.bellfinder.db.Visit
import uk.org.freeflight.bellfinder.db.VisitView
import java.util.*

class ViewModel (application: Application) : AndroidViewModel(application) {
    private val dao: BellFinderDao = BellFinderDatabase.getDatabase(application).bellFinderDao()

    // List of towers
    val getTowers: Flow<List<Tower>> = dao.getPrefTowers()

    // Single tower
    fun getTower(towerId: Long): Flow<Tower> = dao.getTower(towerId)

    // Insert towers
    suspend fun insertTowers(towers: List<Tower>) = dao.insertTowers(towers)

    // Delete all towers
    suspend fun deleteTowers() = dao.deleteTowers()

    // Visited tower IDs
    val getVisitedTowerIds: Flow<List<Long>> = dao.getVisitedTowerIds()

    // Live visit views order by date
    val getVisitViews: Flow<List<VisitView>> = dao.getVisitViews()

    fun getVisit(visitId: Long): Flow<Visit> = dao.getVisit(visitId)

    fun getTowerVisits(towerId: Long): Flow<List<Visit>> = dao.getTowerVisits(towerId)

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

    // Preferences
    val getPreferences: Flow<Preferences> = dao.getPreferences()

    fun updatePrefsUnringable(unringable: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dao.updatePrefsUnringable(unringable)
    }

    fun updatePrefsBells(bells: String) = viewModelScope.launch(Dispatchers.IO) {
        dao.updatePrefsBells(bells)
    }
}
