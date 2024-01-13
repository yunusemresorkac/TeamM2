package com.app.gamenews.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.app.gamenews.R
import com.app.gamenews.adapter.GalleryAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ActivityAddPostBinding
import com.app.gamenews.model.Post
import com.app.gamenews.viewmodel.PostViewModel
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

class AddPostActivity : AppCompatActivity(), GalleryAdapter.GalleryClick {

    private lateinit var binding : ActivityAddPostBinding
    private var firebaseUser : FirebaseUser? = null
    private val postViewModel by viewModel<PostViewModel>()
    private lateinit var galleryAdapter: GalleryAdapter
    private val imageList: MutableList<Bitmap> = mutableListOf()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        initRecycler()

        binding.galleryBtn.setOnClickListener {
            if (DummyMethods.getReadGalleryPermission(this)){
                dispatchGalleryIntent()
            }
        }

        binding.sharePost.setOnClickListener {
            if (binding.titleEt.text.toString().trim().isEmpty()){
                DummyMethods.showMotionToast(this,"Lütfen Başlık Girin","",MotionToastStyle.ERROR)
                return@setOnClickListener
            }
            if (imageList.size < 1){
                DummyMethods.showMotionToast(this,"Lütfen En Az Bir Resim Seçin","",MotionToastStyle.ERROR)
                return@setOnClickListener
            }
            savePost()
        }

    }

    private fun initRecycler(){
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        galleryAdapter = GalleryAdapter(imageList,this)
        binding.recyclerView.adapter = galleryAdapter
    }

    private fun sharePost(images : MutableList<String>){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val post = firebaseUser?.let {
                    Post(
                        it.uid,DummyMethods.generateRandomString(10),System.currentTimeMillis(),
                        binding.titleEt.text.toString().trim(),images,0,0)
                }

                postViewModel.sharePost(post!!,{onSuccess()},{onFailure()})
            }catch (e: Exception) {
               e.printStackTrace()
            }
        }




    }

    private fun onSuccess(){
        DummyMethods.showMotionToast(this,"Gönderi Paylaşıldı","",MotionToastStyle.SUCCESS)
        finish()
    }

    private fun onFailure(){
        DummyMethods.showMotionToast(this,"Bir şeyler  ters gitti.","",MotionToastStyle.ERROR)
        finish()
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
                    if (data?.clipData != null) {
                        // Birden fazla resim seçildi
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                            imageList.add(imageBitmap)

                        }
                    } else if (data?.data != null) {
                        // Tek resim seçildi
                        val imageUri = data.data
                        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        imageList.add(imageBitmap)

                    }
                    galleryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun savePost() {
            val pd = ProgressDialog(this)
            pd.setCancelable(false)
            pd.show()

            val imageUrls = mutableListOf<String>()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    for (imageBitmap in imageList) {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , byteArrayOutputStream)
                        val imageData = byteArrayOutputStream.toByteArray()

                        val storageReference = firebaseStorage.reference.child("PostImages/${System.currentTimeMillis()}.jpg")

                        val uploadTask = storageReference.putBytes(imageData)
                            .addOnProgressListener {
                                val progress: Double =
                                    100.0 * it.bytesTransferred / it.totalByteCount
                                val currentProgress = progress.toInt()
                                pd.setMessage("Yükleniyor... $currentProgress%")
                            }.await()
                        val imageUrl = uploadTask.storage.downloadUrl.await().toString()
                        imageUrls.add(imageUrl)
                    }

                    withContext(Dispatchers.Main) {
                        sharePost(imageUrls)
                        pd.dismiss()

                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        pd.dismiss()
                        Toast.makeText(
                            this@AddPostActivity,
                            "Resimleri kaydetme hatası: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }





    override fun deletePhoto(position : Int) {
        imageList.removeAt(position)
        galleryAdapter.notifyDataSetChanged()
    }


}

