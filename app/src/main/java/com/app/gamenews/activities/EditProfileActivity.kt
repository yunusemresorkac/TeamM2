package com.app.gamenews.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.app.gamenews.R
import com.app.gamenews.controller.AdsController
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ActivityEditProfileBinding
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding
    private val userViewModel by viewModel<UserViewModel>()
    private var firebaseUser : FirebaseUser? = null
    private  var myImageBitmap: Bitmap? = null

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        getUserData()
        loadAds()

        binding.profileImage.setOnClickListener{
            if (DummyMethods.getReadGalleryPermission(this)){
                dispatchGalleryIntent()
            }
        }

        binding.deleteImage.setOnClickListener {
            deleteImage()
        }

        binding.saveBtn.setOnClickListener {
            saveUsername()
        }



    }

    private fun loadAds(){
        val adsController = AdsController(this)
        adsController.loadBanner(binding.adView)
    }

    private fun saveUsername(){
        if (binding.username.text.toString().trim().isNotEmpty()){
            val map : HashMap<String,Any> = HashMap()
            map["username"] = binding.username.text.toString().trim()
            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
                .update(map).addOnSuccessListener {
                    DummyMethods.showMotionToast(this,"İsim Güncellendi","",MotionToastStyle.INFO)
                }
        }
    }

    private fun deleteImage(){
        val map : HashMap<String,Any> = HashMap()
        map["profileImage"] = ""
        FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
            .update(map).addOnSuccessListener {
                DummyMethods.showMotionToast(this,"Resim Silindi","",MotionToastStyle.INFO)

            }
    }

    private fun getUserData(){
        firebaseUser?.let { fu->
            userViewModel.getUserRealTime(this, fu.uid){
                it?.let { user ->
                    binding.username.setText(user.username)
                    if (user.profileImage.isNotEmpty()){
                        binding.deleteImage.visibility = View.VISIBLE
                        Glide.with(applicationContext).load(user.profileImage).into(binding.profileImage)
                    }else{
                        binding.deleteImage.visibility = View.GONE
                    }

                }
            }
        }

    }

    private fun dispatchGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                REQUEST_IMAGE_GALLERY -> {
                    // Galeriden seçim sonucu
                    data?.data?.let { imageUri ->
                        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        myImageBitmap = imageBitmap
                        binding.profileImage.setImageBitmap(imageBitmap)
                        saveImage()
                    }

                }
            }
        }
    }

    private fun saveImage() {
        val pd = ProgressDialog(this@EditProfileActivity)
        pd.setCancelable(false)
        pd.show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                myImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100 , byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()

                val storageReference = firebaseStorage.reference.child("ProfileImages/${System.currentTimeMillis()}.jpg")

                val uploadTask = storageReference.putBytes(imageData)
                    .addOnProgressListener {
                        val progress: Double =
                            100.0 * it.bytesTransferred / it.totalByteCount
                        val currentProgress = progress.toInt()
                        pd.setMessage("Yükleniyor... $currentProgress%")
                    }.await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()
                withContext(Dispatchers.Main) {
                    updateImageUrl(imageUrl,pd)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Resim kaydetme hatası: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateImageUrl(url : String , pd : ProgressDialog){
        val map : HashMap<String,Any> = HashMap()
        map["profileImage"] = url
        FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
            .update(map).addOnSuccessListener {
                pd.dismiss()
            }
    }



}