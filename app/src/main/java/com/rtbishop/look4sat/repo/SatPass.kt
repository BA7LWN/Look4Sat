/*
 * Look4Sat. Amateur radio & weather satellites passes calculator for Android.
 * Copyright (C) 2019, 2020 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.rtbishop.look4sat.repo

import android.os.Parcelable
import com.github.amsacode.predict4java.SatPassTime
import com.github.amsacode.predict4java.TLE
import com.rtbishop.look4sat.predict4kotlin.PassPredictor
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class SatPass(
    val tle: TLE,
    val predictor: @RawValue PassPredictor,
    val pass: @RawValue SatPassTime,
    var progress: Int = 0,
    var active: Boolean = false
) : Parcelable