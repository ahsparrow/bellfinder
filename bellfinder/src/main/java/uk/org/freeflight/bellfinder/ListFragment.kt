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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class ListFragment: SearchableFragment() {
    protected lateinit var selectionBuilder: SelectionTracker.Builder<Long>
    lateinit var adapter: ListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recycler_list, container, false)

        //  Setup RecyclerView
        val recyclerView = view.findViewById(R.id.recyclerview) as RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.contentDescription = adapter.name

        // Selection tracker
        selectionBuilder = SelectionTracker.Builder(
            "selection",
            recyclerView,
            ItemIdKeyProvider(adapter),
            ItemLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())

        return view
    }

    // Selection tracker - get item key/position
    class ItemIdKeyProvider(private val adapter: ListAdapter) :
        ItemKeyProvider<Long>(SCOPE_CACHED) {

        override fun getKey(position: Int): Long? {
            return adapter.getItemKey(position)
        }

        override fun getPosition(key: Long): Int {
            return adapter.findPositionFromKey(key)
        }
    }

    // Selection tracker - get item details from motion position
    class ItemLookup(private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent) : ItemDetails<Long>? {

            val view = rv.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (rv.getChildViewHolder(view) as ListAdapter.ItemViewHolder)
                    .getItemDetails()
            }
            return null
        }
    }
}
