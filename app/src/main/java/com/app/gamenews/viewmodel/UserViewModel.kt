package com.app.gamenews.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.app.gamenews.model.User
import com.app.gamenews.repo.UserRepo

class UserViewModel (application: Application) : AndroidViewModel(application){

    private val repo : UserRepo = UserRepo()

    fun getUserRealTime(context : Context, userId: String, callback: (User?) -> Unit) {
        repo.getUserRealTime(context, userId, callback)
    }

    fun getUser(context : Context, userId: String, callback: (User?) -> Unit) {
        repo.getUser(context, userId, callback)
    }


    fun getUsername(context : Context, userId: String, callback: (String) -> Unit) {
        repo.getUsername(context, userId, callback)
    }

    fun getUserImage(context : Context, userId: String, callback: (String) -> Unit) {
        repo.getUserImage(context, userId, callback)
    }

    fun getUserFollowers(context : Context, userId: String, callback: (Int) -> Unit) {
        repo.getUserFollowers(context, userId, callback)
    }

    fun getUserFollowing(context : Context, userId: String, callback: (Int) -> Unit) {
        repo.getUserFollowing(context, userId, callback)
    }

    fun getUserCoin(context : Context, userId: String, callback: (Double) -> Unit) {
        repo.getUserCoin(context, userId, callback)
    }

    fun updateCoin(context: Context, userId: String, amount : Double){
        repo.updateCoin(context, userId, amount)
    }




    fun updateStatusOffline(context: Context, userId: String){
        repo.updateStatusOffline(context, userId)
    }
    fun updateStatusOnline(context: Context, userId: String){
        repo.updateStatusOnline(context, userId)
    }

    fun updateToken(token: String, userId: String) {
        repo.updateToken(token, userId)
    }


}