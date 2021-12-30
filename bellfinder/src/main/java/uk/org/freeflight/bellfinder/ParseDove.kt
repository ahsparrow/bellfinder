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

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import uk.org.freeflight.bellfinder.db.Tower
import java.io.InputStream

fun parseDove(input: InputStream) : List<Tower> {
    val data = csvReader().readAllWithHeader(input)

    fun String.maybeNull(): String? {return if (this == "") null else this}

    val towers = data.map { tower ->
        Tower(
            tower.getValue("TowerBase").toLong(),
            tower.getValue("Place"),
            tower.getValue("PlaceCL").maybeNull(),
            tower.getValue("County").maybeNull(),
            tower.getValue("Dedicn").maybeNull(),
            tower.getValue("Bells").toInt(),
            tower.getValue("Wt").toInt(),
            tower.getValue("UR") != "",
            tower.getValue("Practice").maybeNull(),
            tower.getValue("Lat").toDouble(),
            tower.getValue("Long").toDouble()
        )
    }

    return towers
}