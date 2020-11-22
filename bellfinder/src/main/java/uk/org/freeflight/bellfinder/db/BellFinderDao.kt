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

package uk.org.freeflight.bellfinder.db

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface BellFinderDao {
    // Towers...

    // All towers
    @Query("SELECT * FROM towers ORDER BY Place ASC")
    fun liveTowers(): LiveData<List<Tower>>

    // ...non-live, unsorted
    @Query("SELECT * FROM towers")
    suspend fun getTowers() : List<Tower>

    // Single tower
    @Query("SELECT * FROM towers WHERE TowerId = :towerId LIMIT 1")
    suspend fun getTower(towerId: Long): Tower

    // Visited towers ids
    @Query("SELECT DISTINCT towerId from visits")
    fun liveVisitedTowerIds(): LiveData<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTower(tower: Tower)

    @Insert
    suspend fun insertTowers(tower: List<Tower>)

    @Query("DELETE FROM towers")
    suspend fun deleteTowers()

    // Visits...

    // Single visit from visit id
    @Query("SELECT * FROM visits WHERE visitId = :visitId LIMIT 1")
    suspend fun getVisit(visitId: Long): Visit

    // Ordered (newest first) list of visits
    @Query("SELECT * FROM VisitView ORDER BY date DESC")
    fun liveVisitViews(): LiveData<List<VisitView>>

    // ...non-live version
    @Query("SELECT * FROM VisitView ORDER BY date DESC")
    suspend fun getVisitViews(): List<VisitView>

    @Insert
    suspend fun insertVisit(visit: Visit)

    @Query("INSERT INTO visits (towerId, date, notes, peal, quarter) " +
            "VALUES(:towerId, :date, :notes, :peal, :quarter)")
    suspend fun insertVisit(towerId: Long, date: GregorianCalendar, notes: String, peal: Boolean, quarter: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisits(visits: List<Visit>)

    @Update
    suspend fun updateVisit(visit: Visit)

    @Query("DELETE FROM visits WHERE visitId = :visitId")
    suspend fun deleteVisit(visitId: Long)
}