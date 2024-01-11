package com.app.gamenews.activities

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.app.gamenews.R
import com.app.gamenews.databinding.ActivityOpeningBinding
import com.yeslab.fastprefs.FastPrefs

class OpeningActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOpeningBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = FastPrefs(this)
        val booleanValue = prefs.getBoolean("night_mode", true)

        if (booleanValue){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            prefs.setBoolean("night_mode", true)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            prefs.setBoolean("night_mode", false)

        }
        updateStatusBarColor()


        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

        },2000)


    }

    private fun updateStatusBarColor() {
        val isDarkMode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)

        if (isDarkMode) {
            // Karanlık moddaysa
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        } else {
            // Normal moddaysa
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }





}