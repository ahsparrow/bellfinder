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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

abstract class ListAdapter(val name: String,
                           private val onClick: (id: Long) -> Unit
) : RecyclerView.Adapter<ListAdapter.ItemViewHolder>() {
    // List of items in list
    protected var itemIds = emptyList<Long>()

    // View holder for text items
    class ItemViewHolder(itemView: View,
                         val onClick: (id: Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        var id: Long? = null

        val layout: ConstraintLayout = itemView.findViewById(R.id.recycler_item_layout)
        val place: TextView = itemView.findViewById(R.id.textview_recyclerview_place)
        val place2: TextView = itemView.findViewById(R.id.textview_recyclerview_place2)
        val extra: TextView = itemView.findViewById(R.id.textview_recyclerview_extra)
        val extra2: TextView = itemView.findViewById(R.id.textview_recyclerview_extra2)
        val bells: TextView = itemView.findViewById(R.id.textview_recyclerview_bells)

        init {
            itemView.setOnClickListener {
                id?.let {
                    onClick(it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_list_item, parent, false)
        return ItemViewHolder(itemView, onClick)
    }

    override fun getItemCount() = itemIds.size

    abstract fun search(pattern: String)
}