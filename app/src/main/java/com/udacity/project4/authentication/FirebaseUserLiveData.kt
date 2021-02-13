package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser?>() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val firebaseAuthListner = FirebaseAuth.AuthStateListener { auhtState ->
        value = auhtState.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(firebaseAuthListner)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(firebaseAuthListner)
    }
}