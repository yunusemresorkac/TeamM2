package com.app.gamenews

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.app.gamenews.controller.StatusManager
import com.app.gamenews.di.appModule
import com.app.gamenews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application(){

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }


}