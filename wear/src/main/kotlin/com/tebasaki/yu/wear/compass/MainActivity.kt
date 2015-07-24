package com.tebasaki.yu.wear.compass

import android.app.Activity
import android.os.Bundle
import android.support.wearable.view.WatchViewStub
import android.widget.TextView

public class MainActivity : Activity() {

    private var mTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val stub = findViewById(R.id.watch_view_stub) as WatchViewStub
        stub.setOnLayoutInflatedListener(object : WatchViewStub.OnLayoutInflatedListener {
            override fun onLayoutInflated(stub: WatchViewStub) {
                mTextView = stub.findViewById(R.id.text) as TextView
            }
        })
    }
}
