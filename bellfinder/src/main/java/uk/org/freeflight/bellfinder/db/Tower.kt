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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Definition of the towers database table
@Entity(tableName = "towers")
data class Tower(
    @PrimaryKey @ColumnInfo(name = "TowerID") val towerId: Long,
    @ColumnInfo(name = "Place") val place: String,
    @ColumnInfo(name = "Place2") val place2: String,
    @ColumnInfo(name = "County") val county: String,
    @ColumnInfo(name = "Dedicn") val dedication: String,
    @ColumnInfo(name = "Bells") val bells: Int,
    @ColumnInfo(name = "Wt") val weight: Int,
    @ColumnInfo(name = "UR") val unringable: Boolean,
    @ColumnInfo(name = "PracN") val practiceNight: String,
    @ColumnInfo(name = "PrXF") val practiceExtra: String,
    @ColumnInfo(name = "Lat") val latitude: Double,
    @ColumnInfo(name = "Long") val longitude: Double
) {
    constructor(data: Map<String, String>) : this(
        data.getValue("TowerBase").toLong(),
        data.getValue("Place"),
        data.getValue("Place2"),
        data.getValue("County"),
        data.getValue("Dedicn"),
        data.getValue("Bells").toInt(),
        data.getValue("Wt").toInt(),
        data.getValue("UR") != "",
        data.getValue("PracN"),
        data.getValue("PrXF"),
        data.getValue("Lat").toDouble(),
        data.getValue("Long").toDouble()
    )

    companion object {
        // These are really the field names in the Dove data file
        val DB_COLS = listOf("TowerBase", "Place", "Place2", "County", "Dedicn", "Bells", "Wt", "UR", "PracN", "PrXF", "Lat", "Long")
    }
}
