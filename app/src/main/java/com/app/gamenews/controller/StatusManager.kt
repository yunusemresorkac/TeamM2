package com.app.gamenews.controller

import android.content.Context
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseUser

class StatusManager {

    fun setUserOnline(firebaseUser : FirebaseUser, userViewModel: UserViewModel, context: Context){
        userViewModel.updateStatusOnline(context, firebaseUser.uid)

    }

    fun setUserOffline(firebaseUser : FirebaseUser, userViewModel: UserViewModel, context: Context){
        userViewModel.updateStatusOffline(context, firebaseUser.uid)

    }

}