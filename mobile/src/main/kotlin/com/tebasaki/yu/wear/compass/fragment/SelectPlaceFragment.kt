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

    private val mStartPlacePickerBtn: Button by bindView(R.id.startPlacePickerBtn)
    private val mPlaceName: TextView by bindView(R.id.placeName)
    private val mPhoneNumber: TextView by bindView(R.id.phoneNumber)
    private val mWebSiteUrl: TextView by bindView(R.id.webSiteUrl)
    private val mLatitudeText: TextView by bindView(R.id.latitude)
    private val mLongitudeText: TextView by bindView(R.id.longitude)
    private val mSendDataToWearBtn: Button by bindView(R.id.sendDataToWearBtn)

    private val mAutocompletePlaces: AutoCompleteTextView by bindView(R.id.autocomplete_places)
    private var mAdapter: PlaceAutocompleteAdapter? = null

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    companion object {

        private val TAG: String = SelectPlaceFragment.javaClass.simpleName
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

        // use GoogleApiClient
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


    /**
     * View injection complete when onActivityCreated
     * so buttons setOnClickListener on this.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mStartPlacePickerBtn.setOnClickListener {
            val builder: PlacePicker.IntentBuilder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
        }

        mSendDataToWearBtn.setOnClickListener {
            if (0.0 == mLatitude || 0.0 == mLongitude) {
                showToast("Please pick or input place")
                return@setOnClickListener
            }
            // Send data to wear, Use MessageApi
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

        // Edit place with auto complete
        mAdapter = PlaceAutocompleteAdapter(activity, R.layout.item_auto_complete_text,
                mGoogleApiClient!!, BOUNDS_GREATER_SYDNEY, null)
        mAutocompletePlaces.setAdapter(mAdapter)
        mAutocompletePlaces.onItemClickListener = mAutocompleteClickListener
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }


    /**
     * For PLACE_PICKER_UI
     */
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

            mAutocompletePlaces.setText(item?.description)
            mAutocompletePlaces.setSelection(mAutocompletePlaces.text.length())

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
        mPlaceName.text = place.name
        // Tel
        mPhoneNumber.text = if (!TextUtils.isEmpty(place.phoneNumber)) {
            place.phoneNumber
        } else {
            getText(R.string.none)
        }
        // Web URL
        mWebSiteUrl.text = if (null != place.websiteUri) {
            place.websiteUri.toString()
        } else {
            getText(R.string.none)
        }

        // Local
        val local: LatLng = place.latLng
        mLatitude = local.latitude
        mLongitude = local.longitude
        mLatitudeText.text = mLatitude.toString()
        mLongitudeText.text = mLongitude.toString()
    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}