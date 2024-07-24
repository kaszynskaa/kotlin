package com.s21845.digitaldiary.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log
import com.s21845.digitaldiary.R
import java.io.IOException
import java.util.*

//A AsyncTask class to get the address from latitude and longitude
class GetAddressFromLatLng(
    context: Context, private val latitude: Double,
    private val longitude: Double
) : AsyncTask<Void, String, String>() {

    //Constructs a Geocoder whose responses will be localized for the given Locale.
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private val context: Context = context

    //A variable of address listener interface.
    private lateinit var mAddressListener: AddressListener

    //Background method of AsyncTask where the background operation will be performed.
    override fun doInBackground(vararg params: Void?): String {
        try {
            /*
             * Returns an array of Addresses that are known to describe the
             * area immediately surrounding the given latitude and longitude.
             */
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(",")
                }
                sb.deleteCharAt(sb.length - 1)
                return sb.toString()
            }
        } catch (e: IOException) {
            Log.e("HappyPlaces", context.getString(R.string.unable_to_connect_to_geocoder))
        }

        return ""
    }

    //onPostExecute method of AsyncTask where the result will be received and assigned to the interface accordingly.
    override fun onPostExecute(resultString: String?) {
        if (resultString == null) {
            mAddressListener.onError()
        } else {
            mAddressListener.onAddressFound(resultString)
        }
        super.onPostExecute(resultString)
    }

    //A public function to set the AddressListener.
    fun setAddressListener(addressListener: AddressListener) {
        mAddressListener = addressListener
    }

    //A public function to execute the AsyncTask from the class it is called.
    fun getAddress() {
        execute()
    }

    //An interface for AddressListener which contains the function like success and error.
    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }
}