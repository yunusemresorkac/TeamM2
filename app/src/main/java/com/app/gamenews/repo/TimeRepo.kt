package com.app.gamenews.repo

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Calendar

class TimeRepo {

    fun getNowFromApi(): Long = runBlocking {
        val calendar = Calendar.getInstance()
        val url = "https://www.timeapi.io/api/Time/current/zone?timeZone=Europe/Istanbul"

        try {
            val response = withContext(Dispatchers.IO) { URL(url).readText() }
            val jsonObject = JSONObject(response)

            val year = jsonObject.getInt("year")
            val month = jsonObject.getInt("month")
            val day = jsonObject.getInt("day")
            val hour = jsonObject.getInt("hour")
            val minute = jsonObject.getInt("minute")
            val seconds = jsonObject.getInt("seconds")

            Log.e("TAG", "getNowFromApi: "+"YEAR: " + year + "MONTH:" + month + "DAY: "+day +"HOUR: "+hour+"MINUTE: "+minute + "SECONDS: "+seconds )

            calendar.set(year, month - 1, day, hour, minute, seconds)
            return@runBlocking calendar.timeInMillis
        } catch (e: Exception) {
            Log.e("TAG", "getNowFromApi: "+e.message.toString())
            e.printStackTrace()

            return@runBlocking System.currentTimeMillis()
        }
    }





}