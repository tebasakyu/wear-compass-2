package com.tebasaki.yu.wear.compass.fragment

import android.app.Activity
import android.app.Fragment
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle

import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable

/**
 * Detector location on device.
 *
 * Must implement OnLocationListener when use this fragment
 */
public class DetectLocationFragment : Fragment() ,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mCallback : OnLocationListener? = null

    companion object {

        private val UPDATE_INTERVAL_MS = 500L
        private val FASTEST_INTERVAL_MS= 500L

        fun newInstance() : DetectLocationFragment {
            return DetectLocationFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hardware has GPS check
        if (! hasGps()) {
            Log.w("", "This hardware doesn't have GPS.")
            activity.fragmentManager.popBackStack()
        }

        // build GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build()
    }


    override fun onResume() {
        super.onResume()
        mGoogleApiClient?.connect()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            mCallback = activity as OnLocationListener
        } catch(ex: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnLocationListener")
        }
    }

    override fun onPause() {
        super.onPause()

        if (null != mGoogleApiClient) {
            if (mGoogleApiClient!!.isConnected) {
                LocationServices.FusedLocationApi
                        .removeLocationUpdates(mGoogleApiClient, this);
            }
            mGoogleApiClient?.disconnect()
        }
    }


    override fun onConnected(bundle: Bundle?) {

        // settings
        var locationRequest: LocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS)

        // request location
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(object: ResultCallback<Status> {
                    override fun onResult(status: Status) {

                        if (status.status.isSuccess) {
                            Log.d("", "Successfully requested location updates")
                        } else {
                            Log.e("",
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.statusCode
                                            + ", message: "
                                            + status.statusMessage)
                        }
                    }
                })
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d("", "connection to location client suspended")
    }

    override fun onLocationChanged(location: Location) {
        mCallback?.onLocationChanged(location)
    }


    /**
     * Check has feature GPS.
     */
    private fun hasGps() : Boolean {
        return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    }


    public interface OnLocationListener {
        fun onLocationChanged(location: Location)
    }
}