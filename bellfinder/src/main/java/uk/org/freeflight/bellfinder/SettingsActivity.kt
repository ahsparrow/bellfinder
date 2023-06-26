package uk.org.freeflight.bellfinder

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
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
                viewModel.getPreferences.collect() {
                    Log.w(TAG, "Collect " + it)
                    val pref = findPreference<SwitchPreference>("unringable_preference")
                    pref?.isChecked = it.unringable
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val pref = findPreference<SwitchPreference>("unringable_preference")
            pref?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { key, value ->
                    Log.w(TAG, "Change " + key + " " + value)
                    viewModel.updatePreferences(value == true)
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