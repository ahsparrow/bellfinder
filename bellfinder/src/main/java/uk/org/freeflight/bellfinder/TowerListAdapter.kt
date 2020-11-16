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

import android.graphics.Paint
import androidx.core.content.ContextCompat.getDrawable
import uk.org.freeflight.bellfinder.db.Tower

class TowerListAdapter(onClick: (id: Long) -> Unit,
                       onLongClick: (id: Long) -> Boolean
): ListAdapter("Towers", onClick, onLongClick) {

    // Tower data cached from database
    var towerMap = mapOf<Long, Tower>()

    // List of visited towers to highlight
    private var visitedTowers = listOf<Long>()

    // Populate list view holder with tower details
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val id = itemIds[position]
        val current = towerMap[id]
        if (current != null) {
            // Information to be used by onClick
            holder.id = id

            // Set place information in text views
            holder.place.text = current.place
            holder.place.paintFlags = if (current.unringable) {
                holder.place.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.place.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            holder.place2.text = if (current.place2 != "") current.place2 else current.dedication

            // Number of bells
            holder.bells.text = current.bells.toString()

            // Set background drawable
            holder.layout.background = if (visitedTowers.contains(id)) {
                getDrawable(holder.layout.context, R.drawable.recycler_item_selector_visited)
            } else {
                getDrawable(holder.layout.context, R.drawable.recycler_item_selector)
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