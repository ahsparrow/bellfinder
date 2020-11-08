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

import androidx.core.content.ContextCompat.getColor
import uk.org.freeflight.bellfinder.db.Tower

class TowerListAdapter : ListAdapter("Towers") {

    // Tower data cached from database
    private var towerMap = mapOf<Long, Tower>()

    // List of visited towers to highlight
    private var visitedTowers = listOf<Long>()

    // Populate list view holder with tower details
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val id = itemIds[position]
        val current = towerMap[id]
        if (current != null) {
            // Information to be used by the selection tracker
            holder.id = id
            holder.pos = position

            // Set place information in text views
            holder.place.text = current.place
            holder.place2.text = if (current.place2 != "") current.place2 else current.dedication

            // Number of bells
            holder.extra2.text = current.bells.toString()

            // Set background colour
            if (visitedTowers.contains(id)) {
                holder.layout.setBackgroundColor(
                    getColor(
                        holder.itemView.context,
                        R.color.visited_item_bg
                    )
                )
            } else {
                holder.layout.setBackgroundColor(
                    getColor(
                        holder.itemView.context,
                        R.color.recycler_item_bg
                    )
                )
            }
        }
    }

    // Set tower details (initially display all towers)
    internal fun setTowers(towers: List<Tower>) {
        itemIds = towers.map { it.towerId }
        towerMap = towers.associateBy({ it.towerId }, { it })
        notifyDataSetChanged()
    }

    // Set list of visited towers
    internal fun setVisitedTowers(towers: List<Long>) {
        visitedTowers = towers
        notifyDataSetChanged()
    }

    // Filter displayed towers by match with start of word name
    override fun search(pattern: String) {
        itemIds = if (pattern == "") {
            towerMap.map { it.key }
        } else {
            val regex = Regex("\\b$pattern", RegexOption.IGNORE_CASE)

            towerMap.filter {
                regex.containsMatchIn(it.value.place) || regex.containsMatchIn(it.value.place2)
            }.map { it.key }
        }
    }
}