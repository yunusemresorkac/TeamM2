package com.app.gamenews.activities

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.adapter.PostAdapter
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.LikeManager
import com.app.gamenews.databinding.ActivityMyPostsBinding
import com.app.gamenews.model.Post
import com.app.gamenews.viewmodel.MyPostViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.OutputStream

class MyPostsActivity : AppCompatActivity() ,PostAdapter.PostClick{

    private lateinit var binding : ActivityMyPostsBinding
    private val myPostViewModel by viewModel<MyPostViewModel>()
    private val userViewModel by viewModel<UserViewModel>()

    private lateinit var postAdapter: PostAdapter
    private lateinit var postList : ArrayList<Post>
    private var isLoading = false

    private var firebaseUser : FirebaseUser? = null
    private var selectedPostPosition: Int = 0
    private lateinit var postId : String
    private lateinit var postOwnerId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        postId = intent.getStringExtra("postId").toString()
        postOwnerId = intent.getStringExtra("postOwnerId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser



        initRecycler()
        myPostViewModel.fetchInitialData(postOwnerId)
        observeViewModel()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true
                    myPostViewModel.loadMoreData(postOwnerId)

                }
            }
        })



    }


    private fun initRecycler(){
        postList = ArrayList()

        binding.recyclerView.setHasFixedSize(true)
        postAdapter = PostAdapter(postList, this,userViewModel,this)
        binding.recyclerView.adapter = postAdapter

        binding.recyclerView.onFlingListener = null

        val dividerItemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)



    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            myPostViewModel.list.collect { posts ->
                println("postlar $posts")
                postList.clear()
                postList.addAll(posts)
                postAdapter.notifyDataSetChanged()
                isLoading = false
                postId.let {
                    val index = postList.indexOfFirst { post -> post.postId == postId }
                    if (index != -1) {
                        selectedPostPosition = index
                        // LayoutManager'ı güncelle
                        val layoutManager = LinearLayoutManager(this@MyPostsActivity)
                        layoutManager.scrollToPosition(selectedPostPosition)
                        binding.recyclerView.layoutManager = layoutManager
                    }
                }
            }
        }
    }

    override fun clickCommentBtn(post: Post) {
        val intent = Intent(this, CommentActivity::class.java)
        intent.putExtra("postId",post.postId)
        intent.putExtra("collectionName", "Posts")
        startActivity(intent)
    }

    override fun clickLikeBtn(post: Post, likeManager: LikeManager) {
        if (firebaseUser != null){
            likeManager.toggleLike(post.postId,"Posts")

        }else{
            DummyMethods.showMotionToast(this,"Beğenmek için hesap oluşturun","",
                MotionToastStyle.INFO)
        }
    }

    override fun clickShareBtn(post: Post) {
        sharePost(post)
    }

    override fun clickPostActions(post: Post) {
        if (firebaseUser?.uid.equals(post.userId)){
            showActionsDialog("Sil","Düzenle","Gönderiyi silebilir ya da düzenleyebilirsin","",{
                myPostViewModel.deletePost(post){
                    DummyMethods.showMotionToast(this,"Gönderi Silindi","",MotionToastStyle.SUCCESS)

                }
            },{
                startActivity(Intent(this,EditPostActivity::class.java)
                    .putExtra("postId",post.postId)
                )
            })
        }else{
            showActionsDialog("Bildir","İptal","Bu gönderiyi bildirmek ister misin",
                "Bir ihlal yapıldığı kesinleşirse bu gönderi silinecektir",{

            },{

            })
        }

    }



    private fun showActionsDialog( positiveBtnText : String, negativeBtnText : String, title : String,message : String, onPositiveClick: () -> Unit, onNegativeClick : () -> Unit){

        val mDialog = MaterialDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveBtnText) { dialogInterface, which ->
                onPositiveClick()
                dialogInterface?.dismiss()
            }
            .setNegativeButton(negativeBtnText) { dialogInterface, which ->
                onNegativeClick()
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()



    }






    private fun sharePost(post: Post){
        if (DummyMethods.getWritePermission(this)){
            Glide.with(this)
                .asBitmap()
                .load(post.imageUrls[0])
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val icon: Bitmap = resource
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "image/jpeg"
                        val textToAdd = post.title
                        share.putExtra(Intent.EXTRA_TEXT, textToAdd)
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "title")
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        val uri: Uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!


                        val outstream: OutputStream
                        try {
                            outstream = contentResolver.openOutputStream(uri)!!
                            icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream)
                            outstream.close()
                        } catch (e: Exception) {
                            System.err.println(e.toString())
                        }

                        share.putExtra(Intent.EXTRA_STREAM, uri)
                        startActivity(Intent.createChooser(share, "Share Image"))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }


    }


}