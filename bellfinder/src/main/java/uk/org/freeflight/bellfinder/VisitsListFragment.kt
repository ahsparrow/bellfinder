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
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisitsListFragment : ListFragment() {
    private val adapter = VisitsListAdapter(::onClick, ::onLongClick)
    private val viewModel: ViewModel by activityViewModels()

    override fun getAdapter() = adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.liveVisitViews.observe(this) { visits ->
            visits?.let {
                adapter.setVisits(visits)

                val tv: TextView? = view?.findViewById(R.id.textview_visits_info)
                tv?.visibility = if (visits.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun search(pattern: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                adapter.search(pattern)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun onClick(id: Long) {
        val intent = Intent(activity, VisitEditActivity::class.java)
        intent.putExtra("TOWER_ID", adapter.visitMap[id]?.towerId ?: 0)
        intent.putExtra("VISIT_ID", id)
        startActivity(intent)
    }

    private fun onLongClick(id: Long): Boolean {
        val visit = adapter.visitMap[id] ?: return true

        lifecycleScope.launch {
            val tower = withContext(Dispatchers.IO) {
                viewModel.getTower(visit.towerId)
            }
            viewModel.towerPosition = ViewModel.Position(tower.latitude, tower.longitude)

            (activity as MainActivity).setViewPage("Map", true)
        }
        return true
    }
}