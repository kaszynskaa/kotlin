package com.s21845.digitaldiary.activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.s21845.digitaldiary.R
import com.s21845.digitaldiary.databinding.ActivityHappyPlaceDetailsBinding
import com.s21845.digitaldiary.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.s21845.digitaldiary.MainActivity

class HappyPlaceDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityHappyPlaceDetailsBinding
    private var mHappyPlaceDetails: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarHappyPlaceDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        binding.toolbarHappyPlaceDetail.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = mHappyPlaceDetails!!.title
            Glide.with(this)
                .load(mHappyPlaceDetails!!.image)
                .into(binding.ivPlaceImageDetails)
            binding.tvDescription.text = mHappyPlaceDetails!!.description
            binding.tvLocation.text = mHappyPlaceDetails!!.location

            binding.btnPlayAudio.setOnClickListener {
                mHappyPlaceDetails?.audio?.let { audioPath ->
                    val mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioPath)
                        prepare()
                        start()
                    }
                }
            }
            binding.btnViewOnMap.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, mHappyPlaceDetails)
                startActivity(intent)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(mHappyPlaceDetails!!.latitude, mHappyPlaceDetails!!.longitude)
        googleMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 10f))
    }
}
