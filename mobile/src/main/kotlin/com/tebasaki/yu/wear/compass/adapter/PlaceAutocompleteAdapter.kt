package com.tebasaki.yu.wear.compass.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * I'm referenced this.
 * com.example.google.playservices.placecomplete
 */
public class PlaceAutocompleteAdapter(val mContext: Context, val mResource: Int,
                                      val mGoogleApiClient: GoogleApiClient, val mBounds: LatLngBounds,
                                      val mPlaceFilter: AutocompleteFilter) : BaseAdapter(), Filterable {

    /** Current results returned by this adapter. */
    private var mResultList: Array<PlaceAutocomplete>? = null


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        return convertView
    }

    override fun getItem(position: Int): PlaceAutocomplete? {
        return mResultList?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return if (null == mResultList) 0 else mResultList!!.size()
    }

    /**
     * override on Filterable method
     */
    override fun getFilter(): Filter? {
        var filter: Filter = object:Filter() {

            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                var results: Filter.FilterResults = Filter.FilterResults()
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getAutocomplete(constraint)
                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList
                        results.count = mResultList!!.size()
                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }
        }
        return filter
    }

    private fun getAutocomplete(constraint: CharSequence): Array<PlaceAutocomplete>? {

        if (!mGoogleApiClient.isConnected()) {
            return null
        } else {

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            val results = Places.GeoDataApi.getAutocompletePredictions(
                    mGoogleApiClient, constraint.toString(), mBounds, mPlaceFilter)

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            val autocompletePredictions = results.await(60, TimeUnit.SECONDS)

            // Confirm that the query completed successfully, otherwise return null
            val status = autocompletePredictions.getStatus()
            if (!status.isSuccess()) {
                autocompletePredictions.release()
                return null
            }

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).
            val iterator: Iterator<AutocompletePrediction> = autocompletePredictions.iterator()
            val resultList: Array<PlaceAutocomplete> = emptyArray()
            for (i in 0..autocompletePredictions.getCount()) {
                if (iterator.hasNext()) {
                    val prediction = iterator.next()
                    resultList.set(i, PlaceAutocomplete(prediction.getPlaceId(), prediction.getDescription()))
                }
            }
            // Release the buffer now that all data has been copied.
            autocompletePredictions.release()

            return resultList
        }
    }

    class PlaceAutocomplete(val placeId: CharSequence, val description: CharSequence)
}