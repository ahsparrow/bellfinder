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

import android.annotation.SuppressLint
import uk.org.freeflight.bellfinder.db.VisitView

class VisitsListAdapter : ListAdapter("Visits") {
    // Visit data from cached from database
    private var visitMap = mapOf<Long, VisitView>()

    // Populate list view holder with tower details
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val id = itemIds[position]
        val current = visitMap[id]
        if (current != null) {
            // Information to be used by the selection tracker
            holder.id = id
            holder.pos = position

            // Set place information in text views
            holder.place.text = current.place
            holder.place2.text = if (current.place2 != "") current.place2 else current.dedication

            holder.extra.text = holder.itemView.context
                .getString(R.string.date_format_short).format(current.date)

            val pq = if (current.peal) "P" else if (current.quarter) "Q" else ""
            holder.extra2.text = if (pq == "") current.bells.toString() else "$pq ${current.bells}"
        }
    }

    // Set visit details
    internal fun setVisits(visits: List<VisitView>) {
        itemIds = visits.map { it.visitId }
        visitMap = visits.associateBy({ it.visitId }, { it })

        notifyDataSetChanged()
    }

    // Filter displayed towers by match with start of word name
    override fun search(pattern: String) {
        itemIds = if (pattern == "") {
            visitMap.map { it.key }
        } else {
            val regex = Regex("\\b$pattern", RegexOption.IGNORE_CASE)

            visitMap.filter {
                regex.containsMatchIn(it.value.place) || regex.containsMatchIn(it.value.place2)
            }.map { it.key }
        }
    }
}