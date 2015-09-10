package com.tebasaki.yu.wear.compass.activity

import android.location.Location
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.WatchViewStub
import android.widget.TextView
import com.tebasaki.yu.wear.compass.R
import com.tebasaki.yu.wear.compass.fragment.DetectLocationFragment

public class MainActivity : WearableActivity(),
                            DetectLocationFragment.OnLocationListener {

    private var mTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<WearableActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // supports ambient mode
        setAmbientEnabled()

        val stub = findViewById(R.id.watch_view_stub) as WatchViewStub
        stub.setOnLayoutInflatedListener(object : WatchViewStub.OnLayoutInflatedListener {
            override fun onLayoutInflated(stub: WatchViewStub) {
                mTextView = stub.findViewById(R.id.text) as TextView
                mTextView?.setText("てすと")

                getFragmentManager().beginTransaction()
                    .add(R.id.fl_container, DetectLocationFragment.newInstance())
                    .commit()

            }
        })
    }

    override fun onLocationChanged(location: Location) {
        mTextView?.setText("てすと:" + location.getLatitude() + "," + location.getLongitude())
    }
}
