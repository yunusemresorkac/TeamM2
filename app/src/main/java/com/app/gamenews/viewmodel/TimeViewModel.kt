package com.app.gamenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.gamenews.repo.TimeRepo

class TimeViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : TimeRepo = TimeRepo()

    fun getNowFromApi() : Long  {
         return repo.getNowFromApi()
    }


}