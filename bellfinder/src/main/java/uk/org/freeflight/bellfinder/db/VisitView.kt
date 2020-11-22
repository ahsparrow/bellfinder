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

import androidx.room.DatabaseView
import java.util.*

@DatabaseView(
    "SELECT visits.visitId, visits.towerId, visits.date, visits.notes, " +
            "visits.peal, visits.quarter, " +
            "towers.place AS place, towers.place2 AS place2, " +
            "towers.dedicn AS dedication, towers.bells AS bells " +
            "FROM visits INNER JOIN towers ON visits.towerId = towers.towerId")
data class VisitView (
    val visitId: Long,
    val towerId: Long,
    val date: GregorianCalendar,
    val notes: String,
    val peal: Boolean,
    val quarter: Boolean,
    val place: String,
    val place2: String,
    val dedication: String,
    val bells: Int
)