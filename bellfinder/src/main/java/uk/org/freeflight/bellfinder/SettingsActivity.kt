package uk.org.freeflight.bellfinder

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val viewModel: ViewModel by viewModels()

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar_settings))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(viewModel))
                .commit()
        }
    }

    class SettingsFragment(private var viewModel: ViewModel) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            lifecycle.coroutineScope.launch {
                viewModel.getPreferences.collect { prefs ->
                    val prefUnringable = findPreference<SwitchPreference>("unringable_preference")
                    prefUnringable?.isChecked = prefs.unringable

                    val prefBells = findPreference<MultiSelectListPreference>("bells_preference")
                    prefBells?.values = prefs.bells.map { it.toString() }.toSet()
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val unringablePref = findPreference<SwitchPreference>("unringable_preference")
            unringablePref?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, value ->
                    viewModel.updatePrefsUnringable(value == true)
                    true
                }

            val bellsPref = findPreference<MultiSelectListPreference>("bells_preference")
            bellsPref?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, value ->
                    @Suppress("UNCHECKED_CAST")
                    val bellSet: Set<String> = value as Set<String>
                    val bells = if (bellSet.isEmpty()) "" else  bellSet.reduce { acc, string -> acc + string }

                    viewModel.updatePrefsBells(bells)
                    true
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}