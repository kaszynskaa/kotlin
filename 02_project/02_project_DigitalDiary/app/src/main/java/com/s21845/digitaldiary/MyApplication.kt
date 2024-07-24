package com.s21845.digitaldiary

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        signInAnonymously()
    }

    private fun signInAnonymously() {
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MyApplication", "signInAnonymously:success")
                } else {
                    Log.w("MyApplication", "signInAnonymously:failure", task.exception)
                }
            }
    }
}
