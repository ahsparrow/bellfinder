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
import android.graphics.Paint
import android.location.Location
import androidx.core.content.ContextCompat.getDrawable
import uk.org.freeflight.bellfinder.db.Tower
import kotlin.math.round

class NearbyListAdapter(onClick: (id: Long) -> Unit,
                        onLongClick: (id: Long) -> Boolean
): ListAdapter("Nearby", onClick, onLongClick) {
    // Tower data from cached from database
    var towerMap = mapOf<Long, Tower>()
    private var towerDistances = mapOf<Long, Double>()

    // List of visited towers to highlight
    private var visitedTowers = listOf<Long>()

    // Location (for distance sort)
    private var location: Location? = null

    // Populate list view holder with tower details
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val id = itemIds[position]
        val current = towerMap[id]
        if (current != null) {
            // Information to be used by the selection tracker
            holder.id = id

            // Set place information in text views
            holder.place.text = current.placeCountyList ?: current.place
            holder.place.paintFlags = if (current.unringable) {
                holder.place.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.place.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            holder.place2.text = current.county ?: ""

            // Number of bells
            holder.bells.text = current.bells.toString()

            // Show a distance if available
            val dist = towerDistances[id]
            if (dist != null) {
                val miles = dist / 1609.3
                holder.extra.text = holder.itemView.context.getString(R.string.distance_formatter).format(miles)
            }

            // Show tenor weight
            val weight = round(current.weight / 112.0).toInt()
            holder.extra2.text = "$weight cwt"

            // Set background drawable
            holder.layout.background = if (visitedTowers.contains(id)) {
                getDrawable(holder.layout.context, R.drawable.visited_item_selector)
            } else {
                getDrawable(holder.layout.context, R.drawable.item_selector)
            }
        }
    }

    override fun getItemCount() : Int {
        // Less than 15 miles distant
        return towerDistances.values.count { it < 24140 }
    }

    // Set tower details (initially display all towers)
    internal fun setTowers(towers: List<Tower>) {
        itemIds = towers.map { it.towerId }
        towerMap = towers.associateBy({ it.towerId }, { it })

        // If a location has already been set then do the sort
        location?.let { sort(it) }
        notifyDataSetChanged()
    }

    // Set list of visited towers
    internal fun setVisitedTowers(towers: List<Long>) {
        visitedTowers = towers
        notifyDataSetChanged()
    }

    // Sort displayed towers by distance from given location
    fun sort(location: Location) {
        this.location = location
        towerDistances = towerMap.mapValues {
            val towerLoc = Location("").apply {
                latitude = it.value.latitude
                longitude = it.value.longitude
            }
            location.distanceTo(towerLoc).toDouble()
        }

        val tmp = itemIds.sortedBy { towerDistances[it] }
        itemIds = tmp
    }

    override fun search(pattern: String) {}
}