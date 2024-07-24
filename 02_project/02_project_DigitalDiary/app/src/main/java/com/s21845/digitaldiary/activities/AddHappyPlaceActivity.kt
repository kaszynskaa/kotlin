package com.s21845.digitaldiary.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.s21845.digitaldiary.MainActivity
import com.s21845.digitaldiary.R
import com.s21845.digitaldiary.databinding.ActivityAddHappyPlaceBinding
import com.s21845.digitaldiary.models.HappyPlaceModel
import com.s21845.digitaldiary.utils.database.DatabaseHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddHappyPlaceBinding
    private var saveImageToInternalStorage: Uri? = null
    private var audioFilePath: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var cal = Calendar.getInstance()
    private var placeDetails: HappyPlaceModel? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val contentURI = result.data?.data
            if (contentURI != null) {
                try {
                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    saveImageToInternalStorage = saveImageToInternalStorage(contentURI)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, getString(R.string.failed_to_load_image), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_getting_selected_file), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val drawTextActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUriString = result.data?.getStringExtra("image_uri")
            if (imageUriString != null) {
                saveImageToInternalStorage = Uri.parse(imageUriString)
                binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            placeDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
            placeDetails?.let {
                binding.etTitle.setText(it.title)
                if (!it.image.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.image)
                        .into(binding.ivPlaceImage)
                    saveImageToInternalStorage = Uri.parse(it.image)
                }
                binding.etDescription.setText(it.description)
                binding.etDate.setText(it.date)
                binding.etLocation.setText(it.location)
                mLatitude = it.latitude
                mLongitude = it.longitude
                audioFilePath = it.audio
            }
        }

        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.tvSelectCurrentLocation.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.btnRecordAudio.setOnClickListener(this)
        binding.btnAddCaption.setOnClickListener(this)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this@AddHappyPlaceActivity,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.ivPlaceImage.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher.launch(galleryIntent)
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.et_date -> {}
            R.id.tv_add_image -> {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryActivityResultLauncher.launch(galleryIntent)
            }
            R.id.tv_select_current_location -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                } else {
                    getCurrentLocation()
                }
            }
            R.id.btn_save -> {
                when {
                    binding.etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.please_enter_title), Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null && placeDetails?.image.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.please_select_image), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if (placeDetails == null) null else placeDetails!!.id,
                            binding.etTitle.text.toString(),
                            saveImageToInternalStorage?.toString() ?: placeDetails?.image,
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            mLatitude,
                            mLongitude,
                            audioFilePath,
                            binding.btnAddCaption.text.toString()
                        )

                        val dbHandler = DatabaseHandler(this)

                        if (placeDetails == null) {
                            uploadImageToFirebaseStorage(saveImageToInternalStorage!!) { imageUrl ->
                                happyPlaceModel.image = imageUrl
                                dbHandler.addHappyPlace(happyPlaceModel) { success ->
                                    if (success) {
                                        Toast.makeText(this, getString(R.string.details_inserted_successfully), Toast.LENGTH_SHORT).show()
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    } else {
                                        Toast.makeText(this, getString(R.string.failed_to_add_details), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            if (saveImageToInternalStorage != null && saveImageToInternalStorage.toString() != placeDetails!!.image) {
                                uploadImageToFirebaseStorage(saveImageToInternalStorage!!) { imageUrl ->
                                    happyPlaceModel.image = imageUrl
                                    dbHandler.updateHappyPlace(happyPlaceModel) { success ->
                                        if (success) {
                                            Toast.makeText(this, getString(R.string.details_updated_successfully), Toast.LENGTH_SHORT).show()
                                            setResult(Activity.RESULT_OK)
                                            finish()
                                        } else {
                                            Toast.makeText(this, getString(R.string.failed_to_update_details), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                dbHandler.updateHappyPlace(happyPlaceModel) { success ->
                                    if (success) {
                                        Toast.makeText(this, getString(R.string.details_updated_successfully), Toast.LENGTH_SHORT).show()
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    } else {
                                        Toast.makeText(this, getString(R.string.failed_to_update_details), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            R.id.btn_record_audio -> {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            }
            R.id.btn_add_caption -> {
                val intent = Intent(this, DrawTextActivity::class.java)
                intent.putExtra("image_uri", saveImageToInternalStorage.toString())
                drawTextActivityResultLauncher.launch(intent)
            }

        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            audioFilePath = "${externalCacheDir?.absolutePath}/${UUID.randomUUID()}.3gp"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                    isRecording = true
                    binding.btnRecordAudio.text = getString(R.string.stop_recording)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@AddHappyPlaceActivity, getString(R.string.recording_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            isRecording = false
            binding.btnRecordAudio.text = getString(R.string.start_recording)
            Toast.makeText(this@AddHappyPlaceActivity, getString(R.string.audio_recorded_successfully), Toast.LENGTH_SHORT).show()
        }
        mediaRecorder = null
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mLatitude = location.latitude
                    mLongitude = location.longitude
                    binding.etLocation.setText(getAddressFromLatLng(mLatitude, mLongitude))
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.get(0)?.getAddressLine(0) ?: "Unknown Location"
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY && data != null) {
                val contentURI = data.data
                try {
                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    saveImageToInternalStorage = contentURI
                    binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    saveImageToInternalStorage = saveImageToInternalStorage(contentURI)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AddHappyPlaceActivity, getString(R.string.failed_to_load_image_from_gallery), Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == PLACE_PICKER_REQUEST && data != null) {
                val selectedLocation = data.getParcelableExtra<LatLng>("selected_location")
                if (selectedLocation != null) {
                    mLatitude = selectedLocation.latitude
                    mLongitude = selectedLocation.longitude
                    binding.etLocation.setText("${selectedLocation.latitude}, ${selectedLocation.longitude}")
                }
            }
        }
    }

    private fun selectImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(galleryIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_read_external_storage), Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_record_audio), Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_access_location), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri?): Uri {
        val filename = "${UUID.randomUUID()}.jpg"
        val file = File(filesDir, filename)
        try {
            val inputStream: InputStream? = uri?.let { contentResolver.openInputStream(it) }
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream?.read(buffer).also { length = it!! }!! > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SaveImage", "Error saving image: ${e.message}")
        }
        return Uri.fromFile(file)
    }

    private fun uploadImageToFirebaseStorage(fileUri: Uri, callback: (String) -> Unit) {
        val ref = storageReference.child("images/${UUID.randomUUID()}")
        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("FirebaseStorage", "File uploaded successfully. Download URL: $uri")
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorage", "File upload failed", e)
            }
    }

    companion object {
        private const val GALLERY = 1
        private const val PLACE_PICKER_REQUEST = 2
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 3
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val LOCATION_PERMISSION_REQUEST_CODE = 4
        private const val PERMISSION_REQUEST_CODE = 5
    }
}
