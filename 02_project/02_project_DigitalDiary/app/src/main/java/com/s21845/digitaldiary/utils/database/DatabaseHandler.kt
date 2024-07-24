package com.s21845.digitaldiary.utils.database

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.s21845.digitaldiary.R
import com.s21845.digitaldiary.models.HappyPlaceModel

class DatabaseHandler(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val collectionHappyPlace = "HappyPlaces"

    @SuppressLint("StringFormatInvalid")
    fun addHappyPlace(happyPlace: HappyPlaceModel, callback: (Boolean) -> Unit) {
        val docRef = db.collection(collectionHappyPlace).document()
        happyPlace.id = docRef.id
        docRef.set(happyPlace)
            .addOnSuccessListener {
                Log.d(TAG, context.getString(R.string.document_added_with_id, docRef.id))
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, context.getString(R.string.error_adding_document), e)
                callback(false)
            }
    }

    fun getHappyPlacesList(callback: (List<HappyPlaceModel>) -> Unit) {
        db.collection(collectionHappyPlace)
            .get()
            .addOnSuccessListener { result ->
                val happyPlacesList = mutableListOf<HappyPlaceModel>()
                for (document in result) {
                    val happyPlace = document.toObject(HappyPlaceModel::class.java)
                    happyPlace.id = document.id
                    happyPlacesList.add(happyPlace)
                }
                callback(happyPlacesList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, context.getString(R.string.error_getting_documents), exception)
                callback(emptyList())
            }
    }

    fun updateHappyPlace(happyPlace: HappyPlaceModel, callback: (Boolean) -> Unit) {
        if (happyPlace.id != null) {
            db.collection(collectionHappyPlace).document(happyPlace.id!!)
                .set(happyPlace)
                .addOnSuccessListener {
                    Log.d(TAG, context.getString(R.string.document_updated_successfully))
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, context.getString(R.string.error_updating_document), e)
                    callback(false)
                }
        } else {
            callback(false)
        }
    }

    companion object {
        private const val TAG = "DatabaseHandler"
    }
}
