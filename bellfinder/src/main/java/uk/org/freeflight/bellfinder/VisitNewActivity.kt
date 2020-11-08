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

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class VisitNewActivity : VisitEditActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set place name text and disable clicking
        towerId?.let {
            setPlaceName(it, false)
        }
        findViewById<TextView>(R.id.textview_visit_place).isClickable = false

        // Hide the delete button
        val deleteButton = findViewById<Button>(R.id.button_visit_delete)
        deleteButton.visibility = View.GONE

        setVisitDate()
    }
}