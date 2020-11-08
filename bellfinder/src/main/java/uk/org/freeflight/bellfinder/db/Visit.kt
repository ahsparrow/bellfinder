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

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// Definition of the visits database table
@Entity(tableName = "visits")
data class Visit(
    @PrimaryKey(autoGenerate = true) val visitId: Long?,
    val towerId: Long,
    val date: GregorianCalendar,
    var notes: String?,
    val peal: Boolean,
    val quarter: Boolean
)