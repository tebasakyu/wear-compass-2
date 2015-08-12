package com.tebasaki.yu.wear.compass.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import butterknife.bindView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.tebasaki.yu.wear.compass.R
import com.tebasaki.yu.wear.compass.adapter.PlaceAutocompleteAdapter
import kotlin.platform.platformStatic


public class SelectPlaceFragment : Fragment() {

    private var mGoogleApiClient: GoogleApiClient? = null

    private val startPlacePickerBtn: Button by bindView(R.id.startPlacePickerBtn)
    private val placeName: TextView by bindView(R.id.placeName)
    private val phoneNumber: TextView by bindView(R.id.phoneNumber)
    private val webSiteUrl: TextView by bindView(R.id.webSiteUrl)

    private val autocompletePlaces: AutoCompleteTextView by bindView(R.id.autocomplete_places)
    private var mAdpter: PlaceAutocompleteAdapter? = null

    companion object {

        private val TAG: String = SelectPlaceFragment.javaClass.getSimpleName()
        private val PLACE_PICKER_REQUEST: Int = 1000
        private val BOUNDS_GREATER_SYDNEY: LatLngBounds = LatLngBounds(
                LatLng(-34.041458, 150.790100), LatLng(-33.682247, 151.383362)
        )

        platformStatic fun newInstance() : SelectPlaceFragment {
            return SelectPlaceFragment()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGoogleApiClient = GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .build()

    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
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

        mAdpter = PlaceAutocompleteAdapter(getActivity(), android.R.layout.simple_list_item_1,
                mGoogleApiClient!!, BOUNDS_GREATER_SYDNEY, null)
        autocompletePlaces.setAdapter(mAdpter)
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
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