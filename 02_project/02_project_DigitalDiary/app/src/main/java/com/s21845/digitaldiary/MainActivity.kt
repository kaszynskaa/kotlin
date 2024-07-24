package com.s21845.digitaldiary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.Geofence
import com.s21845.digitaldiary.activities.AddHappyPlaceActivity
import com.s21845.digitaldiary.activities.AuthenticationActivity
import com.s21845.digitaldiary.activities.GeofenceHelper
import com.s21845.digitaldiary.activities.HappyPlaceDetailsActivity
import com.s21845.digitaldiary.databinding.ActivityMainBinding
import com.s21845.digitaldiary.models.HappyPlaceModel
import com.s21845.digitaldiary.utils.SharedPrefManager
import com.s21845.digitaldiary.utils.adapters.HappyPlacesAdapter
import com.s21845.digitaldiary.utils.database.DatabaseHandler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var geofenceHelper: GeofenceHelper

    companion object {
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start AuthenticationActivity if the user is not authenticated
        if (!SharedPrefManager.isPasswordSet(this)) {
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_CODE_LOCATION_PERMISSION
            )
        } else {
            setupGeofences()
        }

        // Load happy places from Firebase on app launch
        getHappyPlacesListFromFirebase()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
                getHappyPlacesListFromFirebase()
                setupGeofences()
            }
        } else {
            Log.e("Activity", "Cancelled or Back Pressed")
        }
    }

    private fun getHappyPlacesListFromFirebase() {
        val dbHandler = DatabaseHandler(this)
        dbHandler.getHappyPlacesList { happyPlaceList ->
            if (happyPlaceList.isNotEmpty()) {
                Log.d("MainActivity", "Happy Places List: $happyPlaceList")
                binding.rvHappyPlacesList.visibility = View.VISIBLE
                binding.tvNoRecordsAvailable.visibility = View.GONE
                setupHappyPlacesRecyclerView(happyPlaceList)
            } else {
                Log.d("MainActivity", "No happy places found.")
                binding.rvHappyPlacesList.visibility = View.GONE
                binding.tvNoRecordsAvailable.visibility = View.VISIBLE
            }
        }
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList: List<HappyPlaceModel>) {
        binding.rvHappyPlacesList.layoutManager = LinearLayoutManager(this)
        binding.rvHappyPlacesList.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, ArrayList(happyPlaceList))
        binding.rvHappyPlacesList.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        placesAdapter.attachSwipeHandler(binding.rvHappyPlacesList)
        placesAdapter.setOnEditListener { position ->
            val place = happyPlaceList[position]
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            intent.putExtra(EXTRA_PLACE_DETAILS, place)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun setupGeofences() {
        geofenceHelper = GeofenceHelper(this)
        val dbHandler = DatabaseHandler(this)
        dbHandler.getHappyPlacesList { happyPlaces ->
            for (place in happyPlaces) {
                geofenceHelper.addGeofence(
                    place.id.toString(),
                    place.latitude,
                    place.longitude,
                    1000f, // 1 km radius
                    Geofence.NEVER_EXPIRE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            setupGeofences()
        }
    }
}
