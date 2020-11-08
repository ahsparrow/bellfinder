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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.OnItemActivatedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisitsListFragment : ListFragment() {
    private val viewModel: ViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val visitsAdapter = VisitsListAdapter()
        adapter = visitsAdapter

        viewModel.liveVisitViews.observe(this, { visits ->
            visits?.let {
                visitsAdapter.setVisits(visits)

                val tv: TextView? = view?.findViewById(R.id.textview_visits_info)
                tv?.visibility =  if (visits.isEmpty()) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Selection tracker
        selectionBuilder.withOnItemActivatedListener(ItemListener()).build()

        return view
    }

    // Selected item listener
    inner class ItemListener: OnItemActivatedListener<Long> {
        override fun onItemActivated(item: ItemDetailsLookup.ItemDetails<Long>, e: MotionEvent) :
                Boolean {

            val selectionKey = item.selectionKey
            return if (selectionKey != null) {
                // Start new activity to display selected tower
                val intent = Intent(activity, VisitEditActivity::class.java)
                intent.putExtra("VISIT_ID", selectionKey)
                startActivity(intent)
                true
            }  else {
                false
            }
        }
    }

    override fun search(pattern: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                adapter.search(pattern)
            }
            adapter.notifyDataSetChanged()
        }
    }
}