/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 4/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.jsbridge

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.location.LocationManager.PASSIVE_PROVIDER
import android.os.Bundle
import android.os.Looper
import android.webkit.JavascriptInterface
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val LOCATION_REQUEST_TIMEOUT = 30_000L

class LocationJs(
    private val promise: NativePromiseFactory,
    private val locationPermissionFlow: Flow<Boolean>,
    private val locationManager: LocationManager?
) : JsApi {

    private val locationUpdateChannel = BroadcastChannel<Location>(BUFFERED)

    private val networkListener = object : DefaultListener() {
        override fun onLocationChanged(location: Location) {
            location.run(locationUpdateChannel::offer)
        }
    }

    private val gpsListener = object : DefaultListener() {
        override fun onLocationChanged(location: Location) {
            location.run(locationUpdateChannel::offer)
        }
    }

    private val passiveListener = object : DefaultListener() {
        override fun onLocationChanged(location: Location) {
            location.run(locationUpdateChannel::offer)
        }
    }

    @SuppressLint("MissingPermission")
    @JavascriptInterface
    fun getLocation() = promise.create {
        checkNotNull(locationManager)
        check(locationPermissionFlow.first()) {
            "Location permission denied."
        }
        check(locationManager.run {
            isProviderEnabled(GPS_PROVIDER)
                || isProviderEnabled(NETWORK_PROVIDER)
                || isProviderEnabled(PASSIVE_PROVIDER)
        }) { "No location providers enabled." }

        withTimeout(LOCATION_REQUEST_TIMEOUT) {
            locationUpdateChannel.asFlow()
                .take(1)
                .onStart {
                    locationManager.requestSingleUpdate(NETWORK_PROVIDER, networkListener, Looper.getMainLooper())
                    locationManager.requestSingleUpdate(GPS_PROVIDER, gpsListener, Looper.getMainLooper())
                    locationManager.requestSingleUpdate(PASSIVE_PROVIDER, passiveListener, Looper.getMainLooper())
                }
                .flowOn(Main)
                .map { getJsonLocation(it) }
                .flowOn(Default)
                .onCompletion {
                    locationManager.removeUpdates(networkListener)
                    locationManager.removeUpdates(gpsListener)
                    locationManager.removeUpdates(passiveListener)
                }
                .first()
        }
    }

    private fun getJsonLocation(location: Location): JSONObject {
        val coordObj = JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
        }
        return JSONObject().apply {
            put("timestamp", location.time)
            put("coordinate", coordObj)
            put("altitude", location.altitude)
            put("horizontal_accuracy", location.accuracy.toDouble())
            put("description", "")
        }
    }

    private open class DefaultListener : LocationListener {
        override fun onLocationChanged(location: Location) = Unit
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
    }
}

