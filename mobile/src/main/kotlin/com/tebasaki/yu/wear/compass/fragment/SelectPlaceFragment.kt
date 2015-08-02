package com.tebasaki.yu.wear.compass.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.bindView
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.tebasaki.yu.wear.compass.R
import kotlin.platform.platformStatic


public class SelectPlaceFragment : Fragment() {

    private val startPlacePickerBtn: Button by bindView(R.id.startPlacePickerBtn)
    private val placeName: TextView by bindView(R.id.placeName)
    private val phoneNumber: TextView by bindView(R.id.phoneNumber)
    private val webSiteUrl: TextView by bindView(R.id.webSiteUrl)

    companion object {

        private val PLACE_PICKER_REQUEST: Int = 1000

        platformStatic fun newInstance() : SelectPlaceFragment {
            return SelectPlaceFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_select_place, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startPlacePickerBtn.setOnClickListener {
            val builder: PlacePicker.IntentBuilder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (PLACE_PICKER_REQUEST == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                val place: Place = PlacePicker.getPlace(data, getActivity())
                placeName.setText(place.getName())
                phoneNumber.setText(place.getPhoneNumber())
                webSiteUrl.setText(place.getWebsiteUri()?.toString())
            }
        }
    }
}