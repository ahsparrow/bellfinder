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
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.org.freeflight.bellfinder.db.Visit
import java.util.*

open class VisitEditActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private val viewModel: ViewModel by viewModels()

    private var towerId: Long = 0
    private var visitId: Long? = null

    // Defaults to today's date
    private var visitDate = GregorianCalendar()

    companion object {
        const val STATE_YEAR = "year"
        const val STATE_MONTH = "month"
        const val STATE_DAY = "day"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_details)

        // Get intent data
        towerId = intent.extras!!.getLong("TOWER_ID")
        visitId = if (intent.extras!!.containsKey("VISIT_ID")) {
            intent.extras!!.getLong("VISIT_ID")
        } else {
            null
        }

        setPlaceName(towerId, visitId != null)

        val pealCheckBox: CheckBox = findViewById(R.id.checkbox_visit_peal)
        val quarterCheckBox: CheckBox = findViewById(R.id.checkbox_visit_quarter)
        val notesEditText: EditText = findViewById(R.id.edittext_visit_notes)

        if (savedInstanceState == null) {
            visitId?.let { id ->
                lifecycle.coroutineScope.launch {
                    viewModel.getVisit(id).first { visit ->
                        visitDate = visit.date
                        setVisitDate()

                        pealCheckBox.isChecked = visit.peal
                        quarterCheckBox.isChecked = visit.quarter
                        notesEditText.setText(visit.notes)
                        true
                    }
                }
            } ?: setVisitDate()
        } else {
            savedInstanceState.run {
                visitDate = GregorianCalendar(
                    getInt(STATE_YEAR), getInt(STATE_MONTH), getInt(STATE_DAY))
                setVisitDate()
            }
        }

        // Cancel button
        findViewById<Button>(R.id.button_visit_cancel).setOnClickListener {
            finish()
        }

        // Delete button
        val buttonDelete = findViewById<Button>(R.id.button_visit_delete)
        buttonDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(R.string.delete_visit)
                .setPositiveButton(R.string.yes) { _, _ ->
                    visitId?.let {
                        viewModel.deleteVisit(it)
                    }
                    finish()
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .create()
                .show()
        }

        if (visitId == null)
            buttonDelete.visibility = View.GONE

        // Save button
        findViewById<Button>(R.id.button_visit_save).setOnClickListener {
            val visit = Visit(
                visitId,
                towerId,
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.run {
            putInt(STATE_YEAR, visitDate.get(Calendar.YEAR))
            putInt(STATE_MONTH, visitDate.get(Calendar.MONTH))
            putInt(STATE_DAY, visitDate.get(Calendar.DAY_OF_MONTH))
        }
    }

    // Date picker call back
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        visitDate.clear()
        visitDate.set(year, month, day)

        setVisitDate()
    }

    private fun setPlaceName(towerId: Long, isClickable: Boolean) {
        findViewById<TextView>(R.id.textview_visit_place).setOnClickListener {
            val intent = Intent(this, TowerInfoActivity::class.java)
            intent.putExtra("TOWER_ID", towerId)
            startActivity(intent)
        }

        lifecycle.coroutineScope.launch {
            viewModel.getTower(towerId).first { tower ->
                val textView: TextView = findViewById(R.id.textview_visit_place)

                val txt = SpannableString("${tower.place}\n${tower.dedication}")
                if (isClickable) {
                    txt.setSpan(RelativeSizeSpan(0.8f), tower.place.length, txt.length, 0)
                }

                textView.text = txt
                textView.isClickable = isClickable
                textView.isFocusable = isClickable
                true
            }
        }
    }

    // Format and display date
    private fun setVisitDate() {
        val txt = getString(R.string.date_format_long).format(visitDate)
        val span = SpannableString(txt).apply {
            setSpan(UnderlineSpan(), 0, txt.length, 0)
        }

        findViewById<TextView>(R.id.textview_visit_date).text = span
    }
}