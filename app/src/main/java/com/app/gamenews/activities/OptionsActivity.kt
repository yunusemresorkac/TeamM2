package com.app.gamenews.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.app.gamenews.R
import com.app.gamenews.databinding.ActivityOptionsBinding
import com.yeslab.fastprefs.FastPrefs

class OptionsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        val prefs = FastPrefs(this)
        val booleanValue = prefs.getBoolean("night_mode", true)

        binding.switchCompat.isChecked = booleanValue
        binding.switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.switchCompat.isChecked = true
                prefs.setBoolean("night_mode",true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.switchCompat.isChecked = false
                prefs.setBoolean("night_mode",false)

            }
        }


    }



    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}