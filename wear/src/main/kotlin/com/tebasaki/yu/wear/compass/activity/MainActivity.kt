package com.tebasaki.yu.wear.compass.activity

import android.location.Location
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.WatchViewStub
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import com.tebasaki.yu.wear.compass.R
import com.tebasaki.yu.wear.compass.fragment.DetectCompassFragment
import com.tebasaki.yu.wear.compass.fragment.DetectLocationFragment

public class MainActivity : WearableActivity(),
                            DetectLocationFragment.OnLocationListener,
                            DetectCompassFragment.OnCompassListener {

    private var mGoogleApiClient: GoogleApiClient? = null

    private var mCanUpdateViews: Boolean = false
    private var mFromAzimuth: Float = 0f
    private var mToAzimuth: Float = 0f
    private var mDestinationLatitude = 0.0
    private var mDestinationLongitude = 0.0

    private var mRemainingDistance: TextView? = null
    private var mArrowImg: ImageView? = null

    companion object {
        val TAG: String = MainActivity.javaClass.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // supports ambient mode
        setAmbientEnabled()
        // use GoogleApiClient for receive message in Activity
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(object: GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {
                        Wearable.MessageApi.addListener(mGoogleApiClient, {
                            messageEvent ->
                                if (messageEvent.path.equals("/locale_set")) {

                                    // parse data from mobile
                                    val message = String(messageEvent.data)
                                    Log.d(TAG, "message = $message")
                                    val latLog = message.split(",")
                                    mDestinationLatitude = latLog.get(0).toDouble()
                                    mDestinationLongitude = latLog.get(1).toDouble()

                                    // start detect
                                    fragmentManager.beginTransaction()
                                            .add(R.id.fl_container, DetectLocationFragment.newInstance())
                                            .add(R.id.fl_container, DetectCompassFragment.newInstance())
                                            .commit()
                                }
                        })
                    }
                    override fun onConnectionSuspended(i: Int) {
                        // no-op
                    }
                })
                .build()

        val stub = findViewById(R.id.watch_view_stub) as WatchViewStub
        stub.setOnLayoutInflatedListener(object : WatchViewStub.OnLayoutInflatedListener {
            override fun onLayoutInflated(stub: WatchViewStub) {
                mRemainingDistance = stub.findViewById(R.id.remaining_distance) as TextView
                mArrowImg = stub.findViewById(R.id.arrow) as ImageView
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (null != mGoogleApiClient) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != mGoogleApiClient && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (mCanUpdateViews) {

            val locationTo: Location = Location("")
            locationTo.latitude = mDestinationLatitude
            locationTo.longitude = mDestinationLongitude

            mRemainingDistance?.text = location.distanceTo(locationTo).toInt().toString()

            val bearing: Float = location.bearingTo(locationTo)
            displayToDestination(if (0 < bearing) { 360 - bearing } else { bearing * (-1)})
        }
    }


    override fun onCompassChanged(fromAzimuth: Float, toAzimuth: Float) {
        if (mCanUpdateViews) {
            mFromAzimuth = fromAzimuth
            mToAzimuth = toAzimuth
        }
    }


    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        mCanUpdateViews = false
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
        mCanUpdateViews = true
    }


    private fun displayToDestination(bearingTo: Float) {

        val rotateFrom = mFromAzimuth - bearingTo
        val rotateTo = mToAzimuth - bearingTo

        val anim: Animation = RotateAnimation(-rotateFrom, -rotateTo,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.duration = 300
        anim.repeatCount = 0
        anim.fillAfter = true

        mArrowImg?.startAnimation(anim)
    }
}
