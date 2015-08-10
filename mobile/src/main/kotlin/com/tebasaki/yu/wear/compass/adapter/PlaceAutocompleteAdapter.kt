package com.tebasaki.yu.wear.compass.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable


public class PlaceAutocompleteAdapter : BaseAdapter(), Filterable {

    /** Current results returned by this adapter. */
    private var mResultList: Array<PlaceAutocomplete>? = null



    override fun getCount(): Int {
        return mResultList!!.size()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        return convertView
    }

    override fun getItem(position: Int): PlaceAutocomplete {
        return mResultList!!.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getFilter(): Filter? {
        var filter: Filter = object:Filter() {

            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                throw UnsupportedOperationException()
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
                throw UnsupportedOperationException()
            }
        }
        return filter
    }


    class PlaceAutocomplete(val placeId: CharSequence, val description: CharSequence)
}