/*
 * Look4Sat. Amateur radio satellite tracker and pass predictor.
 * Copyright (C) 2019-2021 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rtbishop.look4sat.ui.prefsScreen

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.rtbishop.look4sat.R
import com.rtbishop.look4sat.repository.PrefsRepo
import com.rtbishop.look4sat.utility.QthConverter
import com.rtbishop.look4sat.utility.round
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PrefsFragment : PreferenceFragmentCompat() {
    
    @Inject
    lateinit var locationManager: LocationManager
    
    @Inject
    lateinit var prefsRepo: PrefsRepo
    
    @Inject
    lateinit var qthConverter: QthConverter
    
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setPositionFromGPS()
            } else {
                showSnack(getString(R.string.pref_pos_gps_error))
            }
        }
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        findPreference<Preference>(PrefsRepo.keyPositionGPS)?.apply {
            setOnPreferenceClickListener {
                setPositionFromGPS()
                return@setOnPreferenceClickListener true
            }
        }
        
        findPreference<Preference>(PrefsRepo.keyPositionQTH)?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                setPositionFromQth(newValue.toString())
            }
        }
    }
    
    private fun setPositionFromQth(qthString: String): Boolean {
        val loc = qthConverter.qthToLocation(qthString)
        return if (loc == null) {
            showSnack(getString(R.string.pref_pos_qth_error))
            false
        } else {
            prefsRepo.setStationPosition(loc.latitude, loc.longitude, loc.heightAMSL)
            showSnack(getString(R.string.pref_pos_success))
            true
        }
    }
    
    private fun setPositionFromGPS() {
        val locPermString = Manifest.permission.ACCESS_FINE_LOCATION
        val locPermResult = ContextCompat.checkSelfPermission(requireContext(), locPermString)
        if (locPermResult == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (location != null) {
                val latitude = location.latitude.round(4)
                val longitude = location.longitude.round(4)
                val altitude = location.altitude.round(1)
                prefsRepo.setStationPosition(latitude, longitude, altitude)
                showSnack(getString(R.string.pref_pos_success))
            } else showSnack(getString(R.string.pref_pos_gps_null))
        } else requestPermissionLauncher.launch(locPermString)
    }

    private fun showSnack(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.nav_bottom)
            .show()
    }
}