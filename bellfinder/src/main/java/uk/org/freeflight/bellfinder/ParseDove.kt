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

    val towers = data.map { tower ->
        Tower(
            tower.getValue("towerId").toLong(),
            tower.getValue("place"),
            tower.getValue("county"),
            tower.getValue("dedication"),
            tower.getValue("bells").toInt(),
            tower.getValue("weight").toInt(),
            tower.getValue("unringable") == "True",
            tower.getValue("practice"),
            tower.getValue("latitude").toDouble(),
            tower.getValue("longitude").toDouble()
        )
    }

    return towers
}