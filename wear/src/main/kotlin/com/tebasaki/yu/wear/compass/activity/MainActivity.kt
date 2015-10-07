package com.tebasaki.yu.wear.compass.activity

import android.location.Location
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.WatchViewStub
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.tebasaki.yu.wear.compass.R
import com.tebasaki.yu.wear.compass.fragment.DetectCompassFragment
import com.tebasaki.yu.wear.compass.fragment.DetectLocationFragment

public class MainActivity : WearableActivity(),
                            DetectLocationFragment.OnLocationListener,
                            DetectCompassFragment.OnCompassListener {

    private var mCanUpdateViews: Boolean = false
    private var mFromAzimuth: Float = 0f
    private var mToAzimuth: Float = 0f

    private var mImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // supports ambient mode
        setAmbientEnabled()

        val stub = findViewById(R.id.watch_view_stub) as WatchViewStub
        stub.setOnLayoutInflatedListener(object : WatchViewStub.OnLayoutInflatedListener {
            override fun onLayoutInflated(stub: WatchViewStub) {
                mImageView = stub.findViewById(R.id.img) as ImageView

                fragmentManager.beginTransaction()
                        .add(R.id.fl_container, DetectLocationFragment.newInstance())
                        .add(R.id.fl_container, DetectCompassFragment.newInstance())
                        .commit()
            }
        })
    }

    override fun onLocationChanged(location: Location) {
        if (mCanUpdateViews) {

            // debug code 中目黒駅
            val locationTo: Location = Location("")
            locationTo.latitude = 35.6442877
            locationTo.longitude = 139.6990956

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
        mCanUpdateViews = true
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
        mCanUpdateViews = false
    }


    private fun displayToDestination(bearingTo: Float) {

        val rotateFrom = bearingTo - mFromAzimuth
        val rotateTo = bearingTo - mToAzimuth

        val anim: Animation = RotateAnimation(-rotateFrom, -rotateTo,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.duration = 300
        anim.repeatCount = 0
        anim.fillAfter = true

        mImageView?.startAnimation(anim)
    }
}
