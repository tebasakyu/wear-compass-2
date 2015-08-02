package com.tebasaki.yu.wear.compass.activity

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import com.tebasaki.yu.wear.compass.R
import com.tebasaki.yu.wear.compass.fragment.SelectPlaceFragment


public class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, SelectPlaceFragment.newInstance())
                .commit()
    }
}
