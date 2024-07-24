package com.s21845.digitaldiary.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.s21845.digitaldiary.R
import com.s21845.digitaldiary.databinding.ActivityDrawTextBinding
import com.s21845.digitaldiary.views.DrawTextView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class DrawTextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDrawText)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarDrawText.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }
        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(binding.ivBaseImage)
        }

        binding.btnDrawText.setOnClickListener {
            val text = binding.etCaption.text.toString()
            if (text.isNotEmpty()) {
                binding.drawTextView.setText(text)
            } else {
                Toast.makeText(this, getString(R.string.enter_text), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveDrawnImage.setOnClickListener {
            saveImageWithTextAndDrawing()
        }
    }

    private fun saveImageWithTextAndDrawing() {
        val bitmap = createBitmapFromView(binding.ivBaseImage, binding.drawTextView)
        val uri = saveBitmapToFile(bitmap)
        if (uri != null) {
            val intent = Intent().apply {
                putExtra("image_uri", uri.toString())
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.failed_to_save_image), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBitmapFromView(imageView: ImageView, drawTextView: DrawTextView): Bitmap {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        drawTextView.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        val filename = "${UUID.randomUUID()}.jpg"
        val file = File(filesDir, filename)
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
