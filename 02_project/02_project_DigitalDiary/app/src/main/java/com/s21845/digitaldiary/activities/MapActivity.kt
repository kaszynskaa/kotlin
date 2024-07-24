package com.s21845.digitaldiary.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.s21845.digitaldiary.MainActivity
import com.s21845.digitaldiary.R
import com.s21845.digitaldiary.databinding.ActivityMapBinding
import com.s21845.digitaldiary.models.HappyPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private var mMap: GoogleMap? = null
    private var mHappyPlaceDetails: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMap)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        binding.toolbarMap.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mHappyPlaceDetails?.let {
            val position = LatLng(it.latitude, it.longitude)
            mMap?.addMarker(MarkerOptions().position(position).title(it.title))
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }
}