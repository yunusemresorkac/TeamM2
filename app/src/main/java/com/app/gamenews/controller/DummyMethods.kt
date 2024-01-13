package com.app.gamenews.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.app.gamenews.R
import com.app.gamenews.activities.MainActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DummyMethods {

    companion object{

        private const val SECOND = 1
        private const val MINUTE = 60 * SECOND
        private const val HOUR = 60 * MINUTE
        private const val DAY = 24 * HOUR
        private const val MONTH = 30 * DAY
        private const val YEAR = 12 * MONTH


        fun generateRandomString(length: Int): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }



        fun getWritePermission(context: Context ): Boolean {
            var checkPermission = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Dexter.withActivity(context as Activity?)
                    .withPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            checkPermission = true
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            checkPermission = false
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken
                        ) {
                            token.continuePermissionRequest()
                        }
                    }).check()
            }else{
                Dexter.withActivity(context as Activity?)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            checkPermission = true
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            checkPermission = false
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken
                        ) {
                            token.continuePermissionRequest()
                        }
                    }).check()

            }
            return checkPermission


        }

        fun getReadGalleryPermission(context: Context ): Boolean {
            var checkPermission = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Dexter.withActivity(context as Activity?)
                    .withPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            checkPermission = true
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            checkPermission = false
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken
                        ) {
                            token.continuePermissionRequest()
                        }
                    }).check()
            }else{
                Dexter.withActivity(context as Activity?)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            checkPermission = true
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            checkPermission = false
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken
                        ) {
                            token.continuePermissionRequest()
                        }
                    }).check()

            }
            return checkPermission


        }




        fun getTimeAgo(time : Long): String {
            val now = System.currentTimeMillis()

            val diff = (now - time) / 1000

            return when {
                diff < MINUTE -> "Şimdi"
                diff < 2 * MINUTE -> "1 dk önce"
                diff < 60 * MINUTE -> "${diff / MINUTE} dk önce"
                diff < 2 * HOUR -> "bir saat önce"
                diff < 24 * HOUR -> "${diff / HOUR} saat önce"
                diff < 2 * DAY -> "dün"
                diff < 30 * DAY -> "${diff / DAY} gün önce"
                diff < 2 * MONTH -> "bir ay önce"
                diff < 12 * MONTH -> "${diff / MONTH} ay önce"
                diff < 2 * YEAR -> "bir yıl önce"
                else -> "${diff / YEAR} yıl önce"
            }
        }

        fun formatNumber(value: Int): String {
            if (value < 1000) {
                return value.toString()
            } else if (value < 1_000_000) {
                val dividedValue = value / 1000.0
                return String.format("%.1fk", dividedValue)
            } else if (value < 1_000_000_000) {
                val dividedValue = value / 1_000_000.0
                return String.format("%.1fM", dividedValue)
            } else if (value < 1_000_000_000_000) {
                val dividedValue = value / 1_000_000_000.0
                return String.format("%.1fB", dividedValue)
            }
            return value.toString()
        }

        fun convertTime(time: Long): String? {
            val formatter = SimpleDateFormat("dd MMMM k:mm")
            return formatter.format(Date(time.toString().toLong()))
        }


        @SuppressLint("SimpleDateFormat")
        fun convertToMillis(dateString: String): Long {
            val format = SimpleDateFormat("dd/M/yyyy HH:mm:ss",Locale.ENGLISH)
            val date: Date = format.parse(dateString)
            return date.time
        }

        fun isEmulator(): Boolean {
            return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.HARDWARE.contains("goldfish")
                    || Build.HARDWARE.contains("ranchu")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion") || Build.PRODUCT.startsWith("sdk") && Build.PRODUCT.endsWith(
                "google"
            ))
        }


        fun showMotionToast(context: Context, title :String, message: String, style: MotionToastStyle ){

            MotionToast.createToast(
                context as Activity,
                title,
                message,
                style,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, R.font.coiny))
        }

        fun isDeviceRooted(): Boolean {
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }
            try {
                val file = File("/system/app/Superuser.apk")
                if (file.exists()) {
                    return true
                }
            } catch (e: Exception) {
                // Exception handling
            }
            return false
        }


        fun isCurrentDateGreaterThanOneMonthLater(millis: Long): Boolean {
            val currentDate = Calendar.getInstance()
            val oneMonthLater = Calendar.getInstance()

            oneMonthLater.timeInMillis = millis
            oneMonthLater.add(Calendar.MONTH, 1)

            return currentDate.timeInMillis > oneMonthLater.timeInMillis
        }


        fun isDeviceDarkMode(context: Context): Boolean {
            val currentNightMode = context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK
            return currentNightMode == UI_MODE_NIGHT_YES
        }


        fun formatDoubleNumber(number: Double): String {
            val decimalFormat = DecimalFormat("0.0")
            return decimalFormat.format(number)
        }

        fun formatIsoDate(isoDate: String): String {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(isoDate)

            val goodFormat = SimpleDateFormat("dd MMMM HH:mm") // Türkçe olarak örnek
            return goodFormat.format(date!!)
        }

        fun copyText(context: Context, label  :String, text : String){
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label,text)
            clipboard.setPrimaryClip(clip)
        }


    }




}