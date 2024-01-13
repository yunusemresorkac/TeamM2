package com.app.gamenews.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.app.gamenews.R
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ActivityCreateRoomBinding
import com.app.gamenews.model.Room
import com.app.gamenews.viewmodel.RoomViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream

class CreateRoomActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreateRoomBinding
    private var firebaseUser : FirebaseUser? = null
    private val roomViewModel by viewModel<RoomViewModel>()
    private var mImageBitmap : Bitmap? = null

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding.createRoom.setOnClickListener {
            if (firebaseUser!=null){
                createRoom()
            }
        }

        binding.goGallery.setOnClickListener {
            if (DummyMethods.getReadGalleryPermission(this)){
                dispatchGalleryIntent()
            }
        }

    }

    private fun createRoom(){
        if (binding.titleEt.text.toString().trim().isEmpty()){
            DummyMethods.showMotionToast(this,"Lütfen Başlık Girin","", MotionToastStyle.ERROR)
            return
        }
        if (binding.gameEt.text.toString().trim().isEmpty()){
            DummyMethods.showMotionToast(this,"Lütfen Oyun Adı Girin","", MotionToastStyle.ERROR)
            return
        }
        if (binding.usersNumberEt.text.toString().trim().isEmpty()){
            DummyMethods.showMotionToast(this,"Lütfen Kişi Sayısı Girin","En Az 3 Olmalı.", MotionToastStyle.ERROR)
            return
        }

        if (binding.usersNumberEt.text.toString().toInt() < 3){
            DummyMethods.showMotionToast(this,"Oda Kapasitesi 3-10 Arası Olmalı","", MotionToastStyle.ERROR)
            return
        }
        if (binding.usersNumberEt.text.toString().toInt() > 10){
            DummyMethods.showMotionToast(this,"Oda Kapasitesi 3-10 Arası Olmalı","", MotionToastStyle.ERROR)
            return
        }
        if (mImageBitmap == null){
            DummyMethods.showMotionToast(this,"Lütfen oyuna uygun bir görsel seçin.","", MotionToastStyle.ERROR)
            return
        }



       saveAllData()



    }

    private fun roomCreated(roomId : String){
        roomViewModel.joinToRoom(roomId,firebaseUser!!.uid){
            DummyMethods.showMotionToast(this,"Oda Oluşturuldu.","",MotionToastStyle.SUCCESS)
            startActivity(Intent(this,RoomDetailsActivity::class.java)
                .putExtra("roomId",roomId))
        }

    }

    private fun failed(){
        DummyMethods.showMotionToast(this,"Bir şeyler ters gitti.","Lütfen Yeniden Deneyin",MotionToastStyle.ERROR)
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
                   if (data?.data != null) {
                       // Tek resim seçildi
                       val imageUri = data.data
                       val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                       mImageBitmap = imageBitmap
                       binding.gameImage.setImageBitmap(mImageBitmap)
                    }
                }
            }
        }
    }

    private fun saveAllData() {
        val pd = ProgressDialog(this@CreateRoomActivity)
        pd.setCancelable(false)
        pd.show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                mImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100 , byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()

                val storageReference = FirebaseStorage.getInstance().reference.child("GameImages/${System.currentTimeMillis()}.jpg")

                val uploadTask = storageReference.putBytes(imageData)
                    .addOnProgressListener {
                        val progress: Double =
                            100.0 * it.bytesTransferred / it.totalByteCount
                        val currentProgress = progress.toInt()
                        pd.setMessage("Yükleniyor... $currentProgress%")
                    }.await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val room = Room(DummyMethods.generateRandomString(10),System.currentTimeMillis(),firebaseUser!!.uid,
                            binding.usersNumberEt.text.toString().toInt(),1,binding.titleEt.text.toString()
                            ,binding.gameEt.text.toString(),binding.discordEt.text.toString().trim(),imageUrl)
                        roomViewModel.createRoom(room,{roomCreated(room.roomId)}, {failed()})
                    } catch (e: Exception) {

                    }
                }


                withContext(Dispatchers.Main) {
                    pd.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                    Toast.makeText(
                        this@CreateRoomActivity,
                        "Resim kaydetme hatası: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}