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

// Definition of the towers database table
@Entity(tableName = "towers")
data class Tower(
    @PrimaryKey  val towerId: Long,
    val place: String,
    val placeCountyList: String?,
    val county: String?,
    val dedication: String?,
    val bells: Int,
    val weight: Int,
    val unringable: Boolean,
    val practiceNight: String?,
    val practiceExtra: String?,
    val latitude: Double,
    val longitude: Double
)
