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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TowerListFragment: ListFragment() {
    private val adapter = TowerListAdapter(::onClick, ::onLongClick)
    private val viewModel: ViewModel by activityViewModels()

    override fun getAdapter() = adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.coroutineScope.launch {
            viewModel.getTowers.first { towers ->
                towers.let { adapter.setTowers(towers) }
                true
            }
        }

        lifecycle.coroutineScope.launch {
            viewModel.getVisitedTowerIds.collect { towerIds ->
                towerIds.let { adapter.setVisitedTowers(towerIds) }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun search(pattern: String) {
        adapter.search(pattern)
        adapter.notifyDataSetChanged()
    }

    private fun onClick(id: Long) {
        val intent = Intent(activity, TowerInfoActivity::class.java)
        intent.putExtra("TOWER_ID", id)
        startActivity(intent)
    }

    private fun onLongClick(id: Long): Boolean {
        val tower = adapter.towerMap[id]
        viewModel.towerPosition = tower?.let {ViewModel.Position(tower.latitude, tower.longitude)}

        (activity as MainActivity).setViewPage("Map", true)
        return true
    }
}