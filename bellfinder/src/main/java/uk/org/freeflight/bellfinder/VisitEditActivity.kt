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

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.freeflight.bellfinder.db.Visit
import java.util.*

open class VisitEditActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private val viewModel: ViewModel by viewModels()

    protected var towerId: Long? = null
    private var visitId: Long? = null
    private var visitDate = GregorianCalendar()

    companion object {
        const val STATE_YEAR = "year"
        const val STATE_MONTH = "month"
        const val STATE_DAY = "day"
        const val STATE_TOWERID = "towerid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_details)

        // Get intent data
        towerId = intent.extras?.get("TOWER_ID") as Long?
        visitId = intent.extras?.get("VISIT_ID") as Long?

        val pealCheckBox: CheckBox = findViewById(R.id.checkbox_visit_peal)
        val quarterCheckBox: CheckBox = findViewById(R.id.checkbox_visit_quarter)
        val notesEditText: EditText = findViewById(R.id.edittext_visit_notes)

        // Initialise a previous visit
        if (savedInstanceState == null) {
            visitId?.let { id ->
                lifecycleScope.launch {
                    val visit = withContext(Dispatchers.IO) {
                        viewModel.getVisit(id)
                    }

                    visitDate = visit.date
                    pealCheckBox.isChecked = visit.peal
                    quarterCheckBox.isChecked = visit.quarter
                    notesEditText.setText(visit.notes)

                    towerId = visit.towerId
                    setPlaceName(visit.towerId, true)

                    setVisitDate()
                }
            }
        } else {
            savedInstanceState.run {
                visitDate = GregorianCalendar(
                    getInt(STATE_YEAR), getInt(STATE_MONTH), getInt(STATE_DAY))
                setVisitDate()

                val id = getLong(STATE_TOWERID)
                if (id != 0L)  {
                    towerId = id
                    setPlaceName(id, true)
                }
            }
        }

        // Cancel button
        findViewById<Button>(R.id.button_visit_cancel).setOnClickListener {
            finish()
        }

        // Delete button
        findViewById<Button>(R.id.button_visit_delete).setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(R.string.delete_visit)
                .setPositiveButton(R.string.yes) { _, _ ->
                    visitId?.let { viewModel.deleteVisit(it) }
                    finish()
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
        }

        // Save button
        findViewById<Button>(R.id.button_visit_save).setOnClickListener {
            towerId?.let { id ->
                val visit = Visit(
                    visitId,
                    id,
                    visitDate,
                    notesEditText.text.toString(),
                    pealCheckBox.isChecked,
                    quarterCheckBox.isChecked
                )

                if (visitId == null) {
                    viewModel.insertVisit(visit)
                } else {
                    viewModel.updateVisit(visit)
                }
            }
            finish()
        }

        // Date clicked
        findViewById<TextView>(R.id.textview_visit_date).setOnClickListener {
            DatePickerDialog(
                this,
                this,
                visitDate.get(Calendar.YEAR),
                visitDate.get(Calendar.MONTH),
                visitDate.get(Calendar.DAY_OF_MONTH)
            ).show()

        }

        // Peal checkbox
        pealCheckBox.setOnClickListener {
            if (pealCheckBox.isChecked)
                quarterCheckBox.isChecked = false
        }

        // Quarter checkbox
        quarterCheckBox.setOnClickListener {
            if (quarterCheckBox.isChecked)
                pealCheckBox.isChecked = false
        }

        findViewById<TextView>(R.id.textview_visit_place).setOnClickListener {
            val intent = Intent(this, TowerInfoActivity::class.java)
            intent.putExtra("TOWER_ID", towerId)
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.run {
            putInt(STATE_YEAR, visitDate.get(Calendar.YEAR))
            putInt(STATE_MONTH, visitDate.get(Calendar.MONTH))
            putInt(STATE_DAY, visitDate.get(Calendar.DAY_OF_MONTH))

            towerId?.let {
                putLong(STATE_TOWERID, it)
            }
        }
    }

    // Date picker call back
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        visitDate.clear()
        visitDate.set(year, month, day)

        setVisitDate()
    }

    // Get tower info from database and update text view
    protected fun setPlaceName(towerId: Long, underline: Boolean) {
        lifecycleScope.launch {
            val tower = withContext(Dispatchers.IO) {
                viewModel.getTower(towerId)
            }

            val tv: TextView = findViewById(R.id.textview_visit_place)
            tv.text = if (tower.place2 != "") {
                "${tower.place2}, ${tower.dedication}"
            } else {
                "${tower.place}, ${tower.dedication}"
            }

            if (underline)
                tv.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }
    }

    // Format and display date
    protected fun setVisitDate() {
        findViewById<TextView>(R.id.textview_visit_date).text = getString(R.string.date_format_long).format(visitDate)
    }
}