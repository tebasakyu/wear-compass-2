package com.tebasaki.yu.wear.compass.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.bindView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.wearable.Wearable
import com.tebasaki.yu.wear.compass.R
import com.tebasaki.yu.wear.compass.adapter.PlaceAutocompleteAdapter


public class SelectPlaceFragment : Fragment() {

    private var mGoogleApiClient: GoogleApiClient? = null

    private val startPlacePickerBtn: Button by bindView(R.id.startPlacePickerBtn)
    private val placeName: TextView by bindView(R.id.placeName)
    private val phoneNumber: TextView by bindView(R.id.phoneNumber)
    private val webSiteUrl: TextView by bindView(R.id.webSiteUrl)
    private val latitudeText: TextView by bindView(R.id.latitude)
    private val longitudeText: TextView by bindView(R.id.longitude)
    private val sendDataToWearBtn: Button by bindView(R.id.sendDataToWearBtn)

    private val autocompletePlaces: AutoCompleteTextView by bindView(R.id.autocomplete_places)
    private var mAdapter: PlaceAutocompleteAdapter? = null

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    companion object {

        private val TAG: String = SelectPlaceFragment.javaClass.getSimpleName()
        private val PLACE_PICKER_REQUEST: Int = 1000
        private val BOUNDS_GREATER_SYDNEY: LatLngBounds = LatLngBounds(
                LatLng(-34.041458, 150.790100), LatLng(-33.682247, 151.383362)
        )

        fun newInstance() : SelectPlaceFragment {
            return SelectPlaceFragment()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGoogleApiClient = GoogleApiClient.Builder(activity)
                .addApi(Places.GEO_DATA_API)
                .addApi(Wearable.API)
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
            startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
        }

        sendDataToWearBtn.setOnClickListener {
            if (0.0 == mLatitude || 0.0 == mLongitude) {
                showToast("Please pick or input place")
                return@setOnClickListener
            }
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                    { nodes ->
                        val sendStr: String = mLatitude.toString() + "," + mLongitude.toString()
                        val sendData: ByteArray = sendStr.toByteArray()
                        for (node in nodes.nodes) {
                            Wearable.MessageApi
                                    .sendMessage(mGoogleApiClient, node.id, "/locale_set", sendData)
                                    .setResultCallback(
                                            { result ->
                                                showToast(result.status.toString())
                                            }
                                    )
                        }
                    })
        }

        mAdapter = PlaceAutocompleteAdapter(activity, R.layout.item_auto_complete_text,
                mGoogleApiClient!!, BOUNDS_GREATER_SYDNEY, null)
        autocompletePlaces.setAdapter(mAdapter)
        autocompletePlaces.onItemClickListener = mAutocompleteClickListener
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (PLACE_PICKER_REQUEST == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                // to display
                displayPlaceInfo(PlacePicker.getPlace(data, activity))
            }
        }
    }


    private val mAutocompleteClickListener = object: AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            val item = mAdapter?.getItem(position)
            val placeId = item?.placeId.toString()

            autocompletePlaces.setText(item?.description)
            autocompletePlaces.setSelection(autocompletePlaces.text.length())

            val placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback)
        }
    }

    private val mUpdatePlaceDetailsCallback = object : ResultCallback<PlaceBuffer> {
        override fun onResult(places: PlaceBuffer?) {

            if (!places!!.status!!.isSuccess) {
                // Request did not complete successfully
                places.release()
                return
            }

            // Get the Place object from the buffer.
            val place = places.get(0)
            // to display
            displayPlaceInfo(place)

            places.release()
        }
    }


    private fun displayPlaceInfo(place: Place) {

        // Place name
        placeName.text = place.name
        // Tel
        if (!TextUtils.isEmpty(place.phoneNumber)) {
            phoneNumber.text = place.phoneNumber
        } else {
            phoneNumber.setText(R.string.none)
        }
        // Web URL
        if (null != place.websiteUri) {
            webSiteUrl.text = place.websiteUri.toString()
        } else {
            webSiteUrl.setText(R.string.none)
        }

        // Local
        val local: LatLng = place.latLng
        latitudeText.text = local.latitude.toString()
        longitudeText.text = local.longitude.toString()

        mLatitude = local.latitude
        mLongitude = local.longitude
    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}