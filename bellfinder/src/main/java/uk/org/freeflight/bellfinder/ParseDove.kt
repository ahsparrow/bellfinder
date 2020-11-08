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

import android.util.Log
import uk.org.freeflight.bellfinder.db.Tower

fun parseDove(data: List<String>) : List<Tower> {
    val columns = data[0].split("\\")

    // Check for correct column names
    for (c  in Tower.DB_COLS) {
        if (c !in columns ) {
            Log.w(TAG, "Dove data: missing column name: $c")
            return emptyList()
        }
    }

    // Get column indices for database fields
    val indices = Tower.DB_COLS.map {columns.indexOf(it)}

    val result = mutableListOf<Tower>()
    data.subList(1, data.size).forEachIndexed { i, line ->
        val doveValues = line.split("\\")

        // Check we have correct number of values
        if (doveValues.size != columns.size) {
            Log.w(TAG, "Dove data: wrong number of fields at line ${i + 1}")
            return emptyList()
        }

        // Extract required values and create map
        val values = indices.map {doveValues[it]}
        val t = Tower.DB_COLS.zip(values).toMap()

        val towerInfo = try {
            Tower(t)
        }
        catch (e: NumberFormatException) {
            Log.w(TAG, "Dove data: bad value at line ${i + 1}")
            null
        }

        // Add new tower to result
        if (towerInfo != null) {
            result.add(towerInfo)
        }
    }

    return result
}